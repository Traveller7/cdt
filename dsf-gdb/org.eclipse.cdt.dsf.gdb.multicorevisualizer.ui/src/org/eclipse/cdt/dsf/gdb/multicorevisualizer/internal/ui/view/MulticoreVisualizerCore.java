// ===========================================================================
// MulticoreVisualizerCore.java -- MulticoreVisualizer CPU core object
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
package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view;

// Java API classes
//import java.util.*;

// SWT/JFace classes
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.visualizer.ui.util.GUIUtils;
import org.eclipse.swt.graphics.GC;

// Eclipse/CDT classes

// custom classes


// ---------------------------------------------------------------------------
// MulticoreVisualizerCPU
// ---------------------------------------------------------------------------

/**
 * MulticoreVisualizer CPU core object.
 */
public class MulticoreVisualizerCore extends MulticoreVisualizerGraphicObject
{
	// --- members ---
	
	/** Parent CPU. */
	protected MulticoreVisualizerCPU m_cpu = null;
	
	/** Core ID. */
	protected int m_id;
	
	/** List of threads currently on this core. */
	protected ArrayList<MulticoreVisualizerThread> m_threads;
	

	// --- constructors/destructors ---
	
	/** Constructor */
	public MulticoreVisualizerCore(MulticoreVisualizerCPU cpu, int id)
	{
		super();
		m_cpu = cpu;
		if (m_cpu != null) m_cpu.addCore(this);
		m_id = id;
		m_threads = new ArrayList<MulticoreVisualizerThread>();
	}
	
	/** Dispose method */
	@Override
	public void dispose() {
		super.dispose();
		if (m_threads != null) {
			m_threads.clear();
			m_threads = null;
		}
	}
	
	
	// --- accessors ---
	
	/** Gets parent CPU. */
	public MulticoreVisualizerCPU getCPU() {
		return m_cpu;
	}

	/** Gets Core ID. */
	public int getID() {
		return m_id;
	}

	
	// --- methods ---
	
	/** Adds child thread. */
	public void addThread(MulticoreVisualizerThread thread)
	{
		m_threads.add(thread);
	}
	
	/** Removes child thread. */
	public void removeThread(MulticoreVisualizerThread thread)
	{
		m_threads.remove(thread);
	}
	
	/** Removes all child threads. */
	public void removeAllThreads()
	{
		m_threads.clear();
	}
	
	/** Gets list of child threads. */
	public List<MulticoreVisualizerThread> getThreads()
	{
		return m_threads;
	}
	

	// --- paint methods ---
	
	// RESOURCES: need to decide where to get Fonts from.
	
	/** Invoked to allow element to paint itself on the viewer canvas */
	@Override
	public void paintContent(GC gc) {
		gc.fillRectangle(m_bounds);
		gc.drawRectangle(m_bounds);
		
		if (m_bounds.height > 16) {
			int text_indent = 3;
			int tx = m_bounds.x + text_indent;
			int ty = m_bounds.y + text_indent;
			GUIUtils.drawText(gc, Integer.toString(m_id), tx, ty);
		}
	}
}
