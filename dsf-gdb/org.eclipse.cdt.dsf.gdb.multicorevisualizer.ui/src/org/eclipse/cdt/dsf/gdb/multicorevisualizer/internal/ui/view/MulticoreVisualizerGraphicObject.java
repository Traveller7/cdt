// ===========================================================================
// MulticoreVisualizerGraphicObject.java -- Graphic object for MulticoreVisualizer
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

// Eclipse/CDT classes
import org.eclipse.cdt.visualizer.ui.canvas.GraphicObject;
import org.eclipse.swt.graphics.GC;

// custom classes


// ---------------------------------------------------------------------------
// MulticoreVisualizerObject
// ---------------------------------------------------------------------------

/**
 * Graphic object for MulticoreVisualizer.
 */
public class MulticoreVisualizerGraphicObject extends GraphicObject
{
	// --- members ---

	// --- constructors/destructors ---
	
	/** Constructor */
	public MulticoreVisualizerGraphicObject() {
		super();
	}
	
	/** Dispose method */
	@Override
	public void dispose() {
		super.dispose();
	}

	
	// --- methods ---
	
	/** Invoked to allow element to paint itself on the viewer canvas */
	@Override
	public void paintContent(GC gc) {
		gc.fillRectangle(m_bounds);
		gc.drawRectangle(m_bounds);
	}
}
