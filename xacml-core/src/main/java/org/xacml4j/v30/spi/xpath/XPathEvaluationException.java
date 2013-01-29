package org.xacml4j.v30.spi.xpath;

import org.w3c.dom.Node;
import org.xacml4j.v30.XPathVersion;



public class XPathEvaluationException extends Exception
{
	private static final long serialVersionUID = -485015612384300131L;

	public XPathEvaluationException(String xpathExpression, 
			XPathVersion version, Node node, Throwable e){
		super(getMessage(e.getMessage(), xpathExpression, version, node), e);
	}
	
	public XPathEvaluationException(String message, String xpathExpression, 
			XPathVersion version, Node node){
		super(getMessage(message, xpathExpression, version, node));
	}
	
	private static String getMessage(String m, String xpathExpression, 
			XPathVersion version, Node context){
		return String.format("Failed to evaluate xpath, reason=\"%s\", " +
				"XPathExpression=\"%s\", XPathVersion=\"%s\", Context =\"{%s}:{%s}\"", 
				m, xpathExpression, version, context.getNamespaceURI(), context.getLocalName());
	}
}
