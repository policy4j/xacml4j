package com.artagon.xacml.v3.policy;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.artagon.xacml.v3.AttributeCategoryId;

public interface AttributeResolver 
{
	/**
	 * Resolves an attribute by given category, attribute id,
	 * an attribute data type and issuer
	 * 
	 * @param category an attribute category
	 * @param attributeId an attribute identifier
	 * @param dataType an attribute data type
	 * @param issuer an attribute issuer
	 * @return {@link BagOfAttributeValues}
	 */
	BagOfAttributeValues<AttributeValue> resolve(AttributeCategoryId category,
			String attributeId, AttributeValueType dataType, 
			String issuer);
			
	NodeList evaluateToNodeSet(String xpath, AttributeCategoryId categoryId) 
		throws EvaluationException;
	
	String evaluateToString(String path, AttributeCategoryId categoryId) 
		throws EvaluationException;
	
	Node evaluateToNode(String path, AttributeCategoryId categoryId) 
		throws EvaluationException;
	
	Number evaluateToNumber(String path, AttributeCategoryId categoryId) 
		throws EvaluationException;

		
	BagOfAttributeValues<AttributeValue> resolve(
			AttributeSelector selector) throws EvaluationException;
	
}
