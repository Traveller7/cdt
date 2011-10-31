// ===========================================================================
// DSFDebugModelListener.java -- DSFDebugModel listener
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
package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils;


// Java API classes

// Eclipse/CDT classes
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardware.ICPUDMContext;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardware.ICoreDMContext;

// custom classes


// ---------------------------------------------------------------------------
// DSFDebugModel listener
// ---------------------------------------------------------------------------

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

}
