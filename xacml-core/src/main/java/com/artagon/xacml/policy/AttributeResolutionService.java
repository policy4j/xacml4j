package com.artagon.xacml.policy;

import javax.xml.xpath.XPath;

import org.oasis.xacml.azapi.constants.AzCategoryId;


public interface AttributeResolutionService 
{

	BagOfAttributes<?> resolve(AzCategoryId category,
			String attributeId, AttributeType dataType, 
			String issuer);
	
	BagOfAttributes<?> resolve(AzCategoryId category, 
			XPath location, AttributeType dataType);
}
