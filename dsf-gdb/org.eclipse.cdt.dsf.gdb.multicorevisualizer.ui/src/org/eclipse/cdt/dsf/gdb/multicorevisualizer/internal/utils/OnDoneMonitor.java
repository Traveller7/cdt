// ===========================================================================
// OnDoneMonitor.java -- Synchronous RequestMonitor implementation.
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
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;


// ---------------------------------------------------------------------------
// OnDoneMonitor
// ---------------------------------------------------------------------------

/**
 * Synchronous RequestMonitor implementation.
 * Executes handleCompleted() immediately, that is, in same thread as done() call.
 */
public class OnDoneMonitor extends RequestMonitor
{
	// --- constructors ---
	
	public OnDoneMonitor() {
		super(ImmediateExecutor.getInstance(), null);
	}
	
	public OnDoneMonitor(RequestMonitor parentMonitor) {
		super(ImmediateExecutor.getInstance(), parentMonitor);
	}
	
	// --- RequestMonitor implementation ---
	
	@Override
	protected void handleCompleted() {
		
	}
}
