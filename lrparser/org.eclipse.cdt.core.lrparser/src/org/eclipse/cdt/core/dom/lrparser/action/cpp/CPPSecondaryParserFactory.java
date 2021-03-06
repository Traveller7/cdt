/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser.action.cpp;

import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.lrparser.ISecondaryParser;
import org.eclipse.cdt.core.dom.lrparser.action.ITokenStream;
import org.eclipse.cdt.internal.core.dom.lrparser.cpp.CPPExpressionParser;
import org.eclipse.cdt.internal.core.dom.lrparser.cpp.CPPNoCastExpressionParser;
import org.eclipse.cdt.internal.core.dom.lrparser.cpp.CPPNoFunctionDeclaratorParser;
import org.eclipse.cdt.internal.core.dom.lrparser.cpp.CPPSizeofExpressionParser;
import org.eclipse.cdt.internal.core.dom.lrparser.cpp.CPPTemplateTypeParameterParser;

public class CPPSecondaryParserFactory implements ICPPSecondaryParserFactory {

	
	private static final CPPSecondaryParserFactory DEFAULT_INSTANCE = new CPPSecondaryParserFactory();
	
	public static CPPSecondaryParserFactory getDefault() {
		return DEFAULT_INSTANCE;
	}
	
	
	public ISecondaryParser<ICPPASTTemplateParameter> getTemplateTypeParameterParser(ITokenStream stream, Map<String,String> properties) {
		return new CPPTemplateTypeParameterParser(stream, properties);
	}
	
	public ISecondaryParser<IASTDeclarator> getNoFunctionDeclaratorParser(ITokenStream stream, Map<String,String> properties) {
		return new CPPNoFunctionDeclaratorParser(stream, properties); 
	}

	public ISecondaryParser<IASTExpression> getExpressionParser(ITokenStream stream, Map<String,String> properties) {
		return new CPPExpressionParser(stream, properties);
	}

	public ISecondaryParser<IASTExpression> getNoCastExpressionParser(ITokenStream stream, Map<String,String> properties) {
		return new CPPNoCastExpressionParser(stream, properties);
	}

	public ISecondaryParser<IASTExpression> getSizeofExpressionParser(ITokenStream stream, Map<String,String> properties) {
		return new CPPSizeofExpressionParser(stream, properties);
	}
	
}
