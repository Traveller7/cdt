// ===========================================================================
// VisualizerCPU.java -- Visualizer framework model object.
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
import java.util.List;

//Custom classes


//----------------------------------------------------------------------------
// VisualizerCPU
//----------------------------------------------------------------------------

/** Represents single CPU. */
public class VisualizerCPU
	implements Comparable<VisualizerCPU>
{
	// --- members ---
	
	/** ID of this core. */
	public int m_id;
	
	/** List of cores */
	protected ArrayList<VisualizerCore> m_cores;
	
	
	// --- constructors/destructors ---
	
	/** Constructor */
	public VisualizerCPU(int id)
	{
		m_id = id;
		m_cores = new ArrayList<VisualizerCore>();
	}
	
	/** Dispose method */
	public void dispose()
	{
		if (m_cores != null) {
			for (VisualizerCore core : m_cores) {
				core.dispose();
			}
			m_cores.clear();
			m_cores = null;
		}
	}
	
	
	// --- accessors ---
	
	/** Gets ID of this CPU. */
	public int getID()
	{
		return m_id;
	}
	

	// --- methods ---
	
	/** Gets number of cores. */
	public int getCoreCount()
	{
		return m_cores.size();
	}
	
	/** Gets cores. */
	public List<VisualizerCore> getCores()
	{
		return m_cores;
	}
	
	/** Adds core. */
	public VisualizerCore addCore(VisualizerCore core)
	{
		m_cores.add(core);
		return core;
	}

	/** Removes core. */
	public void removeCore(VisualizerCore core)
	{
		m_cores.remove(core);
	}

	
	/** Sorts cores, cpus, etc. by IDs. */
	public void sort()
	{
		Collections.sort(m_cores);
	}

	
	// --- Comparable implementation ---
	
	/** Compares this item to the specified item. */
	public int compareTo(VisualizerCPU o) {
		int result = 0;
		if (o != null) {
			if (m_id < o.m_id) {
				result = -1;
			}
			else if (m_id > o.m_id) {
				result = 1;
			}
		}
		return result;
	}
}
