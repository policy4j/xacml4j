package org.xacml4j.v30;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
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

import javax.xml.xpath.XPathExpressionException;

public class XPathEvaluationException extends Exception
{
	private static final long serialVersionUID = -485015612384300131L;

	private XPathEvaluationException(
			String message, Throwable throwable){
		super(message, throwable);
	}

	public static String formatMessage(String xpath, String message, Node context){
		return String.format("Failed to evaluate XPath=\"%s\" at XmlNode =\"{%s}:{%s}\" cause: %s",
				xpath,
				(context != null)?context.getNamespaceURI():null,
				(context != null)?context.getLocalName():null,
				message);
	}

	public static XPathEvaluationException wrap(String xpath, Node content, XPathExpressionException e){
		return new XPathEvaluationException(formatMessage(xpath, e.getMessage(), content), e);
	}
}
