/*******************************************************************************
 * Copyright (c) 2011 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.gdb.launching.GDBProcess;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerCPU;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerCore;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerExecutionState;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerModel;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerThread;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.DSFDebugModel;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.DSFDebugModelListener;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.DSFSessionState;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.DebugViewUtils;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardware.ICPUDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardware.ICoreDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcessDMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.visualizer.ui.canvas.GraphicCanvas;
import org.eclipse.cdt.visualizer.ui.canvas.GraphicCanvasVisualizer;
import org.eclipse.cdt.visualizer.ui.plugin.CDTVisualizerUIPlugin;
import org.eclipse.cdt.visualizer.ui.util.Colors;
import org.eclipse.cdt.visualizer.ui.util.GUIUtils;
import org.eclipse.cdt.visualizer.ui.util.SelectionUtils;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

/**
 * The Multicore Visualizer is a generic visualizer that displays
 * CPUs, cores, threads graphically.
 * 
 * This visualizer uses the CDT Visualizer framework.
 */
@SuppressWarnings("restriction")
public class MulticoreVisualizer extends GraphicCanvasVisualizer
    implements DSFDebugModelListener
{	
	// --- constants ---
	
	/** Eclipse ID for this view */
	public static final String ECLIPSE_ID = "org.eclipse.cdt.dsf.gdb.multicorevisualizer.visualizer"; //$NON-NLS-1$

	
	// --- members ---
	
	/**
	 * The data model drawn by this visualizer.
	 */
	private VisualizerModel fDataModel;

	/** Downcast reference to canvas. */
	protected MulticoreVisualizerCanvas m_canvas;
	
	/** DSF debug context session object. */
	protected DSFSessionState m_sessionState;
	
	/** Event listener class for DSF events */
	protected MulticoreVisualizerEventListener fEventListener;
	

	// --- constructors/destructors ---
	
	/** Constructor. */
	public MulticoreVisualizer()
	{
		// initialize menu/toolbar actions
		createActions();
	}
	
	/** Dispose method. */
	@Override
	public void dispose()
	{
		super.dispose();
		disposeActions();
	}
	
	
	// --- init methods ---
	
	/** Invoked when visualizer is created, to permit any initialization. */
	@Override
	public void initializeVisualizer() {
		fEventListener = new MulticoreVisualizerEventListener(this);
	}
	
	/** Invoked when visualizer is disposed, to permit any cleanup. */
	@Override
	public void disposeVisualizer()
	{
		// handle any other cleanup
		dispose();
	}
	
	
	// --- accessors ---
	
	/** Returns non-localized unique name for this visualizer. */
	@Override
	public String getName() {
		return "multicore"; //$NON-NLS-1$
	}
	
	/** Returns localized name to display for this visualizer. */
	@Override
	public String getDisplayName() {
		// TODO: use a string resource here.
		return "Multicore Visualizer"; //$NON-NLS-1$
	}
	
	/** Returns localized tooltip text to display for this visualizer. */
	@Override
	public String getDescription() {
		// TODO: use a string resource here.
		return "Displays current state of selected debug target."; //$NON-NLS-1$
	}
	
	
	// --- canvas management ---
	
	/** Creates and returns visualizer canvas control. */
	@Override
	public GraphicCanvas createCanvas(Composite parent)
	{
		m_canvas = new MulticoreVisualizerCanvas(parent);
		m_canvas.addSelectionChangedListener(this);
		return m_canvas;
	}
	
	/** Invoked when canvas control should be disposed. */
	@Override
	public void disposeCanvas()
	{
		if (m_canvas != null) {
			m_canvas.removeSelectionChangedListener(this);
			m_canvas.dispose();
			m_canvas = null;
		}
	}
	
	/** Invoked after visualizer control creation, */
	@Override
	protected void initializeCanvas(GraphicCanvas canvas)
	{
		// Any workbench views left open at application shutdown may be instanced
		// before our plugins are fully loaded, so make sure resource manager is initialized.
		// Note: this also associates the resource manager with the Colors class;
		// until this is done, the Colors constants are null.
		CDTVisualizerUIPlugin.getResources();
		
		m_canvas.setBackground(Colors.BLACK);
		m_canvas.setForeground(Colors.GREEN);
	}
	
	/** Returns downcast reference to grid view canvas. */
	public MulticoreVisualizerCanvas getMulticoreVisualizerCanvas()
	{
		return (MulticoreVisualizerCanvas) getCanvas();
	}
	
	/** Return the data model backing this multicore visualizer */
	public VisualizerModel getModel() {
		return fDataModel;
	}
	
	
	// --- action management ---

	/** Creates actions for menus/toolbar. */
	protected void createActions()
	{
	}
	
	/** Updates actions displayed on menu/toolbars. */
	protected void updateActions()
	{
	}

	/** Updates actions specific to context menu. */
    protected void updateContextMenuActions(Point location) 
    {
    }

	/** Cleans up actions. */
	protected void disposeActions() {
	}

	
	// --- menu/toolbar management ---

	/** Invoked when visualizer is selected, to populate the toolbar. */
	@Override
	public void populateToolBar(IToolBarManager toolBarManager)
	{
		updateActions();
	}

	/** Invoked when visualizer is selected, to populate the toolbar's menu. */
	@Override
	public void populateMenu(IMenuManager menuManager)
	{
		updateActions();
	}

	/** Invoked when visualizer view's context menu is invoked, to populate it. */
	@Override
	public void populateContextMenu(IMenuManager menuManager)
	{
		updateActions();
		Point location = m_viewer.getContextMenuLocation();
		updateContextMenuActions(location);
	}

	
	// --- visualizer selection management ---
	
	/** Invoked when visualizer has been selected. */
	@Override
	public void visualizerSelected() {
		updateActions();
	};
	
	/** Invoked when another visualizer has been selected, hiding this one. */
	@Override
	public void visualizerDeselected() {
	};

	
	// --- workbench selection management ---
	
    /**
     * Tests whether if the IVisualizer can display the selection
     * (or something reachable from it).
	 */
	@Override
	public int handlesSelection(ISelection selection)
	{
		// By default, we don't support anything.
		int result = 0;
		
		Object sel = SelectionUtils.getSelectedObject(selection);
		if (sel instanceof GdbLaunch ||
			sel instanceof GDBProcess ||
			sel instanceof IDMVMContext)
		{
			result = 1;
		}
		else {
			result = 0;
		}
		
		return result;
	}
	
    /**
     * Invoked by VisualizerViewer when workbench selection changes.
     */
	@Override
	public void workbenchSelectionChanged(ISelection selection)
	{
		// See if we need to update our debug info from
		// the workbench selection. This will be done asynchronously.
		boolean changed = updateDebugContext();
		
		// Even if debug info doesn't change, we still want to
		// check whether the canvas selection needs to change
		// to reflect the current workbench selection.
		if (! changed) updateCanvasSelection();
	}
	
	
	// --- ISelectionChangedListener implementation ---

	/**
	 * Invoked when visualizer control's selection changes.
	 * Sets control selection as its own selection,
	 * and raises selection changed event for any listeners.
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		super.selectionChanged(event);
		
		// Force Debug View's selection to reflect visualizer selection,
		// since debug view doesn't update itself from the workbench selection.
		// NOTE: This can be overridden by the model selection policy, if there is one.
		ISelection debugViewSelection = visualizerToDebugViewSelection(getSelection());
		DebugViewUtils.setDebugViewSelection(debugViewSelection);
		
		// update actions to reflect change of selection
		updateActions();
	}
	
	
	// --- Selection conversion methods ---
	
	/** Gets debug view selection from visualizer selection. */
	protected ISelection visualizerToDebugViewSelection(ISelection visualizerSelection)
	{
		VisualizerSelectionFinder selectionFinder =
			new VisualizerSelectionFinder();
		ISelection workbenchSelection =
			selectionFinder.findSelection(visualizerSelection);
		return workbenchSelection;
	}
	
	/** Gets visualizer selection from debug view selection. */
	protected ISelection workbenchToVisualizerSelection(ISelection workbenchSelection)
	{
		ISelection visualizerSelection = null;
		
		List<Object> items = SelectionUtils.getSelectedObjects(workbenchSelection);
		
		// Use the current canvas model to match Debug View items
		// with corresponding threads, if any.
		VisualizerModel model = m_canvas.getModel();
		if (model != null) {
			
			Set<Object> selected = new HashSet<Object>();

			for (Object item : items) {
				
				// Currently, we ignore selections other than DSF context objects.
				// TODO: any other cases where we could map selections to canvas?
				if (item instanceof IDMVMContext)
				{
					IDMContext context = ((IDMVMContext) item).getDMContext();
					
					IMIProcessDMContext processContext =
							DMContexts.getAncestorOfType(context, IMIProcessDMContext.class);
					int pid = Integer.parseInt(processContext.getProcId());
					
					IMIExecutionDMContext execContext =
						DMContexts.getAncestorOfType(context, IMIExecutionDMContext.class);
					int tid = (execContext == null) ? 0 : execContext.getThreadId();
					
					if (tid == 0) { // process
						List<VisualizerThread> threads = model.getThreadsForProcess(pid);
						if (threads != null) {
							selected.addAll(threads);
						}
					}
					else { // thread
						VisualizerThread thread = model.getThread(tid);
						if (thread != null) {
							selected.add(thread);
						}
					}
				}
			}
	
			visualizerSelection = SelectionUtils.toSelection(selected);
		}
		
		return visualizerSelection;
	}
	

	// --- DSF Context Management ---
	
	/** Updates debug context being displayed by canvas.
	 *  Returns true if canvas context actually changes, false if not.
	 */
	public boolean updateDebugContext()
	{
		String sessionId = null;
		IAdaptable debugContext = DebugUITools.getDebugContext();
		if (debugContext instanceof IDMVMContext) {
			sessionId = ((IDMVMContext)debugContext).getDMContext().getSessionId();
		} else if (debugContext instanceof GdbLaunch) {
			GdbLaunch gdbLaunch = (GdbLaunch)debugContext;
			if (gdbLaunch.isTerminated() == false) {
				sessionId = gdbLaunch.getSession().getId();
			}
		} else if (debugContext instanceof GDBProcess) {
			ILaunch launch = ((GDBProcess)debugContext).getLaunch();
			if (launch.isTerminated() == false &&
					launch instanceof GdbLaunch) {
				sessionId = ((GdbLaunch)launch).getSession().getId();
			}
		}
		
		return setDebugSession(sessionId);
	}

	/** Sets debug context being displayed by canvas.
	 *  Returns true if canvas context actually changes, false if not.
	 */
	public boolean setDebugSession(String sessionId) {
		boolean changed = true;

		if (m_sessionState != null &&
			! m_sessionState.getSessionID().equals(sessionId))
		{
			m_sessionState.removeServiceEventListener(fEventListener);
			m_sessionState.dispose();
			m_sessionState = null;
			changed = true;
		}
		
		if (m_sessionState == null &&
			sessionId != null)
		{
			m_sessionState = new DSFSessionState(sessionId);
			m_sessionState.addServiceEventListener(fEventListener);
			changed = true;
		}
	
		if (changed) update();
		
		return changed;
	}


	// --- Update methods ---
	
	/** Updates visualizer canvas state. */
	public void update() {
		// Create new VisualizerModel and hand it to canvas,
		// TODO: cache the VisualizerModel somehow and update it,
		// rather than creating it from scratch each time.
		if (m_sessionState == null) {
			// no state to display, we can immediately clear the canvas
			setCanvasModel(null);
			return;
		}
		m_sessionState.execute(new DsfRunnable() { public void run() {
			// get model asynchronously, and update canvas
			// in getVisualizerModelDone().
			getVisualizerModel();
		}});
	}
	
	/** Sets canvas model. (Also updates canvas selection.) */
	protected void setCanvasModel(VisualizerModel model) {
		final VisualizerModel model_f = model;
		GUIUtils.exec(new Runnable() { public void run() {
			m_canvas.setModel(model_f);
			
			// Update the canvas's selection from the current workbench selection.
			updateCanvasSelectionInternal();
		}});
	}
	
	/** Updates canvas selection from current workbench selection. */
	protected void updateCanvasSelection() {
		GUIUtils.exec(new Runnable() { public void run() {
			// Update the canvas's selection from the current workbench selection.
			updateCanvasSelectionInternal();
		}});
	}
	
	/** Updates canvas selection from current workbench selection.
	 *  Note: this method assumes it is called on the UI thread. */
	protected void updateCanvasSelectionInternal()
	{
		updateCanvasSelectionInternal(SelectionUtils.getWorkbenchSelection());
	}
	
	/** Updates canvas selection from current workbench selection.
	 *  Note: this method assumes it is called on the UI thread. */
	protected void updateCanvasSelectionInternal(ISelection selection)
	{
		ISelection canvasSelection = workbenchToVisualizerSelection(selection);
		
		// canvas does not raise a selection changed event in this case
		// to avoid circular selection update events
		if (canvasSelection != null)
			m_canvas.setSelection(canvasSelection, false);
	}
	
	
	// --- Visualizer model update methods ---
	
	/** Starts visualizer model request.
	 *  Calls getVisualizerModelDone() with the constructed model.
	 */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public void getVisualizerModel() {
		fDataModel = new VisualizerModel();
		DSFDebugModel.getCPUs(m_sessionState, this, fDataModel);
	}
	
	/** Invoked when getModel() request completes. */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public void getVisualizerModelDone(VisualizerModel model) {
		model.sort();
		setCanvasModel(model);
	}
	
	
	// --- DSFDebugModelListener implementation ---

	/** Invoked when DSFDebugModel.getCPUs() completes. */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public void getCPUsDone(ICPUDMContext[] cpuContexts, Object arg)
	{
		VisualizerModel model = (VisualizerModel) arg;
		
		if (cpuContexts == null || cpuContexts.length == 0) {
			// Whoops, no CPU data.
			// We'll fake a CPU and use it to contain any cores we find.
			
			model.addCPU(new VisualizerCPU(0));
			
			// keep track of CPUs left to visit
			model.getTodo().add(1);
			
			// Collect core data.
			DSFDebugModel.getCores(m_sessionState, this, model);
		} else {
			// keep track of CPUs left to visit
			int count = cpuContexts.length;
			model.getTodo().add(count);
			
			for (ICPUDMContext cpuContext : cpuContexts) {
				int cpuID = Integer.parseInt(cpuContext.getId());
				model.addCPU(new VisualizerCPU(cpuID));
				
				// Collect core data.
				DSFDebugModel.getCores(m_sessionState, cpuContext, this, model);
			}
			
		}
	}

	
	/** Invoked when getCores() request completes. */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public void getCoresDone(ICPUDMContext cpuContext,
							 ICoreDMContext[] coreContexts,
							 Object arg)
	{
		VisualizerModel model = (VisualizerModel) arg;

		if (coreContexts == null || coreContexts.length == 0) {
			// no cores for this cpu context
			// That's fine.
		} else {
			int cpuID = Integer.parseInt(cpuContext.getId());
			VisualizerCPU cpu = model.getCPU(cpuID);

			// keep track of Cores left to visit
			int count = coreContexts.length;
			model.getTodo().add(count);
			
			for (ICoreDMContext coreContext : coreContexts) {
				int coreID = Integer.parseInt(coreContext.getId());
				cpu.addCore(new VisualizerCore(cpu, coreID));
				
				// Collect thread data
				DSFDebugModel.getThreads(m_sessionState, cpuContext, coreContext, this, model);
			}			
		}
		
		// keep track of CPUs visited
		// note: do this _after_ incrementing for cores
		done(1, model);
	}

	
	/** Invoked when getThreads() request completes. */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public void getThreadsDone(ICPUDMContext  cpuContext,
							   ICoreDMContext coreContext,
							   IDMContext[] threadContexts,
							   Object arg)
	{
		VisualizerModel model = (VisualizerModel) arg;
		
		if (threadContexts == null || threadContexts.length == 0) {
			// no threads for this core
			// That's fine.
		} else {
			// keep track of threads left to visit
			int count = threadContexts.length;
			model.getTodo().add(count);

			for (IDMContext threadContext : threadContexts) {
				IMIExecutionDMContext execContext =
					DMContexts.getAncestorOfType(threadContext, IMIExecutionDMContext.class);

				// Don't add the thread to the model just yet, let's wait until we have its execution state.
				// Collect thread execution state
				DSFDebugModel.getThreadExecutionState(m_sessionState, cpuContext, coreContext, execContext, this, model);
			}
			
		}
		
		// keep track of cores visited
		// note: do this _after_ incrementing for threads
		done(1, model);
	}
	
	/** Invoked when getThreads() request completes. */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public void getThreadExecutionStateDone(ICPUDMContext cpuContext,
			                                ICoreDMContext coreContext,
			                                IMIExecutionDMContext execContext,
			                                VisualizerExecutionState state,
			                                Object arg)
	{
		VisualizerModel model = (VisualizerModel) arg;
		int cpuID  = Integer.parseInt(cpuContext.getId());
		VisualizerCPU  cpu  = model.getCPU(cpuID);
		int coreID = Integer.parseInt(coreContext.getId());
		VisualizerCore core = cpu.getCore(coreID);
		
		if (state == null) {
			// Unable to obtain execution state.  Assume running
			state = VisualizerExecutionState.RUNNING;
		}

		IMIProcessDMContext processContext =
				DMContexts.getAncestorOfType(execContext, IMIProcessDMContext.class);
		int pid = Integer.parseInt(processContext.getProcId());
		int tid = execContext.getThreadId();

		model.addThread(new VisualizerThread(core, pid, tid, state));
		
		// keep track of threads visited
		done(1, model);
	}
	
	/** Update "done" count for current visualizer model. */
	protected void done(int n, VisualizerModel model) {
		model.getTodo().done(n);
		if (model.getTodo().isDone()) {
			getVisualizerModelDone(model);
		}
	}
}

