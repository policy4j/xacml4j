package com.artagon.xacml.v3.policy;

import javax.xml.xpath.XPath;

import com.artagon.xacml.v3.CategoryId;


public interface AttributeResolutionService 
{

	BagOfAttributeValues<?> resolve(CategoryId category,
			String attributeId, AttributeValueType dataType, 
			String issuer);
	
	BagOfAttributeValues<?> resolve(CategoryId category, 
			XPath location, AttributeValueType dataType);
}
