// ===========================================================================
// MulticoreVisualizerCPU.java -- MulticoreVisualizer CPU object
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
import java.util.ArrayList;
import java.util.List;

// SWT/JFace classes

import org.eclipse.cdt.visualizer.ui.util.GUIUtils;
import org.eclipse.swt.graphics.GC;

// Eclipse/CDT classes

// custom classes


// ---------------------------------------------------------------------------
// MulticoreVisualizerCPU
// ---------------------------------------------------------------------------

/**
 * Graphic object for MulticoreVisualizer.
 */
public class MulticoreVisualizerCPU extends MulticoreVisualizerGraphicObject
{
	// --- members ---
	
	/** CPU ID. */
	protected int m_id;
	
	/** Child cores. */
	protected ArrayList<MulticoreVisualizerCore> m_cores;
	

	// --- constructors/destructors ---
	
	/** Constructor */
	public MulticoreVisualizerCPU(int id)
	{
		super();
		m_id = id;
		m_cores = new ArrayList<MulticoreVisualizerCore>();
	}
	
	/** Dispose method */
	@Override
	public void dispose() {
		super.dispose();
	}
	
	
	// --- accessors ---
	
	/** Gets CPU ID. */
	public int getID() {
		return m_id;
	}

	
	// --- methods ---
	
	/** Adds child core. */
	public void addCore(MulticoreVisualizerCore core)
	{
		m_cores.add(core);
	}
	
	/** Removes child core. */
	public void removeCore(MulticoreVisualizerCore core)
	{
		m_cores.remove(core);
	}
	
	/** Gets list of child cores. */
	public List<MulticoreVisualizerCore> getCores()
	{
		return m_cores;
	}

	
	// --- paint methods ---
	
	/** Invoked to allow element to paint itself on the viewer canvas */
	@Override
	public void paintContent(GC gc) {
		gc.fillRectangle(m_bounds);
		gc.drawRectangle(m_bounds);
	}
	
	/** Returns true if object has decorations to paint. */
	@Override
	public boolean hasDecorations() {
		return true;
	}
	
	// RESOURCES: need to decide where to get Fonts from.
	
	/** Invoked to allow element to paint decorations on top of anything drawn on it */
	@Override
	public void paintDecorations(GC gc) {
		if (m_bounds.height > 20) {
			int text_indent = 6;
			int tx = m_bounds.x + m_bounds.width  - text_indent;
			int ty = m_bounds.y + m_bounds.height - text_indent;
			GUIUtils.drawTextAligned(gc, Integer.toString(m_id), tx, ty, false, false);
		}
	}

}
