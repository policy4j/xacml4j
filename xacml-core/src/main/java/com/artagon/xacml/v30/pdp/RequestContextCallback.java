package com.artagon.xacml.v30.pdp;

import org.w3c.dom.Node;

import com.artagon.xacml.v30.AttributeExpType;
import com.artagon.xacml.v30.BagOfAttributesExp;
import com.artagon.xacml.v30.core.AttributeCategory;

public interface RequestContextCallback 
{
	BagOfAttributesExp getAttributeValue(
			AttributeCategory category, 
			String attributeId, 
			AttributeExpType dataType, 
			String issuer);
	
	Node getContent(AttributeCategory category);
}
