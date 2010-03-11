package com.artagon.xacml.v3.policy.spi;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public interface XPathProvider 
{
	NodeList evaluateToNodeSet(String path, Node context) 
		throws XPathEvaluationException;
	
	String evaluateToString(String path, Node context) 
		throws XPathEvaluationException;
	
	Node evaluateToNode(String path, Node context) 
		throws XPathEvaluationException;
	
	Number evaluateToNumber(String path, Node context) 
		throws XPathEvaluationException;
	
}
