// ===========================================================================
// Todo.java -- Todo count tracker.
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

// Package declaration
package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.model;

//Java classes

//Custom classes


// ----------------------------------------------------------------------------
// Todo
// ----------------------------------------------------------------------------

/** Counter for objects that need to track asynchronous progress. */
public class Todo {
	
	// --- members ---
	
	int m_count = 0;
	
	
	// --- constructors/destructors ---
	
	/** Constructor. */
	public Todo() {
	}
	
	/** Constructor. */
	public Todo(int count) {
		m_count = count;
	}
	
	/** Dispose method. */
	public void dispose() {
		m_count = 0;
	}
	
	
	// --- accessors ---
	
	/** Gets count. */
	public int get() {
		return m_count;
	}
	
	/** Sets count. */
	public void set(int count) {
		m_count = count;
	}
	
	/** Returns true if count has reached zero. */
	public boolean isDone() {
		return m_count <= 0;
	}

	/** Increments count, returns new value. */
	public int increment(int n) {
		return (m_count += n);
	}

	/** Increments count, returns new value. */
	public int increment() {
		return (m_count += 1);
	}

	/** Decrements count, returns new value. */
	public int decrement(int n) {
		return (m_count -= n);
	}

	/** Decrements count, returns new value. */
	public int decrement() {
		return (m_count -= 1);
	}
	
	
	// --- methods ---
	
	/** Adds completion steps to count. */
	public void add(int n) {
		increment(n);
	}

	/** Adds completion step to count. */
	public void add() {
		increment(1);
	}

	/** Decrements count, returns true when it's reached zero. */
	public boolean done(int n) {
		return decrement(n) <= 0;
	}

	/** Decrements count, returns true when it's reached zero. */
	public boolean done() {
		return decrement() <= 0;
	}
}