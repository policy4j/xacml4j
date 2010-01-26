package com.artagon.xacml.policy;

import javax.xml.xpath.XPath;

import com.artagon.xacml.CategoryId;


public interface AttributeResolutionService 
{

	BagOfAttributes<?> resolve(CategoryId category,
			String attributeId, AttributeType dataType, 
			String issuer);
	
	BagOfAttributes<?> resolve(CategoryId category, 
			XPath location, AttributeType dataType);
}
