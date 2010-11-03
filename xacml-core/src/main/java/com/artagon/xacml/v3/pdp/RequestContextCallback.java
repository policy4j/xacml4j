package com.artagon.xacml.v3.pdp;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.AttributeCategory;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;

public interface RequestContextCallback 
{
	BagOfAttributeValues getAttributeValue(
			AttributeCategory category, 
			String attributeId, 
			AttributeValueType dataType, 
			String issuer);
	
	Node getContent(AttributeCategory category);
}
