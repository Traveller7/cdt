// ===========================================================================
// SelectionProviderAdapter.java -- ISelectionProvider adapter
// ===========================================================================
// Copyright (C) 2011, Tilera Corporation. All rights reserved.
// Use is subject to license terms.
// ===========================================================================

// package declaration
package org.eclipse.cdt.visualizer.ui.util;

//Java API classes
//import java.util.*;

// SWT/JFace classes
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;

// Eclipse IDE classes

// custom classes
//import com.tilera.ide.TileraIDECorePlugin;
//import com.tilera.ide.TileraIDEUIPlugin;


// ---------------------------------------------------------------------------
// SelectionProviderAdapter
// ---------------------------------------------------------------------------

/**
 * Wrapper for selection "providers" that don't happen to implement
 * ISelectionProvider interface.
 */
public class SelectionProviderAdapter
	implements ISelectionProvider
{
	// --- members ---
	
	/** Real source object. */
	protected Object m_source = null;
	
	/** Selection manager. */
	protected SelectionManager m_selectionManager = null;
	
	
	// --- constructors/destructors ---
	
	/** Constructor. */
	public SelectionProviderAdapter(Object source) {
		m_source = source;
		m_selectionManager = new SelectionManager(this, "SelectionProviderAdapter for source " + m_source.toString());
	}
	
	/** Dispose method. */
	public void dispose() {
		m_source = null;
	    if (m_selectionManager != null) {
	    	m_selectionManager.dispose();
	    	m_selectionManager = null;
	    }
	}
	
	
	// --- accessors ---
	
	/** Gets wrapped selection source. */
	public Object getActualSource() {
		return m_source;
	}

	
	// --- ISelectionProvider implementation ---

	/** Adds selection change listener.
	 *  Default implementation does nothing.
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener)
	{
		m_selectionManager.addSelectionChangedListener(listener);
	}

	/** Removes selection change listener.
	 *  Default implementation does nothing.
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener)
	{
		m_selectionManager.removeSelectionChangedListener(listener);
	}

	/** Gets selection.
	 *  Default implementation does nothing.
	 */
	public ISelection getSelection() {
		return m_selectionManager.getSelection();
	}

	/** Sets selection.
	 *  Default implementation does nothing.
	 */
	public void setSelection(ISelection selection)
	{
		m_selectionManager.setSelection(selection);
	}
}
