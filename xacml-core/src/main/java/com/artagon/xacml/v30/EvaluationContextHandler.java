package com.artagon.xacml.v30;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public interface EvaluationContextHandler 
{
	NodeList evaluateToNodeSet(
			EvaluationContext context,
			String xpath, 
			AttributeCategory categoryId) 
		throws EvaluationException;
	
	
	String evaluateToString(
			EvaluationContext context,
			String path, 
			AttributeCategory categoryId) 
		throws EvaluationException;
	
	Node evaluateToNode(
			EvaluationContext context,
			String path, 
			AttributeCategory categoryId) 
		throws EvaluationException;
	
	Number evaluateToNumber(
			EvaluationContext context,
			String path, 
			AttributeCategory categoryId) 
		throws EvaluationException;
	
	BagOfAttributeValues resolve(
			EvaluationContext context,
			AttributeDesignatorKey key) throws EvaluationException;
	
	BagOfAttributeValues resolve(
			EvaluationContext context,
			AttributeSelectorKey key) throws EvaluationException;
}
