// ===========================================================================
// VisualizerModel.java -- Visualizer framework model object.
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

//Java classes
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

//Custom classes


//----------------------------------------------------------------------------
// VisualizerModel
//----------------------------------------------------------------------------

/** Represents state to display in visualizer. */
public class VisualizerModel
{
	// --- members ---
	
	/** List of cpus (and cores) */
	protected ArrayList<VisualizerCPU> m_cpus;
	
	/** Lookup table for CPUs */
	protected Hashtable<Integer, VisualizerCPU> m_cpuMap;
	
	/** List of threads */
	protected ArrayList<VisualizerThread> m_threads;
	
	/** Completion state tracker. */
	protected Todo m_todo;
	
	
	// --- constructors/destructors ---
	
	/** Constructor */
	public VisualizerModel()
	{
		m_cpus = new ArrayList<VisualizerCPU>();
		m_cpuMap = new Hashtable<Integer, VisualizerCPU>();
		m_threads = new ArrayList<VisualizerThread>();
		m_todo = new Todo();
	}
	
	/** Dispose method */
	public void dispose()
	{
		if (m_cpus != null) {
			for (VisualizerCPU cpu : m_cpus) {
				cpu.dispose();
			}
			m_cpuMap.clear();
			m_cpuMap = null;
			m_cpus.clear();
			m_cpus = null;
		}
		if (m_threads != null) {
			for (VisualizerThread thread : m_threads) {
				thread.dispose();
			}
			m_threads.clear();
			m_threads = null;
		}
		if (m_todo != null) {
			m_todo.dispose();
			m_todo = null;
		}
	}
	
	
	// --- accessors ---
	
	/** Gets completion state tracker. */
	public Todo getTodo() {
		return m_todo;
	}
	
	// --- methods ---
	
	/** Sorts cores, cpus, etc. by IDs. */
	public void sort()
	{
		Collections.sort(m_cpus);
		for (VisualizerCPU cpu : m_cpus) cpu.sort();
		Collections.sort(m_threads);
	}
	
	
	// --- core/cpu management ---
	
	/** Gets number of CPUs. */
	public int getCPUCount()
	{
		return m_cpus.size();
	}
	
	/** Gets CPU with specified ID. */
	public VisualizerCPU getCPU(int id)
	{
		return m_cpuMap.get(id);
	}
	
	/** Gets Core with specified ID. */
	public VisualizerCore getCore(int id)
	{
		VisualizerCore result = null;
		for (VisualizerCPU cpu: m_cpus) {
			result = cpu.getCore(id);
			if (result != null) break;
		}
		return result;
	}
	
	/** Gets CPU set. */
	public List<VisualizerCPU> getCPUs()
	{
		return m_cpus;
	}
	
	/** Adds CPU. */
	public VisualizerCPU addCPU(VisualizerCPU cpu)
	{
		m_cpus.add(cpu);
		m_cpuMap.put(cpu.getID(), cpu);
		return cpu;
	}

	/** Removes CPU. */
	public void removeCPU(VisualizerCPU cpu)
	{
		m_cpus.remove(cpu);
		m_cpuMap.remove(cpu.getID());
	}

	
	/** Gets maximum number of cores per CPU. */
	public int getCoresPerCPU()
	{
		int maxCores = 1;
		for (VisualizerCPU cpu : m_cpus) {
			int cores = cpu.getCoreCount();
			if (cores > maxCores) maxCores = cores;
		}
		return maxCores;
	}
	
	
	// --- thread management ---
	
	/** Gets threads. */
	public List<VisualizerThread> getThreads()
	{
		return m_threads;
	}
	
	/** Adds thread. */
	public VisualizerThread addThread(VisualizerThread thread)
	{
		m_threads.add(thread);
		return thread;
	}

	/** Removes thread. */
	public void removeThread(VisualizerThread thread)
	{
		m_threads.remove(thread);
	}


	
}