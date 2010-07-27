package com.artagon.xacml.v3.spi.pip;

import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.RequestAttributesCallback;

public interface AttributeResolver 
{
	/**
	 * Gets attribute resolver descriptor
	 * 
	 * @return {@link AttributeResolverDescriptor}
	 */
	AttributeResolverDescriptor getDescriptor();
	
	BagOfAttributeValues<? extends AttributeValue> resolve(
			PolicyInformationPointContext context,
			AttributeDesignator ref, 
			RequestAttributesCallback callback);
	
	boolean canResolve(AttributeDesignator ref);
}
