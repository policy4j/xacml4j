package com.artagon.xacml.v30.pdp;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.artagon.xacml.v30.AttributeCategory;
import com.artagon.xacml.v30.AttributeDesignatorKey;
import com.artagon.xacml.v30.AttributeSelectorKey;
import com.artagon.xacml.v30.BagOfAttributeExp;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.EvaluationException;


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
	
	BagOfAttributeExp resolve(
			EvaluationContext context,
			AttributeDesignatorKey key) throws EvaluationException;
	
	BagOfAttributeExp resolve(
			EvaluationContext context,
			AttributeSelectorKey key) throws EvaluationException;
}
