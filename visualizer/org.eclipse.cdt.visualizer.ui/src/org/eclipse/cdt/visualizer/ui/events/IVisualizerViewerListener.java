// ===========================================================================
// IVisualizerViewerListener.java -- IVisualizerViewer event listener.
// ===========================================================================
// Copyright (C) 2011, Tilera Corporation. All rights reserved.
// Use is subject to license terms.
// ===========================================================================

// package declaration
package org.eclipse.cdt.visualizer.ui.events;

import org.eclipse.cdt.visualizer.ui.IVisualizerViewer;


// ---------------------------------------------------------------------------
// IVisualizerViewerListener
// ---------------------------------------------------------------------------

/**
 * IVisualizerViewer event listener.
 */
public interface IVisualizerViewerListener
{
	// --- methods ---
	
	/** Invoked when VisualizerViewer's selected IVisualizer changes. */
	public void visualizerEvent(IVisualizerViewer source, VisualizerViewerEvent event);
	
}
