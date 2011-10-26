// ===========================================================================
// VisualizerThread.java -- Visualizer framework model object.
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

// Package declaration
package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model;

// Java classes

// Custom classes


//----------------------------------------------------------------------------
// IVisualizer
//----------------------------------------------------------------------------

/** Represents single thread. */
public class VisualizerThread
	implements Comparable<VisualizerThread>
{
	// --- members ---
	
	/** Current core this thread is on. */
	public VisualizerCore m_core;
	
	/** Process ID (pid). */
	public int m_pid;
	
	/** Thread ID (tid). */
	public int m_tid;
	
	
	// --- constructors/destructors ---
	
	/** Constructor */
	public VisualizerThread(VisualizerCore core, int pid, int tid)
	{
		m_core = core;
		m_pid = pid;
		m_tid = tid;
	}
	
	/** Dispose method */
	public void dispose()
	{
		m_core = null;
	}
	
	
	// --- accessors ---
	
	/** Gets core. */
	public VisualizerCore getCore()
	{
		return m_core;
	}
	
	/** Gets process id (pid). */
	public int getPID()
	{
		return m_tid;
	}
	
	/** Gets thread id (tid). */
	public int getTID()
	{
		return m_tid;
	}

	
	// --- methods ---
	

	
	// --- Comparable implementation ---
	
	/** Compares this item to the specified item. */
	public int compareTo(VisualizerThread o) {
		int result = 0;
		if (o != null) {
			if (m_pid < o.m_pid) {
				result = -1;
			}
			else if (m_pid > o.m_pid) {
				result = 1;
			}
			else if (m_tid < o.m_tid) {
				result = -1;
			}
			else if (m_tid > o.m_tid) {
				result = 1;
			}
		}
		return result;
	}
}
