// ===========================================================================
// OnNthDoneMonitor.java -- Synchronous RequestMonitor implementation.
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

//Java API classes

// DSF classes
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;


// ---------------------------------------------------------------------------
// OnNthDoneMonitor
// ---------------------------------------------------------------------------

/**
 * Synchronous RequestMonitor implementation.
 * Executes handleCompleted() immediately, that is, in same thread as done() call.
 */
public class OnNthDoneMonitor extends CountingRequestMonitor
{
	// --- constructors ---
	
	public OnNthDoneMonitor(int count, RequestMonitor parentMonitor) {
		super(ImmediateExecutor.getInstance(), parentMonitor);
		setDoneCount(count);
	}
}
