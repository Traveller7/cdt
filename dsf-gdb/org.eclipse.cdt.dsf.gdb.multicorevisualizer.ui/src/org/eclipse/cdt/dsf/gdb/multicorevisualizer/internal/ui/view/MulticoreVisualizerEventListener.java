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

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view;


import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMContext;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IThreadDMData;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.MulticoreVisualizerUIPlugin;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerCore;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerExecutionState;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerThread;
import org.eclipse.cdt.dsf.gdb.service.IGDBProcesses.IGdbThreadDMData;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcessDMContext;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;

/**
 * DSF event listener class for the Multicore Visualizer.
 * This class will handle different relevant DSF events
 * and update the Multicore Visualizer accordingly.
 */
public class MulticoreVisualizerEventListener {	

	private MulticoreVisualizer fVisualizer;
	
	public MulticoreVisualizerEventListener(MulticoreVisualizer visualizer) {
		fVisualizer = visualizer;
	}

	@DsfServiceEventHandler
	public void handleEvent(ISuspendedDMEvent event) {
		IDMContext context = event.getDMContext();
		if (context instanceof IContainerDMContext) {
    		// We don't deal with processes
    	} else if (context instanceof IMIExecutionDMContext) {
    		// Thread suspended
    		int tid = ((IMIExecutionDMContext)context).getThreadId();

    		VisualizerThread thread = fVisualizer.getModel().getThread(tid);
    		
    		if (thread != null) {
    			assert thread.getState() == VisualizerExecutionState.RUNNING;
    			
    			thread.setState(VisualizerExecutionState.SUSPENDED);
    			fVisualizer.getMulticoreVisualizerCanvas().requestUpdate();
    		}
    	}
	}

	@DsfServiceEventHandler
	public void handleEvent(IResumedDMEvent event) {
		IDMContext context = event.getDMContext();
		if (context instanceof IContainerDMContext) {
    		// We don't deal with processes
    	} else if (context instanceof IMIExecutionDMContext) {
    		// Thread resumed
    		int tid = ((IMIExecutionDMContext)context).getThreadId();

    		VisualizerThread thread = fVisualizer.getModel().getThread(tid);
    		
    		if (thread != null) {
    			assert thread.getState() == VisualizerExecutionState.SUSPENDED ||
     				   thread.getState() == VisualizerExecutionState.CRASHED;
    			
    			thread.setState(VisualizerExecutionState.RUNNING);
    			fVisualizer.getMulticoreVisualizerCanvas().requestUpdate();
    		}
    	}
	}

	/** Invoked when a thread or process starts. */
	@DsfServiceEventHandler
	public void handleEvent(IStartedDMEvent event) {
		IDMContext context = event.getDMContext();
		if (context instanceof IContainerDMContext) {
    		// We don't deal with processes
    	} else if (context instanceof IMIExecutionDMContext) {
    		// New thread added
    		final IMIExecutionDMContext execDmc = (IMIExecutionDMContext)context;
			final IMIProcessDMContext processContext =
					DMContexts.getAncestorOfType(execDmc, IMIProcessDMContext.class);
			IThreadDMContext threadContext =
					DMContexts.getAncestorOfType(execDmc, IThreadDMContext.class);

			DsfServicesTracker tracker = 
					new DsfServicesTracker(MulticoreVisualizerUIPlugin.getBundleContext(), 
                                           execDmc.getSessionId());
			IProcesses procService = tracker.getService(IProcesses.class);
			tracker.dispose();
			
			procService.getExecutionData(threadContext, 
				new ImmediateDataRequestMonitor<IThreadDMData>() {
					@Override
					protected void handleSuccess() {
						IThreadDMData data = getData();
					
						// Check whether we know about cores
						if (data instanceof IGdbThreadDMData) {
							String[] cores = ((IGdbThreadDMData)data).getCores();
							if (cores != null) {
								assert cores.length == 1; // A thread belongs to a single core
								int coreId = Integer.parseInt(cores[0]);
								VisualizerCore vCore = fVisualizer.getModel().getCore(coreId);
								
								int pid = Integer.parseInt(processContext.getProcId());
								int tid = execDmc.getThreadId();

								fVisualizer.getModel().addThread(new VisualizerThread(vCore, pid, tid, VisualizerExecutionState.RUNNING));
								
								fVisualizer.getMulticoreVisualizerCanvas().requestUpdate();
							}
						}
					}
				}
			);
    	}
	}
	
	/** Invoked when a thread or process exits. */
	@DsfServiceEventHandler
	public void handleEvent(IExitedDMEvent event) {
		IDMContext context = event.getDMContext();
		if (context instanceof IContainerDMContext) {
    		// We don't deal with processes
    	} else if (context instanceof IMIExecutionDMContext) {
    		// Thread exited
    		int tid = ((IMIExecutionDMContext)context).getThreadId();

			fVisualizer.getModel().markThreadExited(tid);
			
			fVisualizer.getMulticoreVisualizerCanvas().requestUpdate();
    	}
	}	
}

