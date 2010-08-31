package com.artagon.xacml.v3.spi.pip;

import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.RequestContextAttributesCallback;

public interface AttributeResolver 
{
	/**
	 * Gets attribute resolver descriptor
	 * 
	 * @return {@link AttributeResolverDescriptor}
	 */
	AttributeResolverDescriptor getDescriptor();
	
	BagOfAttributeValues<AttributeValue> resolve(
			PolicyInformationPointContext context,
			AttributeDesignator ref, 
			RequestContextAttributesCallback callback);
	
	boolean canResolve(AttributeDesignator ref);
}
