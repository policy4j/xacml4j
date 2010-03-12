package com.artagon.xacml.v3.policy.spi;

import javax.xml.xpath.XPath;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.policy.AttributeValue;
import com.artagon.xacml.v3.policy.AttributeValueType;
import com.artagon.xacml.v3.policy.BagOfAttributeValues;

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
	
	BagOfAttributeValues<AttributeValue> resolve(AttributeCategoryId category, 
			XPath location, AttributeValueType dataType);
	
	Node getContent(AttributeCategoryId categoryId);
}
