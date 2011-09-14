/*******************************************************************************
 * Copyright (c) 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses.IGdbThreadDMData;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.visualizer.ui.canvas.GraphicCanvas;
import org.eclipse.cdt.visualizer.ui.canvas.GraphicCanvasVisualizer;
import org.eclipse.cdt.visualizer.ui.test.TestCanvas;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings({"restriction","nls"})
public class MulticoreVisualizer extends GraphicCanvasVisualizer {


	// --- constants ---
	
	/** Eclipse ID for this visualizer */
	public static final String ECLIPSE_ID = "org.eclipse.cdt.dsf.gdb.multicorevisualizer.ui.Visualizer";

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private String fDebugSessionId;
	private DsfServicesTracker fServicesTracker;
	private volatile ICommandControlDMContext fTargetContext;

	// --- members ---
	
	/** Visualizer control. */
	TestCanvas m_canvas = null;
	

	// --- constructors/destructors ---
	
	public MulticoreVisualizer() {
	}

	// --- accessors ---
	
	/** Gets canvas control. */
	public TestCanvas getDefaultCanvas()
	{
		return m_canvas;
	}

	
	// --- IVisualizer implementation ---
	
	/** Returns non-localized unique name for this visualizer. */
	@Override
	public String getName() {
		return "default";
	}
	
	/** Returns localized name to display for this visualizer. */
	@Override
	public String getDisplayName() {
		// TODO: use a string resource here.
		return "Multicore Visualizer";
	}
	
	/** Returns localized tooltip text to display for this visualizer. */
	@Override
	public String getDescription() {
		// TODO: use a string resource here.
		return "Multicore visualizer.";
	}
	
	/** Creates and returns visualizer canvas control. */
	@Override
	public GraphicCanvas createCanvas(Composite parent)
	{
		m_canvas = new TestCanvas(parent);
		return m_canvas;
	}
	
	/** Invoked after visualizer control creation, */
	@Override
	protected void initializeCanvas(GraphicCanvas canvas)
	{
		m_canvas.setBackground(canvas.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		m_canvas.setForeground(canvas.getDisplay().getSystemColor(SWT.COLOR_BLACK));
	}


	// --- workbench selection management ---

    /**
     * Tests whether if the IVisualizer can display the selection
     * (or something reachable from it).
     */
	@Override
	public int handlesSelection(ISelection selection)
	{
		// By default, we don't support anything.
		// Changing this to return 1 enables the test canvas.
		return 1;
	}

    /**
     * Invoked by VisualizerViewer when workbench selection changes.
     */
	@Override
	public void workbenchSelectionChanged(ISelection selection)
	{
		updateDebugContext();
	}
	
	
	protected void updateContent() {			
		if (fDebugSessionId != null && getSession() != null) {
			final ICommandControlDMContext ctx = DMContexts.getAncestorOfType(fTargetContext, ICommandControlDMContext.class);
			if (ctx != null) {
				getSession().getExecutor().execute(
						new DsfRunnable() {	
							public void run() {
								final IProcesses procService = getService(IProcesses.class);
								if (procService != null) {
									procService.getRunningProcesses(
											ctx, new DataRequestMonitor<IProcessDMContext[]>(ImmediateExecutor.getInstance(), null) {
												@Override
												protected void handleCompleted() {
													final StringBuffer text = new StringBuffer();
													
				    								if (getData().length > 0 && getData()[0] instanceof IThreadDMData) {
				    									// The list of running processes also contains the name of the processes
				    									// This is much more efficient.  Let's use it.
														for (IProcessDMContext processCtx : getData()) {

															IThreadDMData processData = (IThreadDMData) processCtx;

															int pid = 0;
															try {
																pid = Integer.parseInt(processData.getId());
															} catch (NumberFormatException e) {
															}
															
															String[] cores = null;
															String owner = null;
															if (processData instanceof IGdbThreadDMData) {
																cores = ((IGdbThreadDMData)processData).getCores();
																owner = ((IGdbThreadDMData)processData).getOwner();
															}
															
															text.append(processData.getName());
															
															if (owner != null) {
																text.append(" (" + owner + ")");  //$NON-NLS-1$//$NON-NLS-2$
															}
															
															text.append(" - " + pid); //$NON-NLS-1$

															if (cores != null && cores.length > 0) {
																String coreStr;
																if (cores.length == 1) {
																	coreStr = "core";
																} else {
																	coreStr = "cores";
																}
																text.append(" [" + coreStr + ": ");   //$NON-NLS-1$//$NON-NLS-2$
																
																for (String core : cores) {
																	text.append(core + ", "); //$NON-NLS-1$
																}
																// Remove the last comma and space
																text.replace(text.length()-2, text.length(), "]"); //$NON-NLS-1$
															}
															
															text.append("\n");
														}
				    								}
				    								
													asyncExec(new Runnable() {
														public void run() {
															m_canvas.setText(text.toString());
															m_canvas.redraw();
														}});
												}
											});
								} else {

									asyncExec(new Runnable() {
										public void run() {
											m_canvas.setText(EMPTY_STRING);
											m_canvas.redraw();
										}});
								}

							}
						});
				return;
			}
		}
		
		if (m_canvas != null) {
			m_canvas.setText(EMPTY_STRING);
			m_canvas.redraw();
		}
	}
	
	protected void updateDebugContext() {
		IAdaptable debugContext = DebugUITools.getDebugContext();
		if (debugContext instanceof IDMVMContext) {
			setDebugContext((IDMVMContext)debugContext);
		} else {
			setDebugContext(null);
		}
	}

	protected void setDebugContext(IDMVMContext vmContext) {
		if (vmContext != null) {
			IDMContext dmContext = vmContext.getDMContext();
			String sessionId = dmContext.getSessionId();
			fTargetContext = DMContexts.getAncestorOfType(dmContext, ICommandControlDMContext.class);
			if (!sessionId.equals(fDebugSessionId)) {
				if (fDebugSessionId != null && getSession() != null) {
					try {
						final DsfSession session = getSession();
						session.getExecutor().execute(new DsfRunnable() {
							public void run() {
								session.removeServiceEventListener(MulticoreVisualizer.this);
							}
						});
					} catch (RejectedExecutionException e) {
						// Session is shut down.
					}
				}
				fDebugSessionId = sessionId;
				if (fServicesTracker != null) {
					fServicesTracker.dispose();
				}
				fServicesTracker = new DsfServicesTracker(MulticoreVisualizerUIPlugin.getBundleContext(), sessionId);
				debugSessionChanged();
			}
		} else if (fDebugSessionId != null) {
			if (getSession() != null) {
				try {
					final DsfSession session = getSession();
					session.getExecutor().execute(new DsfRunnable() {
						public void run() {
							session.removeServiceEventListener(MulticoreVisualizer.this);
						}
					});
        		} catch (RejectedExecutionException e) {
                    // Session is shut down.
        		}
			}
			fDebugSessionId = null;
			fTargetContext = null;
			if (fServicesTracker != null) {
				fServicesTracker.dispose();				
				fServicesTracker = null;
			}
			debugSessionChanged();
		}
	}

	private void debugSessionChanged() {
		if (fDebugSessionId != null && getSession() != null) {
			try {
				final DsfSession session = getSession();
				session.getExecutor().execute(new DsfRunnable() {
					public void run() {
						session.addServiceEventListener(MulticoreVisualizer.this, null);
					}
				});
    		} catch (RejectedExecutionException e) {
                // Session is shut down.
    		}
        }
		
		updateContent();
	}

	
	private void asyncExec(Runnable runnable) {
		if (m_canvas != null) {
			m_canvas.getDisplay().asyncExec(runnable);
		}
	}

	public void sessionEnded(DsfSession session) {
		if (session.getId().equals(fDebugSessionId)) {
			asyncExec(new Runnable() {
				public void run() {
					setDebugContext(null);
				}});
		}
	}

	/*
	 * Since something suspended, might as well refresh our status
	 * to show the latest.
	 */
	@DsfServiceEventHandler
	public void handleEvent(ISuspendedDMEvent event) {
		updateContent();
	}

	private DsfSession getSession() {
		return DsfSession.getSession(fDebugSessionId);
	}
	
	private <V> V getService(Class<V> serviceClass) {
		if (fServicesTracker != null) {
			return fServicesTracker.getService(serviceClass);
		}
		return null;
	}
}
