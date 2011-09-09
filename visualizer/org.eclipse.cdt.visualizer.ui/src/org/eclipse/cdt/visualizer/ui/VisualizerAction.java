// ===========================================================================
// VisualizerAction.java -- Base class for visualizer actions. 
// ===========================================================================
// Copyright (C) 2011, Tilera Corporation. All rights reserved.
// Use is subject to license terms.
// ===========================================================================

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
