// ===========================================================================
// MulticoreVisualizer.java -- Grid View Visualizer
// ===========================================================================
// Copyright (C) 2011, Tilera Corporation. All rights reserved.
// Use is subject to license terms.
// ===========================================================================

// NOTE: When we contribute this to CDT, delete above header and use this one:
/*******************************************************************************
 * Copyright (c) 2011 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation)
 *******************************************************************************/

// package declaration
package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view;

//Java API classes
//import java.util.*;

// SWT/JFace classes
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;

//Eclipse/CDT classes
import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.launching.GDBProcess;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerCPU;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerCore;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerModel;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerThread;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.DSFSessionState;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.OnNthDoneMonitor;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.OnDataRequestDoneMonitor;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils.OnDoneMonitor;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardware;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardware.ICPUDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardware.ICoreDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardware.IHardwareTargetDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses.IGdbThreadDMData;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcessDMContext;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.visualizer.ui.canvas.GraphicCanvas;
import org.eclipse.cdt.visualizer.ui.canvas.GraphicCanvasVisualizer;
import org.eclipse.cdt.visualizer.ui.plugin.CDTVisualizerUIPlugin;
import org.eclipse.cdt.visualizer.ui.util.Colors;
import org.eclipse.cdt.visualizer.ui.util.GUIUtils;
import org.eclipse.cdt.visualizer.ui.util.SelectionUtils;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.DebugUITools;

// custom classes


// ---------------------------------------------------------------------------
// GridViewer
// ---------------------------------------------------------------------------

/**
 * The Grid View Visualizer, the Tilera IDE's graphic representation
 * of the state of the Tilera chip.
 * 
 * This view uses the CDT Visualizer framework.
 */
@SuppressWarnings("restriction")
public class MulticoreVisualizer extends GraphicCanvasVisualizer
{	
	// --- constants ---
	
	/** Eclipse ID for this view */
	public static final String ECLIPSE_ID = "org.eclipse.cdt.dsf.gdb.multicorevisualizer.visualizer2"; //$NON-NLS-1$

	
	// --- members ---
	
	/** Downcast reference to canvas. */
	protected MulticoreVisualizerCanvas m_canvas = null;
	
	/** DSF debug context session object. */
	protected DSFSessionState m_sessionState = null;
	

	// --- constructors/destructors ---
	
	/** Constructor. */
	public MulticoreVisualizer()
	{
		super();
		
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
	public void initializeVisualizer()
	{
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
	
	
	// --- action management ---

	/** Creates actions for menus/toolbar. */
	protected void createActions() {

	}
	
	/** Updates actions displayed on menu/toolbars. */
	protected void updateActions() {
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
		
		// HACK: for now, we handle anything
		result = 2;
		
		return result;
	}
	
    /**
     * Invoked by VisualizerViewer when workbench selection changes.
     */
	@Override
	public void workbenchSelectionChanged(ISelection selection)
	{
		updateDebugContext();
	}
	
	/**
	 * Updates visualizer selection state from canvas.
	 * Intended to be invoked when workbench selection changes
	 * and we programmatically set the canvas selection,
	 * since canvas will not raise a selection changed event in this case.
	 */
	protected void updateSelection() {
		setSelection(m_canvas.getSelection(), false);
	}
	
	
	// --- ISelectionChangedListener implementation ---

	/**
	 * Intended to be invoked when visualizer control's selection changes.
	 * Sets control selection as its own selection,
	 * and raises selection changed event for any listeners.
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		super.selectionChanged(event);
		
		// update actions to reflect change of selection
		updateActions();
	}
	
	
	// --- DSF Context Management ---
	
	/** Updates debug context being displayed by canvas. */
	public void updateDebugContext()
	{
		IAdaptable debugContext = DebugUITools.getDebugContext();
		if (debugContext instanceof IDMVMContext) {
			setDebugContext((IDMVMContext)debugContext);
		}
	}

	/** Sets debug context being displayed by canvas. */
	public void setDebugContext(IDMVMContext vmContext) {
		boolean changed = false;
		if (m_sessionState != null) {
			m_sessionState.dispose();
			m_sessionState = null;
			changed = true;
		}
		if (vmContext != null) {
			m_sessionState = new DSFSessionState(vmContext);
			m_sessionState.addServiceEventListener(this);
			changed = true;
		}
		if (changed) update();
	}

	
	// --- DSF Event Handlers ---

	/** Invoked when current debug session is suspended. */
	@DsfServiceEventHandler
	public void handleEvent(ISuspendedDMEvent event) {
		// Update to show latest state.
		update();
	}
	
	
	// --- Update methods ---
	
	/** Sets canvas model. */
	protected void setCanvasModel(VisualizerModel model) {
		final VisualizerModel model_f = model;
		GUIUtils.exec(new Runnable() { public void run() {
			m_canvas.setModel(model_f);
		}});
	}
	
	/** Updates visualizer canvas state. */
	public void update() {
		if (m_sessionState == null) {
			setCanvasModel(null);
			return;
		}

		// Create new VisualizerModel and hand it to canvas,
		// TODO: cache the VisualizerModel and update it,
		// rather than creating it from scratch each time.
		final VisualizerModel model_f = new VisualizerModel();
		m_sessionState.execute(new DsfRunnable() { public void run() {
			populateVisualizerModelCPUs(
				m_sessionState, m_sessionState.getHardwareContext(), model_f, new OnDoneMonitor() {
					@Override
					protected void handleCompleted() {
						model_f.sort();
						setCanvasModel(model_f);
					}
				}
			);
		}});
	}
	
	/** Populates CPU layer of visualizer model object using DSF services. */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	protected void populateVisualizerModelCPUs(
		DSFSessionState sessionState, IHardwareTargetDMContext hwContext, VisualizerModel model, RequestMonitor rm)
	{
		IGDBHardware hwService = sessionState.getHardwareService();
		if (hwService == null) {
			rm.done();
			return;
		}

		final DSFSessionState sessionState_f = sessionState;
		final VisualizerModel model_f = model;
		final RequestMonitor  rm_f = rm;
		
		hwService.getCPUs(hwContext,
			new OnDataRequestDoneMonitor<ICPUDMContext[]>(rm) {
				@Override
				protected void handleCompleted(ICPUDMContext[] cpuContexts) {
					if (!isSuccess() || cpuContexts == null || cpuContexts.length < 1) {
						// Unable to get CPU data.
						// We'll create a single "fake" CPU entry that holds all the cores we find.
						VisualizerCPU cpu = model_f.addCPU(new VisualizerCPU(0));
						// Use hardware target as "context" for this fake CPU
						IHardwareTargetDMContext hwContext = sessionState_f.getHardwareContext();
						populateVisualizerModelCores(sessionState_f, model_f, hwContext, cpu, rm_f);
						rm_f.done();
						return;
					}
					
					// For each CPU, look for its cores.
					int count = cpuContexts.length;
					OnNthDoneMonitor nthrm = new OnNthDoneMonitor(count, rm_f);
					
					for (int i=0; i<count; ++i) {
						ICPUDMContext cpuContext = cpuContexts[i];
						int id = Integer.parseInt(cpuContext.getId());
						VisualizerCPU cpu = model_f.addCPU(new VisualizerCPU(id));
						populateVisualizerModelCores(sessionState_f, model_f, cpuContext, cpu, nthrm);
					}
				}
			}
		);
	}
	
	/** Populates CPU layer of visualizer model object using DSF services. */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	protected void populateVisualizerModelCores(
		DSFSessionState sessionState, VisualizerModel model, IDMContext cpuContext, VisualizerCPU cpu, RequestMonitor rm)
	{
		IGDBHardware hwService = sessionState.getHardwareService();
		if (hwService == null) {
			rm.done();
			return;
		}
		
		final DSFSessionState sessionState_f = sessionState;
		final VisualizerModel model_f = model;
		final VisualizerCPU   cpu_f = cpu;
		final RequestMonitor  rm_f = rm;
		
		hwService.getCores(cpuContext,
			new OnDataRequestDoneMonitor<ICoreDMContext[]>(rm) {
				@Override
				protected void handleCompleted(ICoreDMContext[] coreContexts) {
					if (!isSuccess() || coreContexts == null || coreContexts.length < 1) {
						// Unable to get any core data for this cpu.
						// TODO: log this?
						rm_f.done();
						return;
					}
					
					// For each core, look for its processes/threads.
					int count = coreContexts.length;
					OnNthDoneMonitor nthrm = new OnNthDoneMonitor(count, rm_f);
					
					// For each Core, look for its threads.
					for (ICoreDMContext coreContext : coreContexts) {
						int id = Integer.parseInt(coreContext.getId());
						VisualizerCore core = cpu_f.addCore(new VisualizerCore(id));
						populateVisualizerProcessThreadInfo(sessionState_f, model_f, coreContext, core, nthrm);
					}
				}
			}
		);
	}
	

	//   DSF process/thread/execution hierarchy:
	//
	//            MIControlDMContext (ICommandControlDMContext)
	//                    |
	//            MIProcessDMContext (IProcess)
	//              /           \
	//             /             \
    //   MIContainerDMContext   MIThreadDMC (IThread)
	//      (IContainer)         /
	//             \            /
    //           MIExecutionDMContext (IExecution)
	//                    |
	//             MIStackDMContext / MIExpressionDMContext
	
	/** Populates CPU layer of visualizer model object using DSF services. */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	protected void populateVisualizerProcessThreadInfo(
		DSFSessionState sessionState, VisualizerModel model, ICoreDMContext coreContext, VisualizerCore core, RequestMonitor rm)
	{
		// Get control DM context associated with the core
		// Process/Thread Info service (GDBProcesses_X_Y_Z)
		IProcesses procService = sessionState.getService(IProcesses.class);
		// Debugger control context (GDBControlDMContext)
		ICommandControlDMContext controlContext =
				DMContexts.getAncestorOfType(coreContext, ICommandControlDMContext.class);
		if (procService == null || controlContext == null) {
			rm.done();
			return;
		}

		final ICoreDMContext coreContext_f = coreContext;
		final VisualizerModel model_f = model;
		final IProcesses procService_f = procService;
		final VisualizerCore core_f = core;
		final RequestMonitor rm_f = rm;

		// Get debugged processes
		procService_f.getProcessesBeingDebugged(controlContext,
			new OnDataRequestDoneMonitor<IDMContext[]>(rm) {
		
				@Override
				protected void handleCompleted(IDMContext[] processContexts) {
					if (!isSuccess() || processContexts == null || processContexts.length < 1) {
						// Unable to get any process data for this core
						// Is this an issue? A core may have no processes/threads, right?
						rm_f.done();
						return;
					}
					
					int count = processContexts.length;
					final OnNthDoneMonitor nthrm_f = new OnNthDoneMonitor(count, rm_f);
					
					for (IDMContext processContext : processContexts) {
						IContainerDMContext containerContext =
							DMContexts.getAncestorOfType(processContext, IContainerDMContext.class);
						
						procService_f.getProcessesBeingDebugged(containerContext,
							new OnDataRequestDoneMonitor<IDMContext[]>(nthrm_f) {

								@Override
								protected void handleCompleted(IDMContext[] threadContexts) {
									if (!isSuccess() || threadContexts == null || threadContexts.length < 1) {
										nthrm_f.done();
										return;
									}
									
									int count = threadContexts.length;
									final OnNthDoneMonitor nthrm2_f = new OnNthDoneMonitor(count, nthrm_f);
									
									for (IDMContext threadContext : threadContexts) {
										final IMIExecutionDMContext execContext =
												DMContexts.getAncestorOfType(threadContext, IMIExecutionDMContext.class);
										IThreadDMContext threadContext2 =
												DMContexts.getAncestorOfType(threadContext, IThreadDMContext.class);

										procService_f.getExecutionData(threadContext2, 
											new OnDataRequestDoneMonitor<IThreadDMData>(nthrm2_f) {
											
												@Override
												protected void handleCompleted(IThreadDMData data) {
													// Check whether we know about cores
													if (data instanceof IGdbThreadDMData) {
														String[] cores = ((IGdbThreadDMData)data).getCores();
														if (cores != null && cores.length == 1) {
															if (coreContext_f.getId().equals(cores[0])) {
																// This thread belongs to the proper core
																
																IMIProcessDMContext processContext =
																	DMContexts.getAncestorOfType(execContext, IMIProcessDMContext.class);
																int pid = Integer.parseInt(processContext.getProcId());
																int tid = execContext.getThreadId();
																model_f.addThread(new VisualizerThread(core_f, pid, tid));
															}
														}
													}
													nthrm_f.done();
												}
											}
										);
									}
								}
							}
						);
					}
				}
			}
		);
	}
}

			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			