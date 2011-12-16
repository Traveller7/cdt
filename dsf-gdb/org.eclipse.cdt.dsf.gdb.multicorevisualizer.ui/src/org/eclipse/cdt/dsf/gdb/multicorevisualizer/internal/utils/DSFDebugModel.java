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

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils;

import java.util.ArrayList;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateCountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMData;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMData2;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerExecutionState;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardware;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardware.ICPUDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardware.ICoreDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardware.IHardwareTargetDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses.IGdbThreadDMData;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;


/** Debugger state information accessors. */
public class DSFDebugModel {
	
	/** Requests list of CPUs.
	 *  Calls back to getCPUsDone() on listener. */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public static void getCPUs(DSFSessionState sessionState,
							   DSFDebugModelListener listener,
							   Object arg)
	{
		final DSFDebugModelListener listener_f = listener;
		final Object arg_f                     = arg;
		
		ICommandControlService controlService = sessionState.getService(ICommandControlService.class);
		IGDBHardware hwService = sessionState.getService(IGDBHardware.class);
		if (controlService == null || hwService == null) {
			listener_f.getCPUsDone(null, arg_f);
			return;
		}
		
		IHardwareTargetDMContext contextToUse = DMContexts.getAncestorOfType(controlService.getContext(),
                                                                             IHardwareTargetDMContext.class);
		hwService.getCPUs(contextToUse,
			new ImmediateDataRequestMonitor<ICPUDMContext[]>() {
				@Override
				protected void handleCompleted() {
					ICPUDMContext[] cpuContexts = getData();
					if (! isSuccess()) cpuContexts = null;
					listener_f.getCPUsDone(cpuContexts, arg_f);
				}
			}
		);
	}
	
	/** Requests list of Cores.
	 *  Calls back to getCoresDone() on listener. */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public static void getCores(DSFSessionState sessionState,
							    DSFDebugModelListener listener,
							    Object arg)
	{
		getCores(sessionState, null, listener, arg);
	}

	/** Requests list of Cores.
	 *  Calls back to getCoresDone() on listener. */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public static void getCores(DSFSessionState sessionState,
								ICPUDMContext cpuContext,
							    DSFDebugModelListener listener,
							    Object arg)
	{
		final ICPUDMContext cpuContext_f       = cpuContext;
		final DSFDebugModelListener listener_f = listener;
		final Object arg_f                     = arg;
		
		IGDBHardware hwService = sessionState.getService(IGDBHardware.class);
		if (hwService == null) {
			listener_f.getCoresDone(cpuContext, null, arg_f);
			return;
		}

		IDMContext contextToUse = cpuContext;
		if (contextToUse == null) {
			// if caller doesn't supply a specific cpu context,
			// use the hardware context (so we get all available cores?)
			ICommandControlService controlService = sessionState.getService(ICommandControlService.class);
			contextToUse = DMContexts.getAncestorOfType(controlService.getContext(),
                                                        IHardwareTargetDMContext.class);

		}
		
		hwService.getCores(contextToUse,
			new ImmediateDataRequestMonitor<ICoreDMContext[]>() {
				@Override
				protected void handleCompleted() {
					ICoreDMContext[] coreContexts = getData();

					if (! isSuccess()) coreContexts = null;
					listener_f.getCoresDone(cpuContext_f, coreContexts, arg_f);
				}
			}
		);
	}
	
	/** Requests list of Threads.
	 *  Calls back to getThreadsDone() on listener. */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public static void getThreads(DSFSessionState sessionState,
								  ICPUDMContext cpuContext,
								  ICoreDMContext coreContext,
							      DSFDebugModelListener listener,
							      Object arg)
	{
		// Get control DM context associated with the core
		// Process/Thread Info service (GDBProcesses_X_Y_Z)
		IProcesses procService = sessionState.getService(IProcesses.class);
		// Debugger control context (GDBControlDMContext)
		ICommandControlDMContext controlContext =
				DMContexts.getAncestorOfType(coreContext, ICommandControlDMContext.class);
		if (procService == null || controlContext == null) {
			listener.getThreadsDone(cpuContext, coreContext, null, arg);
			return;
		}

		final ICPUDMContext  cpuContext_f      = cpuContext;
		final ICoreDMContext coreContext_f     = coreContext;
		final IProcesses procService_f         = procService;
		final DSFDebugModelListener listener_f = listener;
		final Object arg_f                     = arg;
		
		// Get debugged processes
		procService_f.getProcessesBeingDebugged(controlContext,
			new ImmediateDataRequestMonitor<IDMContext[]>() {
		
				@Override
				protected void handleCompleted() {
					IDMContext[] processContexts = getData();
				
					if (!isSuccess() || processContexts == null || processContexts.length < 1) {
						// Unable to get any process data for this core
						// Is this an issue? A core may have no processes/threads, right?
						listener_f.getThreadsDone(cpuContext_f, coreContext_f, null, arg_f);
						return;
					}
					
					final ArrayList<IDMContext> threadContexts_f =
							new ArrayList<IDMContext>();
				
					final ImmediateCountingRequestMonitor nthrm_f = new ImmediateCountingRequestMonitor(
						new ImmediateRequestMonitor() {
							@Override
							protected void handleCompleted() {
								IDMContext[] threadContexts = threadContexts_f.toArray(new IDMContext[threadContexts_f.size()]);
								listener_f.getThreadsDone(cpuContext_f, coreContext_f, threadContexts, arg_f);
							}
						});
					nthrm_f.setDoneCount(processContexts.length);
					
					for (IDMContext processContext : processContexts) {
						IContainerDMContext containerContext =
							DMContexts.getAncestorOfType(processContext, IContainerDMContext.class);
						
						procService_f.getProcessesBeingDebugged(containerContext,
							new ImmediateDataRequestMonitor<IDMContext[]>(nthrm_f) {

								@Override
								protected void handleCompleted() {
									IDMContext[] threadContexts = getData();
									
									if (!isSuccess() || threadContexts == null || threadContexts.length < 1) {
										nthrm_f.done();
										return;
									}
									
									final ImmediateCountingRequestMonitor nthrm2_f = new ImmediateCountingRequestMonitor(nthrm_f);
									nthrm2_f.setDoneCount(threadContexts.length);
									
									for (IDMContext threadContext : threadContexts) {
										final IDMContext threadContext_f = threadContext;
										IThreadDMContext threadContext2 =
												DMContexts.getAncestorOfType(threadContext, IThreadDMContext.class);

										procService_f.getExecutionData(threadContext2, 
											new ImmediateDataRequestMonitor<IThreadDMData>(nthrm2_f) {
											
												@Override
												protected void handleCompleted() {
													IThreadDMData data = getData();

													// Check whether we know about cores
													if (data != null && data instanceof IGdbThreadDMData) {
														String[] cores = ((IGdbThreadDMData)data).getCores();
														if (cores != null && cores.length == 1) {
															if (coreContext_f.getId().equals(cores[0])) {
																// This thread belongs to the proper core
																threadContexts_f.add(threadContext_f);
															}
														}
													}
													nthrm2_f.done();
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
	
	/** Requests execution state of a thread.
	 *  Calls back to getThreadExecutionStateDone() on listener. */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public static void getThreadExecutionState(DSFSessionState sessionState,
			                                   ICPUDMContext cpuContext,
			                                   ICoreDMContext coreContext,
			                                   IMIExecutionDMContext threadContext,
			                                   DSFDebugModelListener listener,
			                                   Object arg)
	{
		IRunControl runControl = sessionState.getService(IRunControl.class);

		if (runControl == null) {
			listener.getThreadExecutionStateDone(cpuContext, coreContext, threadContext, null, arg);
			return;
		}

		final ICPUDMContext  cpuContext_f           = cpuContext;
		final ICoreDMContext coreContext_f          = coreContext;
		final IMIExecutionDMContext threadContext_f = threadContext;
		final DSFDebugModelListener listener_f      = listener;
		final Object arg_f                          = arg;

		
		if (runControl.isSuspended(threadContext_f) == false) {
			// The thread is running
			listener_f.getThreadExecutionStateDone(cpuContext_f, coreContext_f, threadContext_f, 
					                               VisualizerExecutionState.RUNNING, arg_f);
		} else {
			// For a suspended thread, let's see why it is suspended,
			// to find out if the thread is crashed
			runControl.getExecutionData(threadContext_f, 
					new ImmediateDataRequestMonitor<IExecutionDMData>() {
				@Override
				protected void handleCompleted() {
					IExecutionDMData executionData = getData();

					VisualizerExecutionState state = VisualizerExecutionState.SUSPENDED;
					
					if (isSuccess() && executionData != null) {
						if (executionData.getStateChangeReason() == StateChangeReason.SIGNAL) {
							if (executionData instanceof IExecutionDMData2) {
								String details = ((IExecutionDMData2)executionData).getDetails();
								if (details != null) {
									if (isCrashSignal(details)) {
										state = VisualizerExecutionState.CRASHED;
									}
								}
							}
						}
					}
					
					listener_f.getThreadExecutionStateDone(cpuContext_f, coreContext_f, threadContext_f, state, arg_f);
				}
			});
		}

	}

	/**
	 * Return true if the string SIGNALINFO describes a signal
	 * that indicates a crash.
	 */
	public static boolean isCrashSignal(String signalInfo) {
		if (signalInfo.startsWith("SIGHUP") || //$NON-NLS-1$
				signalInfo.startsWith("SIGILL") || //$NON-NLS-1$
				signalInfo.startsWith("SIGABRT") || //$NON-NLS-1$
				signalInfo.startsWith("SIGBUS") || //$NON-NLS-1$
				signalInfo.startsWith("SIGSEGV")) { //$NON-NLS-1$
			// Not sure about the list of events here...
			// We are dealing with a crash
			return true;
		}

		return false;
	}
}
