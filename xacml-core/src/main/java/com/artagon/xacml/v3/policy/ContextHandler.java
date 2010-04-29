package com.artagon.xacml.v3.policy;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.AttributeCategoryId;

public interface ContextHandler 
{
	
	Node getContent(AttributeCategoryId categoryId);
	
	BagOfAttributeValues<AttributeValue> resolve(
			EvaluationContext context,
			AttributeCategoryId categoryId, 
			String attributeId, 
			AttributeValueType dataType,
			String issuer) throws EvaluationException;	
}
