package com.artagon.xacml.v30.spi.xpath;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.artagon.xacml.v30.XPathVersion;

/**
 * An XPath provider for executing XPath expressions
 * 
 * @author Giedrius Trumpickas
 */
public interface XPathProvider 
{	
	NodeList evaluateToNodeSet(
			XPathVersion version, String path, Node context) 
		throws XPathEvaluationException;
	
	String evaluateToString(
			XPathVersion version, String path, Node context) 
		throws XPathEvaluationException;
	
	Node evaluateToNode(
			XPathVersion version, String path, Node context) 
		throws XPathEvaluationException;
	
	Number evaluateToNumber(
			XPathVersion version, String path, Node context) 
		throws XPathEvaluationException;
	
}
