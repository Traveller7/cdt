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
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardware;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardware.ICPUDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardware.ICoreDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardware.IHardwareTargetDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses.IGdbThreadDMData;


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
		
		IGDBHardware hwService = sessionState.getHardwareService();
		if (hwService == null) {
			listener_f.getCPUsDone(null, arg_f);
			return;
		}
		
		IHardwareTargetDMContext contextToUse = sessionState.getHardwareContext();
		hwService.getCPUs(contextToUse,
			new OnDataRequestDoneMonitor<ICPUDMContext[]>() {
				@Override
				protected void handleCompleted(ICPUDMContext[] cpuContexts) {
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
		
		IGDBHardware hwService = sessionState.getHardwareService();
		if (hwService == null) {
			listener_f.getCoresDone(cpuContext, null, arg_f);
			return;
		}

		IDMContext contextToUse = cpuContext;
		if (contextToUse == null) {
			// if caller doesn't supply a specific cpu context,
			// use the hardware context (so we get all available cores?)
			contextToUse = sessionState.getHardwareContext();
		}
		
		hwService.getCores(contextToUse,
			new OnDataRequestDoneMonitor<ICoreDMContext[]>() {
				@Override
				protected void handleCompleted(ICoreDMContext[] coreContexts) {
					if (! isSuccess()) coreContexts = null;
					listener_f.getCoresDone(cpuContext_f, coreContexts, arg_f);
				}
			}
		);
	}
	
	/** Requests list of Cores.
	 *  Calls back to getCoresDone() on listener. */
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
			new OnDataRequestDoneMonitor<IDMContext[]>(null) {
		
				@Override
				protected void handleCompleted(IDMContext[] processContexts) {
					if (!isSuccess() || processContexts == null || processContexts.length < 1) {
						// Unable to get any process data for this core
						// Is this an issue? A core may have no processes/threads, right?
						listener_f.getThreadsDone(cpuContext_f, coreContext_f, null, arg_f);
						return;
					}
					
					final ArrayList<IDMContext> threadContexts_f =
							new ArrayList<IDMContext>();
				
					int count = processContexts.length;
					final OnNthDoneMonitor nthrm_f = new OnNthDoneMonitor(count,
						new OnDoneMonitor() {
							@Override
							protected void handleCompleted() {
								IDMContext[] threadContexts = new IDMContext[threadContexts_f.size()];
								threadContexts_f.toArray(threadContexts);
								listener_f.getThreadsDone(cpuContext_f, coreContext_f, threadContexts, arg_f);
							}
						});
					
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
										final IDMContext threadContext_f = threadContext;
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

}
