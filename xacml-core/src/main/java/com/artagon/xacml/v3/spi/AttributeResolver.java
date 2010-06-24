package com.artagon.xacml.v3.spi;

import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;

public interface AttributeResolver 
{
	AttributeResolverDescriptor getDescriptor();
	
	BagOfAttributeValues<? extends AttributeValue> resolve(AttributeDesignator ref, 
			AttributesCallback callback);
}
