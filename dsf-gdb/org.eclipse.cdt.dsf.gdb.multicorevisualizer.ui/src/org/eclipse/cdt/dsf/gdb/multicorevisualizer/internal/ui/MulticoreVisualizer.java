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

import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardware;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardware.ICPUDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardware.ICoreDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardware.IHardwareTargetDMContext;
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


	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private String fDebugSessionId;
	private DsfServicesTracker fServicesTracker;
	private volatile IHardwareTargetDMContext fTargetContext;

	/** Visualizer control. */
	private TestCanvas fCanvas = null;
	
	public MulticoreVisualizer() {
	}

	// --- accessors ---
	
	/** Gets canvas control. */
	public TestCanvas getDefaultCanvas() {
		return fCanvas;
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
	public GraphicCanvas createCanvas(Composite parent) {
		fCanvas = new TestCanvas(parent);
		return fCanvas;
	}
	
	/** Invoked after visualizer control creation, */
	@Override
	protected void initializeCanvas(GraphicCanvas canvas) {
		fCanvas.setBackground(canvas.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		fCanvas.setForeground(canvas.getDisplay().getSystemColor(SWT.COLOR_BLACK));
	}


	// --- workbench selection management ---

    /**
     * Tests whether if the IVisualizer can display the selection
     * (or something reachable from it).
     */
	@Override
	public int handlesSelection(ISelection selection) {
		// By default, we don't support anything.
		// Changing this to return 1 enables the test canvas.
		return 1;
	}

    /**
     * Invoked by VisualizerViewer when workbench selection changes.
     */
	@Override
	public void workbenchSelectionChanged(ISelection selection) {
		updateDebugContext();
	}
	
	
	protected void updateContent() {			
		if (fDebugSessionId != null && getSession() != null) {
			getSession().getExecutor().execute(
					new DsfRunnable() {	
						public void run() {
							final IGDBHardware hwService = getService(IGDBHardware.class);
							if (hwService != null) {
								final StringBuffer text = new StringBuffer();
								
								final CountingRequestMonitor crm = new CountingRequestMonitor(ImmediateExecutor.getInstance(), null) {
									@Override
									protected void handleCompleted() {
										asyncExec(new Runnable() {
											public void run() {
												fCanvas.setText(text.toString());
												fCanvas.redraw();
											}});
									}
								};
								
								// First get the CPUs
								hwService.getCPUs(fTargetContext, 
									new DataRequestMonitor<ICPUDMContext[]>(ImmediateExecutor.getInstance(), null) {
										@Override
										protected void handleCompleted() {
											ICPUDMContext[] cpus = getData();
											
											if (!isSuccess() || cpus == null || cpus.length < 1) {
												// Unable to get CPUs.  Let's fetch the cores directly.
												hwService.getCores(fTargetContext, 
														new DataRequestMonitor<ICoreDMContext[]>(ImmediateExecutor.getInstance(), null) {
															@Override
															protected void handleCompleted() {
																ICoreDMContext[] cores = getData();
																
																if (!isSuccess() || cores == null || cores.length < 1) {
																	text.append("Unable to fetch information about the cores of the target\n");
																} else {
																	text.append("The target has a total of " + cores.length + (cores.length == 1 ? "core\n" : "cores\n"));
																	for (ICoreDMContext core : cores) {
																		text.append("\tcore: " + core.getId() + "\n");
																	}
																}																
																crm.done();
															}
														});
	    										return;
											}

											// For each CPU, look for its cores
											crm.setDoneCount(cpus.length);

											text.append("The target has a total of " + cpus.length + (cpus.length == 1 ? "CPU\n" : "CPUs\n"));

											for (final ICPUDMContext cpu : cpus) {
												text.append("CPU " + cpu.getId());
												
												hwService.getCores(cpu, 
														new DataRequestMonitor<ICoreDMContext[]>(ImmediateExecutor.getInstance(), null) {
															@Override
															protected void handleCompleted() {
																ICoreDMContext[] cores = getData();
																
																if (!isSuccess() || cores == null || cores.length < 1) {
																	text.append("\n\tUnable to fetch information about the cores of cpu" + cpu.getId() + "\n");
																} else {
																	text.append(" has a total of " + cores.length + (cores.length == 1 ? "core:\n" : "cores:\n"));
																	for (ICoreDMContext core : cores) {
																		text.append("\tcore: " + core.getId() + "\n");
																	}
																}																
																crm.done();
															}
														});
											}
										}
									});
							}}});
		} else {
			
//			final ICommandControlDMContext ctx = DMContexts.getAncestorOfType(fTargetContext, ICommandControlDMContext.class);
//			if (ctx != null) {
//				getSession().getExecutor().execute(
//						new DsfRunnable() {	
//							public void run() {
//								final IProcesses procService = getService(IProcesses.class);
//								if (procService != null) {
//									final StringBuffer text = new StringBuffer();
//									
//									final CountingRequestMonitor crm = new CountingRequestMonitor(ImmediateExecutor.getInstance(), null) {
//										@Override
//										protected void handleCompleted() {
//    										asyncExec(new Runnable() {
//    											public void run() {
//    												fCanvas.setText(text.toString());
//    												fCanvas.redraw();
//    											}});
//										}
//									};
//									
//									procService.getProcessesBeingDebugged(
//											ctx, new DataRequestMonitor<IDMContext[]>(ImmediateExecutor.getInstance(), null) {
//												@Override
//												protected void handleCompleted() {
//													IDMContext[] procs = getData();
//													
//													if (!isSuccess() || procs == null || procs.length < 1) {
//			    										text.append("No processes being debugged\n");
//			    										crm.setDoneCount(0);
//			    										return;
//													}
//													
//													// For each process, look for its threads
//													crm.setDoneCount(procs.length);
//													
//													for (IDMContext dmc : procs) {
//														IContainerDMContext container = (IContainerDMContext)dmc;
//														text.append(container.toString()+"\n");
//
//														procService.getProcessesBeingDebugged(
//																container,
//																new DataRequestMonitor<IDMContext[]>(ImmediateExecutor.getInstance(), crm) {
//																	@Override
//																	protected void handleCompleted() {
//																		IDMContext[] threads = getData();
//
//																		if (!isSuccess() || threads == null || threads.length < 1) {
//																			text.append("No threads for this process\n");
//																			crm.done();
//																			return;
//																		}
//
//																		for (IDMContext thread : threads) {
//																			IExecutionDMContext execDmc = (IExecutionDMContext)thread;
//																			text.append("\t"+execDmc.toString()+"\n");
//																		}
//																		
//																		crm.done();
//																	}
//																});
//													}
//												}
//											});
//								}}});
//				return;
//			}
		
			if (fCanvas != null) {
				fCanvas.setText(EMPTY_STRING);
				fCanvas.redraw();
			}
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
			fTargetContext = DMContexts.getAncestorOfType(dmContext, IHardwareTargetDMContext.class);
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
		if (fCanvas != null) {
			fCanvas.getDisplay().asyncExec(runnable);
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
