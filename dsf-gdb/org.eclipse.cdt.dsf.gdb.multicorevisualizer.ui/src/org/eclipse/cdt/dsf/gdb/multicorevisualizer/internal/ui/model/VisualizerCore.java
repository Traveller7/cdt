// ===========================================================================
// VisualizerCore.java -- Visualizer framework model object.
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

//Custom classes


//----------------------------------------------------------------------------
// VisualizerCore
//----------------------------------------------------------------------------

/** Represents single core of a CPU. */
public class VisualizerCore
	implements Comparable<VisualizerCore>
{
	// --- members ---
	
	/** CPU this core is part of. */
	public VisualizerCPU m_cpu = null;
	
	/** Linux CPU ID of this core. */
	public int m_id = 0;
	
	
	// --- constructors/destructors ---
	
	/** Constructor */
	public VisualizerCore(VisualizerCPU cpu, int id)
	{
		m_cpu = cpu;
		m_id = id;
	}
	
	/** Dispose method */
	public void dispose()
	{
	}
	
	
	// --- accessors ---
	
	/** Gets CPU this core is part of. */
	public VisualizerCPU getCPU()
	{
		return m_cpu;
	}

	/** Gets Linux CPU ID of this core. */
	public int getID()
	{
		return m_id;
	}

	
	// --- methods ---
	
	
	
	// --- Comparable implementation ---
	
	/** Compares this item to the specified item. */
	public int compareTo(VisualizerCore o) {
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
