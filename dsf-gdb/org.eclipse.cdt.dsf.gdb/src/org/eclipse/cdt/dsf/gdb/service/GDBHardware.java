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
package org.eclipse.cdt.dsf.gdb.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.Immutable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.AbstractDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.datamodel.IDMData;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.command.CommandCache;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIListThreadGroupsInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIListThreadGroupsInfo.IThreadGroupInfo;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * This class implements the IGDBHardware interface which gives access
 * to hardware information about the target.
 * 
 * @since 4.1
 */
public class GDBHardware extends AbstractDsfService implements IGDBHardware, ICachingService {

	@Immutable
	protected static class GDBCPUDMC extends AbstractDMContext 
	implements ICPUDMContext
	{
		/**
		 * String ID that is used to identify the thread in the GDB/MI protocol.
		 */
		private final String fId;

		/**
		 */
        protected GDBCPUDMC(String sessionId, IHardwareTargetDMContext targetDmc, String id) {
            super(sessionId, targetDmc == null ? new IDMContext[0] : new IDMContext[] { targetDmc });
            fId = id;
        }

		public String getId(){
			return fId;
		}

		@Override
		public String toString() { return baseToString() + ".CPU[" + fId + "]"; }  //$NON-NLS-1$ //$NON-NLS-2$

		@Override
		public boolean equals(Object obj) {
			return baseEquals(obj) && ((GDBCPUDMC)obj).fId.equals(fId);
		}

		@Override
		public int hashCode() { return baseHashCode() ^ fId.hashCode(); }
	}

    @Immutable
	protected static class GDBCoreDMC extends AbstractDMContext
	implements ICoreDMContext
	{
		private final String fId;

		public GDBCoreDMC(String sessionId, ICPUDMContext CPUDmc, String id) {
			super(sessionId, CPUDmc == null ? new IDMContext[0] : new IDMContext[] { CPUDmc });
			fId = id;
		}

		public String getId(){ return fId; }

		@Override
		public String toString() { return baseToString() + ".core[" + fId + "]"; }  //$NON-NLS-1$ //$NON-NLS-2$

		@Override
		public boolean equals(Object obj) {
			return baseEquals(obj) && 
			       (((GDBCoreDMC)obj).fId == null ? fId == null : ((GDBCoreDMC)obj).fId.equals(fId));
		}

		@Override
		public int hashCode() { return baseHashCode() ^ (fId == null ? 0 : fId.hashCode()); }
	}
	
    @Immutable
    protected static class GDBCPUDMData implements ICPUDMData {
    	final int fNumCores;
    	
    	public GDBCPUDMData(int num) {
    		fNumCores = num;
    	}
    	
		public int getNumCores() { return fNumCores; }
    }
    
    @Immutable
    protected static class GDBCoreDMData implements ICoreDMData {
    	final String fPhysicalId;
    	
    	public GDBCoreDMData(String id) {
    		fPhysicalId = id;
    	}
    	
		public String getPhysicalId() { return fPhysicalId; }
    }


    private IGDBControl fCommandControl;
    private IGDBBackend fBackend;
    private CommandFactory fCommandFactory;
	private CommandCache fCommandCache;
	
	// The list of cores should not change, so we can store
	// it once we figured it out.
	private ICPUDMContext[] fCPUs;
    private ICoreDMContext[] fCores;


    public GDBHardware(DsfSession session) {
    	super(session);
    }

    /**
     * This method initializes this service.
     * 
     * @param requestMonitor
     *            The request monitor indicating the operation is finished
     */
    @Override
    public void initialize(final RequestMonitor requestMonitor) {
    	super.initialize(new RequestMonitor(ImmediateExecutor.getInstance(), requestMonitor) {
    		@Override
    		protected void handleSuccess() {
    			doInitialize(requestMonitor);
			}
		});
	}
	
	/**
	 * This method initializes this service after our superclass's initialize()
	 * method succeeds.
	 * 
	 * @param requestMonitor
	 *            The call-back object to notify when this service's
	 *            initialization is done.
	 */
	private void doInitialize(RequestMonitor requestMonitor) {
        
		fCommandControl = getServicesTracker().getService(IGDBControl.class);
    	fBackend = getServicesTracker().getService(IGDBBackend.class);

        fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();

        fCommandCache = new CommandCache(getSession(), fCommandControl);
        fCommandCache.setContextAvailable(fCommandControl.getContext(), true);

		// Register this service.
		register(new String[] { IGDBHardware.class.getName(),
				                GDBHardware.class.getName() },
				 new Hashtable<String, String>());
        
		requestMonitor.done();
	}


	/**
	 * This method shuts down this service. It unregisters the service, stops
	 * receiving service events, and calls the superclass shutdown() method to
	 * finish the shutdown process.
	 * 
	 * @return void
	 */
	@Override
	public void shutdown(RequestMonitor requestMonitor) {
		unregister();
		super.shutdown(requestMonitor);
	}
	
	/**
	 * @return The bundle context of the plug-in to which this service belongs.
	 */
	@Override
	protected BundleContext getBundleContext() {
		return GdbPlugin.getBundleContext();
	}

	public void getCPUs(IHardwareTargetDMContext context, DataRequestMonitor<ICPUDMContext[]> rm) {
		if (fCPUs != null) {
			rm.done(fCPUs);
			return;
		}
		
		if (fBackend.getSessionType() == SessionType.REMOTE) {
			// Until we can get /proc/cpuinfo from the remote, we can't do anything
			fCPUs = new ICPUDMContext[0];
			rm.done(fCPUs);
		} else {
			// For a local session, let's use /proc/cpuinfo on linux
			if (Platform.getOS().equals(Platform.OS_LINUX)) {
				rm.done(fCPUs);
			} else {
				// No way to know the CPUs on a local Windows session.
				fCPUs = new ICPUDMContext[0];
				rm.done(fCPUs);
			}
		}
	}

	public void getCores(IDMContext dmc, final DataRequestMonitor<ICoreDMContext[]> rm) {
		if (dmc instanceof ICPUDMContext) {
			// Get the cores under this particular CPU
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, NOT_SUPPORTED, "Operation not supported", null)); //$NON-NLS-1$
		} else if (dmc instanceof IHardwareTargetDMContext) {
			final IHardwareTargetDMContext targetDmc = (IHardwareTargetDMContext)dmc;
			
			// Get all the cores for this target
			// We already know the list of cores.  Just return it.
			if (fCores != null) {
				rm.done(fCores);
				return;
			}
			
			if (fBackend.getSessionType() == SessionType.REMOTE) {
				// For a remote session, we can use GDB's -list-thread-groups --available
				// command, which shows on which cores a process is running.  This does
				// not necessarily give the exhaustive list of cores, but that is the best
				// we have right now.
				fCommandControl.queueCommand(
						fCommandFactory.createMIListThreadGroups(fCommandControl.getContext(), true),
						new DataRequestMonitor<MIListThreadGroupsInfo>(ImmediateExecutor.getInstance(), rm) {
							@Override
							protected void handleSuccess() {
								// First extract the string id for every core GDB reports
								Set<String> coreIds = new HashSet<String>();
								IThreadGroupInfo[] groups = getData().getGroupList();
								for (IThreadGroupInfo group : groups) {
									coreIds.addAll(Arrays.asList(group.getCores()));
								}
								
								// Now create the context for each distinct core
								//
								// Note that until we support CPUs, let's put them all under
								// a single CPU
								ICPUDMContext cpuDmc = new GDBCPUDMC(getSession().getId(), targetDmc, "0"); //$NON-NLS-1$
								Set<ICoreDMContext> coreDmcs = new HashSet<ICoreDMContext>();
								for (String id : coreIds) {
									coreDmcs.add(new GDBCoreDMC(getSession().getId(), cpuDmc, id));
								}
								fCores = coreDmcs.toArray(new ICoreDMContext[coreDmcs.size()]);
								
								rm.done(fCores);
							}
						});
			} else {
				// For a local session, -list-thread-groups --available does not return
				// the cores field.  Let's use /proc/cpuinfo on linux instead
				if (Platform.getOS().equals(Platform.OS_LINUX)) {
					rm.done(fCores);
				} else {
					// No way to know the cores on a local Windows session.
					fCores = new ICoreDMContext[0];
					rm.done(fCores);
				}
			}
		} else {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid DMC type", null)); //$NON-NLS-1$
		}
	}

	public void getExecutionData(IDMContext dmc, DataRequestMonitor<IDMData> rm) {
		if (dmc instanceof ICoreDMContext) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Not done yet", null)); //$NON-NLS-1$
		} else if (dmc instanceof ICPUDMContext) {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Not done yet", null)); //$NON-NLS-1$

		} else {
			rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, INVALID_HANDLE, "Invalid DMC type", null)); //$NON-NLS-1$
		}
	}
	
	public void flushCache(IDMContext context) {
		fCPUs = null;
		fCores = null;
		fCommandCache.reset(context);
	}
}
