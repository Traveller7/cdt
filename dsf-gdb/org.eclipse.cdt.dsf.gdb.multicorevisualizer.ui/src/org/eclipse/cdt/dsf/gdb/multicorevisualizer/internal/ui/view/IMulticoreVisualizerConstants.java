/*******************************************************************************
 * Copyright (c) 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view;

import org.eclipse.cdt.visualizer.ui.util.Colors;
import org.eclipse.swt.graphics.Color;



/**
 * Constants to be used in the Multicore Visualizer.
 * 
 * @noimplement This interface is not intended to be implemented by clients.
 */

public class IMulticoreVisualizerConstants {
	
	// Colors for drawing threads
	
	/** Color to be used to draw a running thread */
	public static final Color COLOR_RUNNING_THREAD = Colors.GREEN;
	/** Color to be used to draw a suspended thread */
	public static final Color COLOR_SUSPENDED_THREAD = Colors.YELLOW;
	/** Color to be used to draw a crashed thread */
	public static final Color COLOR_CRASHED_THREAD = Colors.RED;
	/** Color to be used to draw an exited thread (if they are being shown) */
	public static final Color COLOR_EXITED_THREAD = Colors.GRAY;

	// Colors for drawing cores
	
	/** Color to be used to draw a running core */
	public static final Color COLOR_RUNNING_CORE_FG = Colors.GREEN;
	public static final Color COLOR_RUNNING_CORE_BG = Colors.DARK_GREEN;
	/** Color to be used to draw a suspended core */
	public static final Color COLOR_SUSPENDED_CORE_FG = Colors.YELLOW;
	public static final Color COLOR_SUSPENDED_CORE_BG = Colors.DARK_YELLOW;
	/** Color to be used to draw a crashed core */
	public static final Color COLOR_CRASHED_CORE_FG = Colors.RED;
	public static final Color COLOR_CRASHED_CORE_BG = Colors.DARK_RED;

}
