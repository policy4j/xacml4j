package com.artagon.xacml.v3.policy;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.AttributeCategoryId;

public interface ContextHandler 
{
	
	/**
	 * Gets content for a given attribute category
	 * 
	 * @param categoryId an attribute category
	 * @return an instance of {@link Node} or
	 * <code>null</code> if content is not available
	 * for a given category 
	 */
	Node getContent(AttributeCategoryId categoryId);
	
	BagOfAttributeValues<AttributeValue> resolve(
			EvaluationContext context, 
			AttributeCategoryId categoryId,
			String attributeId, 
			AttributeValueType dataType, 
			String issuer) throws EvaluationException;	
}
