package com.artagon.xacml.v30.pdp;

import org.w3c.dom.Node;

import com.artagon.xacml.v30.AttributeCategory;
import com.artagon.xacml.v30.AttributeValueType;
import com.artagon.xacml.v30.BagOfAttributeValues;

public interface RequestContextCallback 
{
	BagOfAttributeValues getAttributeValue(
			AttributeCategory category, 
			String attributeId, 
			AttributeValueType dataType, 
			String issuer);
	
	Node getContent(AttributeCategory category);
}
