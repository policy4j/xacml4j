package com.artagon.xacml.v3.policy;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.artagon.xacml.v3.AttributeCategoryId;

public interface AttributeResolver 
{
				
	NodeList evaluateToNodeList(
			EvaluationContext context,
			String xpath, AttributeCategoryId categoryId) 
		throws EvaluationException;
	
	String evaluateToString(
			EvaluationContext context,
			String path, AttributeCategoryId categoryId) 
		throws EvaluationException;
	
	Node evaluateToNode(
			EvaluationContext context,
			String path, AttributeCategoryId categoryId) 
		throws EvaluationException;
	
	Number evaluateToNumber(
			EvaluationContext context,
			String path, AttributeCategoryId categoryId) 
		throws EvaluationException;

	
	BagOfAttributeValues<AttributeValue> resolve(
			EvaluationContext context,
			AttributeCategoryId categoryId, 
			String attributeId, 
			AttributeValueType dataType,
			String issuer) throws EvaluationException;	
}
