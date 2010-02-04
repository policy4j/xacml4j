package com.artagon.xacml.v3.policy;

import javax.xml.xpath.XPath;

import com.artagon.xacml.v3.AttributeCategoryId;


public interface AttributeResolutionService 
{

	BagOfAttributeValues<?> resolve(AttributeCategoryId category,
			String attributeId, AttributeValueType dataType, 
			String issuer);
	
	BagOfAttributeValues<?> resolve(AttributeCategoryId category, 
			XPath location, AttributeValueType dataType);
}
