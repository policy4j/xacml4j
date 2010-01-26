package com.artagon.xacml.v30.policy;

import javax.xml.xpath.XPath;

import com.artagon.xacml.v30.CategoryId;


public interface AttributeResolutionService 
{

	BagOfAttributeValues<?> resolve(CategoryId category,
			String attributeId, AttributeValueType dataType, 
			String issuer);
	
	BagOfAttributeValues<?> resolve(CategoryId category, 
			XPath location, AttributeValueType dataType);
}
