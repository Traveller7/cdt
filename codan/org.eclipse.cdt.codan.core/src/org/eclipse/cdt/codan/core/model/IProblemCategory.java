/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

/**
 * Problem category.
 * <p>
* <strong>EXPERIMENTAL</strong>. This class or interface has been added as
* part of a work in progress. There is no guarantee that this API will
* work or that it will remain the same.
* </p>
 * Clients may extend and implement this interface.
 */
public interface IProblemCategory extends IProblemElement {
	/**
	 * Category name
	 */
	String getName();

	/**
	 * Unique category id
	 * @return id
	 */
	String getId();

	/**
	 * Category children (other categories or problems)
	 * @return
	 */
	IProblemElement[] getChildren();

}