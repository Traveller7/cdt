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
package org.eclipse.cdt.visualizer.ui;

//Java API classes
//import java.util.*;

// SWT/JFace classes
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

// Eclipse IDE classes

// custom classes
//import com.tilera.ide.TileraIDECorePlugin;
//import com.tilera.ide.TileraIDEUIPlugin;


// ---------------------------------------------------------------------------
// VisualizerAction
// ---------------------------------------------------------------------------

/** Base class for visualizer actions.
 *  (Viewers are not required to use this class. This is simply a
 *  convenience wrapper for the standard Action class.)
 */
public class VisualizerAction extends Action
{
	// --- members ---
	
	// --- constructors/destructors ---
	
	/** Constructor. */
	public VisualizerAction(String text, String description,
						ImageDescriptor image) {
		super(text, image);
		setDescription(description);
	}
	
	/** Constructor. */
	public VisualizerAction(String text, String description,
						ImageDescriptor enabledImage, ImageDescriptor disabledImage) {
		super(text, enabledImage);
		setDescription(description);
		setDisabledImageDescriptor(disabledImage);
	}

	
	// --- methods ---

}
