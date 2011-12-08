/*******************************************************************************
 * Copyright (c) 2011 Tilera Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     William R. Swanson (Tilera Corporation) - initial API and implementation
 *******************************************************************************/

// package declaration
package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view;

import org.eclipse.cdt.visualizer.ui.util.Colors;
import org.eclipse.cdt.visualizer.ui.util.GUIUtils;
import org.eclipse.swt.graphics.GC;

/**
 * MulticoreVisualizer Thread object.
 */
public class MulticoreVisualizerThread extends MulticoreVisualizerGraphicObject
{
	// --- constants ---
	
	/** Thread "pixie" spot width */
	public static final int THREAD_SPOT_WIDTH = 12;
	
	/** Thread "pixie" spot height */
	public static final int THREAD_SPOT_HEIGHT = 12;
	
	/** Minimum containing object size to allow thread to draw itself. */
	public static final int MIN_PARENT_WIDTH = 14;

	
	// --- members ---
	
	/** Parent CPU. */
	protected MulticoreVisualizerCore m_core = null;
	
	/** Process ID. */
	protected int m_pid;
	
	/** Thread ID. */
	protected int m_tid;
	

	// --- constructors/destructors ---
	
	/** Constructor */
	public MulticoreVisualizerThread(MulticoreVisualizerCore core, int pid, int tid)
	{
		super();
		m_core = core;
		m_pid = pid;
		m_tid = tid;
	}
	
	/** Dispose method */
	@Override
	public void dispose() {
		super.dispose();
	}
	
	
	// --- accessors ---
	
	/** Gets parent Core. */
	public MulticoreVisualizerCore getCore() {
		return m_core;
	}
	
	/** Sets parent Core. */
	public void setCore(MulticoreVisualizerCore core) {
		m_core = core;
	}

	/** Gets Process ID. */
	public int getPID() {
		return m_pid;
	}

	/** Gets Thread ID. */
	public int getTID() {
		return m_tid;
	}

	
	// --- methods ---
	
	
	// --- paint methods ---

	// RESOURCES: need to decide where to get Colors and Fonts from.

	/** Invoked to allow element to paint itself on the viewer canvas */
	@Override
	public void paintContent(GC gc) {
		if (m_core.getWidth() >= MIN_PARENT_WIDTH) {
			gc.setBackground(Colors.YELLOW);
	
			int x = m_bounds.x;
			int y = m_bounds.y;
			int w = THREAD_SPOT_WIDTH;
			int h = THREAD_SPOT_HEIGHT;
			
			// draw an alpha-shaded "pixie" light for each thread
			int step1 = 3;
			int step2 = 6;
			gc.setAlpha(128);
			gc.fillOval(x, y, w, h);
			gc.setAlpha(196);
			gc.fillOval(x+step1, y+step1, w-step1*2, h-step1*2);
			gc.setAlpha(255);
			gc.fillOval(x+step2, y+step2, w-step2*2, h-step2*2);

			// draw text annotations
			gc.setBackground(Colors.BLACK);
			gc.setForeground(Colors.WHITE);
			
			// special case: for the "process" thread, draw an enclosing circle
			if (m_pid == m_tid) {
				gc.drawOval(x,y,w,h);
			}
			
			// if it has a debugger, add a marker
			// (for now, every thread is debugged.)
			GUIUtils.drawText(gc, "D", x+w, y-8); //$NON-NLS-1$
			
			// draw TID
			String displayTID = Integer.toString(m_tid);
			GUIUtils.drawText(gc, displayTID, x + w + 4, y + 2);
		}
	}
}
