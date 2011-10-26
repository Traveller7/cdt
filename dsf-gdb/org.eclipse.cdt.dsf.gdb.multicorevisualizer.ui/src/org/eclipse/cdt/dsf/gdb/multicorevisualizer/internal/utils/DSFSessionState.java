// ===========================================================================
// DSFSessionState.java -- DSF session state object
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
package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.utils;

//Java API classes
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

//Eclipse/CDT classes
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.MulticoreVisualizerUIPlugin;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardware;
import org.eclipse.cdt.dsf.gdb.service.IGDBHardware.IHardwareTargetDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;

// custom classes


// ---------------------------------------------------------------------------
// DSFSessionState
// ---------------------------------------------------------------------------

/**
 * DSF session state object.
 * 
 * Encapsulates and manages DsfSession we're currently tracking.
 */
@SuppressWarnings("restriction")
public class DSFSessionState
{
	// --- members ---

	/** Current session ID. */
	protected String m_sessionId;

	/** Current set of session event listeners. */
	protected List<Object> m_sessionListeners;
	
	/** Services tracker, used to access services. */
	protected DsfServicesTracker m_servicesTracker;

	/** Current hardware target context. */
	protected volatile IHardwareTargetDMContext m_hardwareTargetContext;
	
	
	// --- constructors/destructors ---
	
	/** Constructor. */
	public DSFSessionState(IDMVMContext vmContext)
	{
		IDMContext dmContext = vmContext.getDMContext();
		m_sessionId = dmContext.getSessionId();
		m_sessionListeners = new ArrayList<Object>();
		m_servicesTracker = new DsfServicesTracker(MulticoreVisualizerUIPlugin.getBundleContext(), m_sessionId);
		
		m_hardwareTargetContext = DMContexts.getAncestorOfType(dmContext, IHardwareTargetDMContext.class);
	}
	
	/** Dispose method. */
	public void dispose()
	{
		if (m_sessionId != null) {
			removeAllServiceEventListeners();
			m_sessionId = null;
			m_sessionListeners = null;
		}
		
		if (m_servicesTracker != null) {
			m_servicesTracker.dispose();				
			m_servicesTracker = null;
		}
		
		m_hardwareTargetContext = null;
	}
	
	
	// --- listener management ---

	// <soapbox>
	// Good god. Who decided it was okay to use _annotations_ to define
	// the callback methods on a listener instance, for crying out loud?
	// Isn't this, like, the _definitive_ use case for interfaces?
	// </soapbox>

	/** Adds a service event listener. */
	public void addServiceEventListener(Object listener)
	{
		final Object listener_f = listener;
		final DsfSession session_f = getDsfSession();
		if (session_f != null) {
			try {
				session_f.getExecutor().execute(new DsfRunnable() {
					public void run() {
						session_f.addServiceEventListener(listener_f, null);
						m_sessionListeners.add(listener_f);
					}
				});
    		} catch (RejectedExecutionException e) {
                // Session is shut down.
    		}
		}
	}
	
	/** Removes a service event listener. */
	public void removeServiceEventListener(Object listener)
	{
		final Object listener_f = listener;
		final DsfSession session_f = getDsfSession();
		if (session_f != null) {
			try {
				session_f.getExecutor().execute(new DsfRunnable() {
					public void run() {
						if (m_sessionListeners != null) {
							session_f.removeServiceEventListener(listener_f);
							m_sessionListeners.remove(listener_f);
						}
					}
				});
    		} catch (RejectedExecutionException e) {
                // Session is shut down.
    		}
		}
	}
	
	/** Removes all service event listeners. */
	public void removeAllServiceEventListeners()
	{
		final DsfSession session_f = getDsfSession();
		if (session_f != null) {
			try {
				session_f.getExecutor().execute(new DsfRunnable() {
					public void run() {
						if (m_sessionListeners != null) {
							for (Object listener : m_sessionListeners) {
								session_f.removeServiceEventListener(listener);
							}
							m_sessionListeners.clear();
						}
					}
				});
    		} catch (RejectedExecutionException e) {
                // Session is shut down.
    		}
		}
	}
	
	
	// --- methods ---
	
	/** Gets current DsfSession, if it's still active. */
	protected DsfSession getDsfSession() {
		return DsfSession.getSession(m_sessionId);
	}
	
	/** Executes DsfRunnable. */
	public void execute(DsfRunnable runnable)
	{
		try {
			DsfSession session = getDsfSession();
			if (session == null) {
				// TODO: log this?
			}
			else {
				session.getExecutor().execute(runnable);
			}
		}
		catch (RejectedExecutionException e) {
			// TODO: log or handle this properly.
			System.err.println("DSFSessionState.execute(): session rejected execution request."); //$NON-NLS-1$
		}
	}
	
	/** Gets service of the specified type. */
	public <V> V getService(Class<V> serviceClass) {
		return (m_servicesTracker == null) ? null : m_servicesTracker.getService(serviceClass);
	}
	
	
	// --- hardware service methods ---
	
	// TODO: maybe these belong in a subclass?
	
	/** Gets hardware target context. */
	public IHardwareTargetDMContext getHardwareContext()
	{
		return m_hardwareTargetContext;
	}
	
	/** Gets IGDBHardware service. */
	public IGDBHardware getHardwareService() {
		return getService(IGDBHardware.class);
	}
}
