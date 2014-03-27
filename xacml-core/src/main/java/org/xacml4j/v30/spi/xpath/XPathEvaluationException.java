package org.xacml4j.v30.spi.xpath;

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
