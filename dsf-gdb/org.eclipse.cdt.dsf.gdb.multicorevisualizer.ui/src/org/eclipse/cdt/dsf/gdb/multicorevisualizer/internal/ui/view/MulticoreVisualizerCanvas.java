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

package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.view;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerCPU;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerCore;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerModel;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model.VisualizerThread;
import org.eclipse.cdt.visualizer.ui.canvas.GraphicCanvas;
import org.eclipse.cdt.visualizer.ui.plugin.CDTVisualizerUIPlugin;
import org.eclipse.cdt.visualizer.ui.util.GUIUtils;
import org.eclipse.cdt.visualizer.ui.util.MouseMonitor;
import org.eclipse.cdt.visualizer.ui.util.SelectionManager;
import org.eclipse.cdt.visualizer.ui.util.Timer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;


/**
 * MulticoreVisualizer's display canvas.
 */
public class MulticoreVisualizerCanvas extends GraphicCanvas
	implements ISelectionProvider
{
	// --- constants ---

	/** Canvas update interval in milliseconds. */
	protected static final int CANVAS_UPDATE_INTERVAL = 100;
	
	/** Spacing to allow between threads, when many are displayed on same tile. */
	protected static final int THREAD_SPACING = 8;
	

	// --- members ---
	
	/** Update timer */
	protected Timer m_updateTimer = null;
	
	/** Whether we need to recache graphic objects. */
	protected boolean m_recache = true;
	
	/**
	 * Whether we need to recache objects that depend on target state.
	 */
	protected boolean m_recacheState = true;
	
	/**
	 * Whether view size has changed, requiring us to recalculate object sizes.
	 */
	protected boolean m_recacheSizes = true;
	
	/** Whether we need to repaint the canvas */
	protected boolean m_update = true;


	// --- UI members ---
	
	/** Text font */
	protected Font m_textFont = null;
	
	/** Externally visible selection manager. */
	protected SelectionManager m_selectionManager;

    /** Mouse-drag marquee graphic element */
    protected MulticoreVisualizerMarquee m_marquee = null;
    
	/** Mouse click/drag monitor */
    protected MouseMonitor m_mouseMonitor = null;
	
    /** Currently selected threads, if any.
     *  (Used to restore selection whenever we need to
     *   recreate thread graphic display objects.)
     */
    protected HashSet<Integer> m_selectedThreads = null;
	

	// --- cached repaint state ---
	
	/** Current visualizer model we're displaying. */
	protected VisualizerModel m_model = null;
	
	/** Number of CPUs to display. */
	protected int m_cpu_count = 15;
	
	/** Number of Cores per CPU to display. */
	protected int m_cores_per_cpu = 3;
	
	/** List of CPUs we're displaying. */
	protected ArrayList<MulticoreVisualizerCPU> m_cpus = null;
	protected Hashtable<VisualizerCPU, MulticoreVisualizerCPU> m_cpuMap = null;

	/** List of CPU cores we're displaying. */
	protected ArrayList<MulticoreVisualizerCore> m_cores = null;
	protected Hashtable<VisualizerCore, MulticoreVisualizerCore> m_coreMap = null;
	
	/** Graphic objects representing threads */
	protected ArrayList<MulticoreVisualizerThread> m_threads = null;
	protected Hashtable<VisualizerThread, MulticoreVisualizerThread> m_threadMap = null;
	

	// --- constructors/destructors ---
	
	/** Constructor. */
	public MulticoreVisualizerCanvas(Composite parent) {
		super(parent);
		initMulticoreVisualizerCanvas(parent);
	}
	
	/** Dispose method. */
	@Override
	public void dispose() {
		cleanupMulticoreVisualizerCanvas();
		super.dispose();
	}

	
	// --- init methods ---
	
	/** Initializes control */
	protected void initMulticoreVisualizerCanvas(Composite parent) {
		// perform any initialization here
		
		// RESOURCES: need to decide where to get Fonts from.
		
		// text font
		m_textFont = CDTVisualizerUIPlugin.getResources().getFont("Luxi Sans", 8); //$NON-NLS-1$
		setFont(m_textFont);
		
		// initialize cached state storage
		m_cpus        = new ArrayList<MulticoreVisualizerCPU>();
		m_cpuMap      = new Hashtable<VisualizerCPU, MulticoreVisualizerCPU>();
		
		m_cores       = new ArrayList<MulticoreVisualizerCore>();
		m_coreMap     = new Hashtable<VisualizerCore, MulticoreVisualizerCore>();
		
		m_threads     = new ArrayList<MulticoreVisualizerThread>();
		m_threadMap   = new Hashtable<VisualizerThread, MulticoreVisualizerThread>();
		
		// mouse-drag monitor
		m_mouseMonitor = new MouseMonitor(this) {
			/** Invoked for a selection click at the specified point. */
			@Override
			public void select(int x, int y, int keys) {
				MulticoreVisualizerCanvas.this.select(x, y, keys);
			}

			/** Invoked for a double click at the specified point. */
			@Override
			public void mouseDoubleClick(int button, int x, int y, int keys) {
				MulticoreVisualizerCanvas.this.select(x, y, keys);
			}

			/** Invoked for a menu mouse down at the specified point. */
			@Override
			public void mouseDown(int button, int x, int y, int keys) {
				// select item(s) under the mouse before popping up the context menu
				if (button == RIGHT_BUTTON) {
					MulticoreVisualizerCanvas.this.select(x, y, keys);
				}
			}

			/** Invoked when mouse is dragged. */
			@Override
			public void drag(int button, int x, int y, int keys, int dragState) {
				if (button == LEFT_BUTTON) {
					MulticoreVisualizerCanvas.this.drag(x, y, keys, dragState);
				}
			}
		};
		
		// saved selection, if any
		m_selectedThreads = new HashSet<Integer>();
		
		// selection marquee
		m_marquee = new MulticoreVisualizerMarquee();

		// selection manager
		m_selectionManager = new SelectionManager(this, "MulticoreVisualizerCanvas selection manager"); //$NON-NLS-1$
		
		// add update timer
		m_updateTimer = new Timer(CANVAS_UPDATE_INTERVAL) {
			@Override
			public void run() {
				update();
			}
		};
		m_updateTimer.setRepeating(false); // one-shot timer
		m_updateTimer.start();
	}
	
	/** Cleans up control */
	protected void cleanupMulticoreVisualizerCanvas() {
		if (m_updateTimer != null) {
			m_updateTimer.dispose();
			m_updateTimer = null;
		}
	    if (m_marquee != null) {
	    	m_marquee.dispose();
	    	m_marquee = null;
	    }
        if (m_mouseMonitor != null) {
            m_mouseMonitor.dispose();
            m_mouseMonitor = null;
	    }
	    if (m_selectedThreads != null) {
	    	m_selectedThreads.clear();
	    	m_selectedThreads = null;
	    }
	    if (m_selectionManager != null) {
	    	m_selectionManager.dispose();
	    	m_selectionManager = null;
	    }
        if (m_cpus != null) {
        	m_cpus.clear();
        	m_cpus = null;
        }
        if (m_cpuMap != null) {
        	m_cpuMap.clear();
        	m_cpuMap = null;
        }
        if (m_cores != null) {
        	m_cores.clear();
        	m_cores = null;
        }
        if (m_coreMap != null) {
        	m_coreMap.clear();
        	m_coreMap = null;
        }
        if (m_threads != null) {
        	m_threads.clear();
        	m_threads = null;
        }
        if (m_threadMap != null) {
        	m_threadMap.clear();
        	m_threadMap = null;
        }
	}
	

	// --- accessors ---
	
	/** Sets model to display, and requests canvas update. */
	public void setModel(VisualizerModel model)
	{
		m_model = model;
		requestRecache();
		requestUpdate();
	}
	
	
	
	// --- resize methods ---
	
	/** Invoked when control is resized. */
	@Override
	public void resized(Rectangle bounds) {
		requestRecache(false, true);
		// note: resize itself will trigger an update, so we don't have to request one
	}

	
	// --- update methods ---
	
	/**
	 * Requests an update on next timer tick.
	 * NOTE: use this method instead of normal update(),
	 * multiple update requests on same tick are batched.
	 */
	public void requestUpdate() {
		GUIUtils.exec(new Runnable() { public void run() {
			m_updateTimer.start();
		}});
	}
	

	// --- paint methods ---
	
	/** Requests that next paint call should recache state and/or size information */
	// synchronized so we don't change recache flags while doing a recache
	public synchronized void requestRecache() {
		requestRecache(true, true);
	}

	/** Requests that next paint call should recache state and/or size information */
	// synchronized so we don't change recache flags while doing a recache
	public synchronized void requestRecache(boolean state, boolean sizes) {
		m_recache = true;
		// NOTE: we intentionally OR these flags with any pending request(s)
		m_recacheState |= state;
		m_recacheSizes |= sizes;
	}
	
	/** Fits n square items into a rectangle of the specified size.
	 *  Returns largest edge of one of the square items that allows
	 *  them all to pack neatly in rows/columns in the specified area. */
	public int fitSquareItems(int nitems, int width, int height) {
		int max_edge = 0;
		if (width > height) {
			for (int items_per_row = nitems; items_per_row > 0; --items_per_row) {
				int rows = (int) Math.ceil(1.0 * nitems / items_per_row);
				int w = width / items_per_row;
				int h = height / rows;
				int edge = (w < h) ? w : h;
				if (edge * rows > height || edge * items_per_row > width) continue;
				if (edge > max_edge) max_edge = edge;
			}
		}
		else {
			for (int items_per_col = nitems; items_per_col > 0; --items_per_col) {
				int cols = (int) Math.ceil(1.0 * nitems / items_per_col);
				int w = width / cols;
				int h = height / items_per_col;
				int edge = (w < h) ? w : h;
				if (edge * cols > width || edge * items_per_col > height) continue;
				if (edge > max_edge) max_edge = edge;
			}
		}
		return max_edge;
	}
	
	/** Recache persistent objects (tiles, etc.) for new monitor */
	// synchronized so we don't change recache flags while doing a recache
	public synchronized void recache() {
		if (! m_recache) return; // nothing to do, free the lock quickly

		if (m_recacheState) {

			// clear all grid view objects
			clear();
			
			// clear cached state
			m_cpus.clear();
			m_cores.clear();
			m_threads.clear();

			// For debugging purposes only, allows us to force a CPU count.
			//int cpu_count = 0;
			//int force_cpu_count = 2;
			
			if (m_model != null) {
				for (VisualizerCPU cpu : m_model.getCPUs()) {
					//if (force_cpu_count >= cpu_count) break;
					//cpu_count++;
					MulticoreVisualizerCPU mcpu = new MulticoreVisualizerCPU(cpu.getID());
					m_cpus.add(mcpu);
					m_cpuMap.put(cpu, mcpu);
					for (VisualizerCore core : cpu.getCores()) {
						MulticoreVisualizerCore mcore = new MulticoreVisualizerCore(mcpu, core.getID());
						m_cores.add(mcore);
						m_coreMap.put(core, mcore);
					}
				}
			}
			/*
			while (cpu_count < force_cpu_count) {
				MulticoreVisualizerCPU mcpu = new MulticoreVisualizerCPU(cpu_count);
				m_cpus.add(mcpu);
				cpu_count++;
			}
			*/
			
			// we've recached state, which implies recacheing sizes too
			m_recacheState = false;
			m_recacheSizes = true;
		}
		
		if (m_recacheSizes) {
			// update cached size information
			
			// General margin/spacing constants.
			int cpu_margin = 8;       // margin around edges of CPU grid
			int cpu_separation = 6;   // spacing between CPUS
			
			int core_margin = 4;      // margin around cores in a CPU 
			int core_separation = 2;  // spacing between cores

			// Get overall area we have for laying out content.
			Rectangle bounds = getClientArea();
			GUIUtils.inset(bounds, cpu_margin);

			// Figure out area to allocate to each CPU box.
			int ncpus  = m_cpus.size();
			int width  = bounds.width  + cpu_separation;
			int height = bounds.height + cpu_separation;
			int cpu_edge = fitSquareItems(ncpus, width, height);
			int cpu_size = cpu_edge - cpu_separation;
			if (cpu_size < 0) cpu_size = 0;
			
			// Calculate area on each CPU for placing cores.
			int ncores  = m_cores.size();
			int cpu_width  = cpu_size - core_margin * 2 + core_separation;
			int cpu_height = cpu_size - core_margin * 2 + core_separation;
			int core_edge  = fitSquareItems(ncores, cpu_width, cpu_height);
			int core_size  = core_edge - core_separation;
			if (core_size < 0) core_size = 0;
			
			int x = bounds.x, y = bounds.y;
			for (MulticoreVisualizerCPU cpu : m_cpus) {
				cpu.setBounds(x, y, cpu_size-1, cpu_size-1);
				
				int left = x + core_margin;
				int cx = left, cy = y + core_margin;
				for (MulticoreVisualizerCore core : cpu.getCores())
				{
					core.setBounds(cx, cy, core_size, core_size);
					
					cx += core_size + core_separation;
					if (cx + core_size > x + cpu_size) {
						cx = left;
						cy += core_size + core_separation;
					}
				}
				
				x += cpu_size + cpu_separation;
				if (x + cpu_size > bounds.x + width) {
					x = bounds.x;
					y += cpu_size + cpu_separation;
				}
			}

			m_recacheSizes = false;
		}
	}
	
	/** Invoked when canvas repaint event is raised.
	 *  Default implementation clears canvas to background color.
	 */
	@Override
	public void paintCanvas(GC gc) {
		// NOTE: We have a little setup to do first,
		// so we delay clearing/redrawing the canvas until needed,
		// to minimize any potential visual flickering.
		
		// save current selection, if any
		saveSelection();
		
		// recache/resize tiles & shims if needed
		recache();

		// do any "per frame" updating/replacement of graphic objects
		
		// recalculate process/thread graphic objects on the fly
		// TODO: can we cache/pool these and just move them around?
		for (MulticoreVisualizerCore core : m_cores) {
			core.removeAllThreads();
		}
		m_threads.clear();
		m_threadMap.clear();
		
		// update based on current processes/threads
		if (m_model != null) {
			
			// NOTE: we assume that we've already created and sized the graphic
			// objects for cpus/cores in recache() above,
			// so we can use these to determine the size/location of more dynamic elements
			// like processes and threads
			
			for (VisualizerThread thread : m_model.getThreads()) {
				VisualizerCore core = thread.getCore();
				MulticoreVisualizerCore mcore = m_coreMap.get(core);
				if (mcore != null) {
					MulticoreVisualizerThread mthread =
						new MulticoreVisualizerThread(mcore, thread.getPID(), thread.getTID(), thread.getState());
					mcore.addThread(mthread);
					m_threads.add(mthread);
					m_threadMap.put(thread, mthread);
				}
			}
			
			// now set sizes of processes/threads for each tile
			for (MulticoreVisualizerCore core : m_cores) {
				Rectangle bounds = core.getBounds();
				
				// how we lay out threads depends on how many there are
				List<MulticoreVisualizerThread> threads = core.getThreads();
				int threadspotsize = MulticoreVisualizerThread.THREAD_SPOT_SIZE;
				int threadheight = threadspotsize + THREAD_SPACING;
				int count = threads.size();
				int tileheight = bounds.height - 4;
				int tx = bounds.x + 2;
				int ty = bounds.y + 2;
				int dty = (count < 1) ? 0 : tileheight / count;
				if (dty > threadheight) dty = threadheight;
				if (count > 0 && dty * count <= tileheight) {
					ty = bounds.y + 2 + (tileheight - (dty * count)) / 2;
					if (ty < bounds.y + 2) ty = bounds.y + 2;
				}
				else if (count > 0) {
					dty = tileheight / count;
					if (dty > threadheight) dty = threadheight;
				}
				int t = 0;
				for (MulticoreVisualizerThread threadobj : threads) {
					int y = ty + dty * (t++);
					threadobj.setBounds(tx, y, threadspotsize, threadspotsize);
				}
			}
		}
		
		// restore selection, if any
		restoreSelection();

		// NOW we can clear the background
		clearCanvas(gc);

		// RESOURCES: need to decide where to get Colors from.
		// For now we grab them from Visualizer plugin, so make sure resources are initialized.
		CDTVisualizerUIPlugin.getResources();
		
		// paint cpus
		for (MulticoreVisualizerCPU cpu : m_cpus) {
			cpu.paintContent(gc);
		}

		// paint cores
		for (MulticoreVisualizerCore core : m_cores) {
			core.paintContent(gc);
		}
		
		// paint cpus IDs on top of cores
		for (MulticoreVisualizerCPU cpu : m_cpus) {
			cpu.paintDecorations(gc);
		}

		// paint threads on top of cores
		for (MulticoreVisualizerThread thread : m_threads) {
			thread.paintContent(gc);
		}
		
		// paint drag-selection marquee last, so it's on top.
		m_marquee.paintContent(gc);
	}
	
	
	// --- mouse event handlers ---

	/** Invoked when mouse is dragged. */
	public void drag(int x, int y, int keys, int dragState)
	{
		Rectangle region = m_mouseMonitor.getDragRegion();
		switch (dragState) {
		case MouseMonitor.MOUSE_DRAG_BEGIN:
			m_marquee.setBounds(region);
			m_marquee.setVisible(true);
			update();
			break;
		case MouseMonitor.MOUSE_DRAG:
			m_marquee.setBounds(region);
			update();
			break;
		case MouseMonitor.MOUSE_DRAG_END:
		default:
			m_marquee.setBounds(region);
			m_marquee.setVisible(false);

			boolean addToSelection = MouseMonitor.isShiftDown(keys);
			boolean toggleSelection = MouseMonitor.isControlDown(keys);
			
			selectRegion(m_marquee.getBounds(), addToSelection, toggleSelection);
	
			update();
			break;
		}
	}

	/** Invoked for a selection click at the specified point. */
	public void select(int x, int y, int keys)
	{
		boolean addToSelection = MouseMonitor.isShiftDown(keys);
		boolean toggleSelection = MouseMonitor.isControlDown(keys);
		
		selectPoint(x,y, addToSelection, toggleSelection);
	}
	

	// --- selection methods ---
	
	/**
	 * Selects item, if any, at specified point.
	 * 
	 * If addToSelection is true, appends selected item to current selection,
	 * if it isn't already selected.
	 *  
	 * If toggleSelection is true, toggles selection of specified item,
	 * without changing selection state of other items.
	 *  
	 * Otherwise, selects item if any at point and deselects other items.
	 */
	public void selectPoint(int x, int y,
							boolean addToSelection, boolean toggleSelection)
	{
		// Currently we only allow selection of threads.
		if (m_threads != null) {

			MulticoreVisualizerThread thread = null;

			// first see if selection click landed on a thread dot.
			for (MulticoreVisualizerThread tobj : m_threads) {
				if (tobj.contains(x,y)) {
					thread = tobj;
					break;
				}
			}
			
			// if not, see if it landed on a core with only one thread
			if (thread == null) {
				for (MulticoreVisualizerCore tobj : m_cores) {
					if (tobj.contains(x,y)) {
						List<MulticoreVisualizerThread> threads = tobj.getThreads();
						if (threads.size() == 1) {
							thread = threads.get(0);
							break;
						}
					}
				}
			}

			if (! toggleSelection) {
				for (MulticoreVisualizerThread tobj : m_threads) {
					if (tobj != thread) {
						tobj.setSelected(false);
					}
				}
			}

			if (thread != null) {
				if (toggleSelection) {
					thread.setSelected(! thread.isSelected());
				}
				else {
					thread.setSelected(true);
				}
			}

			selectionChanged();
		}
	}
	
	/**
	 * Selects item(s), if any, in specified region
	 * 
	 * If addToSelection is true, appends selected item(s) to current selection,
	 * that aren't already selected.
	 *  
	 * If toggleSelection is true, toggles selection of specified item(s),
	 * without changing selection state of other items.
	 *  
	 * Otherwise, selects item(s) if any in region and deselects other items.
	 */
	public void selectRegion(Rectangle region,
							 boolean addToSelection, boolean toggleSelection)
	{
		// currently, we select/deselect threads, not processes or tiles
		if (m_threads != null) {

			for (MulticoreVisualizerThread tobj : m_threads) {
				
				boolean within = tobj.isWithin(region);
				if (! addToSelection || within) {
					if (toggleSelection) {
						if (within)
							tobj.setSelected(! tobj.isSelected());
					}
					else {
						tobj.setSelected(within);
					}
				}
			}
			
			selectionChanged();
		}
	}
	
	/** Clears tile selection. */
	public void clearSelection() {
		// currently, we select/deselect threads, not processes or tiles
		if (m_threads != null) {

			for (MulticoreVisualizerThread tobj : m_threads) {
				tobj.setSelected(false);
			}
			
			selectionChanged();
		}
	}
	
	
	// --- selection management methods ---
		
	/** Things to do whenever the selection changes. */
	protected void selectionChanged() {
		selectionChanged(true);
	}

	/** Things to do whenever the selection changes. */
	protected void selectionChanged(boolean raiseEvent) {
		saveSelection();
		requestUpdate();
		// NOTE: we update the exposed selection now,
		// and let the canvas update its display on the next "tick".
		updateSelection(raiseEvent);
	}

	/** Saves current selection
	 *  (i.e. as internal mementos, which we can use to re-select objects below.) */
	protected void saveSelection() {
		if (m_selectedThreads != null) {
			m_selectedThreads.clear();
			if (m_threads != null) {
				for (MulticoreVisualizerThread tobj : m_threads) {
					if (tobj.isSelected()) {
						int tid = tobj.getTID();
						m_selectedThreads.add(tid);
					}
				}
			}
		}
	}
	
	/** Restores current selection from saved state. */
	protected void restoreSelection() {
		if (m_selectedThreads != null && m_threads != null) {
			for (MulticoreVisualizerThread tobj : m_threads) {
				int tid = tobj.getTID();
				tobj.setSelected(m_selectedThreads.contains(tid));
			}
		}
	}
	
	/**
	 * Converts current thread selection to ISelection
	 * and reports a selection changed event.
	 */
	protected void updateSelection(boolean raiseEvent) {
		// TODO: decide what we expose as the canvas's selection.
		/*
		if (m_selectedThreads != NULL) {
			ISelection selection = SelectionUtils.toSelection(m_selectedThreads);
			setSelection(selection, raiseEvent);
		}
		*/
	}
	
	
	// --- ISelectionProvider implementation ---
	
	// Delegate to selection manager.
	
	/** Adds external listener for selection change events. */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		m_selectionManager.addSelectionChangedListener(listener);
	}

	/** Removes external listener for selection change events. */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		m_selectionManager.removeSelectionChangedListener(listener);
	}
	
	/** Raises selection changed event. */
	public void raiseSelectionChangedEvent() {
		m_selectionManager.raiseSelectionChangedEvent();
	}
	
	/** Gets current externally-visible selection. */
	public ISelection getSelection()
	{
		return m_selectionManager.getSelection();
	}
	
	/** Sets externally-visible selection. */
	public void setSelection(ISelection selection)
	{
		m_selectionManager.setSelection(selection);
	}
	
	/** Sets externally-visible selection. */
	public void setSelection(ISelection selection, boolean raiseEvent)
	{
		m_selectionManager.setSelection(selection, raiseEvent);
	}

    /** Sets whether selection events are enabled. */
    public void setSelectionEventsEnabled(boolean enabled) {
        m_selectionManager.setSelectionEventsEnabled(enabled);
    }
}
