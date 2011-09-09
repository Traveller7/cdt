// ===========================================================================
// VisualizerViewerEvent.java -- IVisualizerViewer event
// ===========================================================================
// Copyright (C) 2011, Tilera Corporation. All rights reserved.
// Use is subject to license terms.
// ===========================================================================

// package declaration
package org.eclipse.cdt.visualizer.ui.events;

//Java API classes
//import java.util.*;

// custom classes
import org.eclipse.cdt.visualizer.ui.IVisualizerViewer;
import org.eclipse.cdt.visualizer.ui.util.Event;


// ---------------------------------------------------------------------------
// VisualizerViewerEvent
// ---------------------------------------------------------------------------

/**
 * IVisualizerViewer event
 */
public class VisualizerViewerEvent extends Event
{
	// --- event types ---
	
	/** Event type constant */
	public static final int VISUALIZER_CHANGED = 1;
	
	/** Event type constant */
	public static final int VISUALIZER_CONTEXT_MENU = 2;
	

	// --- members ---
	
	/** X coordinate, for menu events. */
	public int x;
	
	/** Y coordinate, for menu events. */
	public int y;
	
	
	// --- constructors/destructors ---
	
	/** Constructor. */
	public VisualizerViewerEvent(IVisualizerViewer source, int eventType) {
		super(source, eventType);
	}
	
	/** Constructor. */
	public VisualizerViewerEvent(IVisualizerViewer source, int eventType, int x, int y) {
		super(source, eventType);
		this.x = x;
		this.y = y;
	}
	
	/** Dispose method. */
	public void dispose() {
		super.dispose();
	}

	// --- Object methods ---
	
	/** Converts event type to string */
	public String typeToString(int type) {
		String result = "";
		switch (type) {
			case VISUALIZER_CHANGED:
				result = "VISUALIZER_CHANGED"; break;
			default:
				result = super.typeToString(type);
				break;
		}
		return result;
	}

}
