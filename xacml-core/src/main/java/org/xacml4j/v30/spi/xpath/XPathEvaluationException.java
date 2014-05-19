package org.xacml4j.v30.spi.xpath;

/*
 * #%L
 * Artagon XACML 3.0 Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Artagon
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.w3c.dom.Node;


public class XPathEvaluationException extends Exception
{
	private static final long serialVersionUID = -485015612384300131L;

	public XPathEvaluationException(String xpathExpression,
			Node node, Throwable e){
		super(getMessage(e.getMessage(), xpathExpression, node), e);
	}

	public XPathEvaluationException(String message, 
			String xpathExpression,
			Node node){
		super(getMessage(message, xpathExpression,  node));
	}

	private static String getMessage(String m, String xpathExpression, Node context){
		return String.format("Failed to evaluate xpath - %s, " +
				"XPathExpression=\"%s\", Context =\"{%s}:{%s}\"",
				m, xpathExpression, 
				(context != null)?context.getNamespaceURI():null, 
				(context != null)?context.getLocalName():null);
	}
}
