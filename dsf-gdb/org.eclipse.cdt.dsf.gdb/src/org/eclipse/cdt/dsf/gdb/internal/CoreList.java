/*******************************************************************************
 * Copyright (c) 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Vector;

import org.eclipse.cdt.internal.core.ICoreInfo;

/**
 */
public class CoreList {

	private class CoreInfo implements ICoreInfo {
		private String fId;
		private String fPhysicalId;
		
		public CoreInfo(String id, String pId) {
			fId = id;
			fPhysicalId = pId;
		}

		public String getId() {
			return fId;
		}

		public String getPhysicalId() {
			return fPhysicalId;
		}		
	}

	private ICoreInfo[] fCoreList;
	
	public CoreList() {
	}
	
	/**
	 * Returns the list of cores as shown in /proc/cpuinfo
	 * This method will only parse /proc/cpuinfo once and cache
	 * the result.  To force a re-parse, one must create a new
	 * CoreList object. 
	 */
	public ICoreInfo[] getCoreList()  {
		if (fCoreList != null) {
			return fCoreList;
		}
		
		File cpuInfo = new File("/proc/cpuinfo"); //$NON-NLS-1$

		Vector<ICoreInfo> coreInfo = new Vector<ICoreInfo>();
        BufferedReader reader = null;
        try {
        	String physicalId = null;
        	String coreId = null;
        	
            Reader r = new InputStreamReader(new FileInputStream(cpuInfo));
            reader = new BufferedReader(r);
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("physical id")) { //$NON-NLS-1$
                	// Found the physical id of this core, so store it temporarily
                	physicalId = line.split(":")[1].trim();  //$NON-NLS-1$
                } else if (line.startsWith("core id")) { //$NON-NLS-1$
                	// Found core id of this core which come after the entry
                	// for physical id, so we have both by now.
                	coreId = line.split(":")[1].trim(); //$NON-NLS-1$
                	coreInfo.add(new CoreInfo(coreId, physicalId));
                	
                	// Get ready to look for the next core.
                	physicalId = null;
                	coreId = null;
                }
            }            
		} catch (IOException e) {
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {/* Don't care */}
			reader = null;
		}
        
		fCoreList = coreInfo.toArray(new ICoreInfo[coreInfo.size()]);
		return fCoreList;
	}
}
