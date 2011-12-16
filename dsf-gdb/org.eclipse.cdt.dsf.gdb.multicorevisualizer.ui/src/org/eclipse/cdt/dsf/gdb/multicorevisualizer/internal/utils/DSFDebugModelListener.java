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


import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerExecutionState;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardware.ICPUDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardware.ICoreDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;

/** Interface for classes that interact with DSFDebugModel. */
public interface DSFDebugModelListener {
	
	/** Invoked when getCPUs() request completes. */
	public void getCPUsDone(ICPUDMContext[] cpuContexts,
							Object arg);

	/** Invoked when getCores() request completes. */
	public void getCoresDone(ICPUDMContext cpuContext,
							 ICoreDMContext[] coreContexts,
							 Object arg);

	/** Invoked when getThreads() request completes. */
	public void getThreadsDone(ICPUDMContext cpuContext,
			 				   ICoreDMContext coreContext,
							   IDMContext[] threadContexts,
							   Object arg);

	/** Invoked when getThreadExecutionState() request completes. */
	public void getThreadExecutionStateDone(ICPUDMContext cpuContext,
			 				                ICoreDMContext coreContext,
							                IMIExecutionDMContext threadContext,
							                VisualizerExecutionState state,
							                Object arg);

}
