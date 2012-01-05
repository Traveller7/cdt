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

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model;

/** Represents single thread. */
public class VisualizerThread
	implements Comparable<VisualizerThread>
{
	// --- members ---
	
	/** Current core this thread is on. */
	protected VisualizerCore m_core;
	
	/** Process ID (pid). */
	protected int m_pid;
	
	/** Thread ID (tid). */
	protected int m_tid;
		
	/** Thread execution state. */
	protected VisualizerExecutionState m_threadState;

	
	// --- constructors/destructors ---

	/** Constructor. */
	public VisualizerThread(VisualizerCore core, int pid, int tid, VisualizerExecutionState state) {
		m_core = core;
		m_pid = pid;
		m_tid = tid;
		m_threadState = state;
	}
	
	/** Dispose method */
	public void dispose() {
		m_core = null;
	}
	
	
	// --- accessors ---
	
	/** Gets core. */
	public VisualizerCore getCore()	{
		return m_core;
	}
	
	public void setCore(VisualizerCore core) {
		m_core = core;
	}

	/** Gets process id (pid). */
	public int getPID() {
		return m_pid;
	}
	
	/** Gets thread id (tid). */
	public int getTID()	{
		return m_tid;
	}

	/** Gets thread execution state. */
	public VisualizerExecutionState getState() {
		return m_threadState;
	}
	
	/** Sets thread execution state. */
	public void setState(VisualizerExecutionState state) {
		m_threadState = state;
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
	
	/** Returns string representation. */
	@Override
	public String toString() {
		StringBuffer output = new StringBuffer();
		output.append(m_core).append(",Proc:").append(m_pid).append(",Thread:").append(m_tid);  //$NON-NLS-1$//$NON-NLS-2$
		return output.toString();
	}
}
