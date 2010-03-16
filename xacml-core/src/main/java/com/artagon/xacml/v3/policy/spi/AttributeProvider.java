package com.artagon.xacml.v3.policy.spi;

import java.util.Map;
import java.util.Set;

import com.artagon.xacml.v3.Attribute;
import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.policy.AttributeValue;
import com.artagon.xacml.v3.policy.BagOfAttributeValues;

public interface AttributeProvider 
{
	AttributeCategoryId getCategory();
	
	Set<String> getProvidedAttributes();
	
	BagOfAttributeValues<AttributeValue> getAttribute(
			Map<String, Attribute> contextAttr, String attributeId);
}
