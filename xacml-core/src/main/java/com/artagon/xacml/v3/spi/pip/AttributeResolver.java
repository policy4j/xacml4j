package com.artagon.xacml.v3.spi.pip;

import java.util.Map;

import com.artagon.xacml.v3.BagOfAttributeValues;

public interface AttributeResolver 
{
	/**
	 * Gets a descriptor {@link AttributeResolverDescriptor}
	 * for this resolver
	 * 
	 * @return {@link AttributeResolverDescriptor}
	 */
	AttributeResolverDescriptor getDescriptor();
	
	
	Map<String, BagOfAttributeValues> resolve(
			PolicyInformationPointContext context, BagOfAttributeValues ...keys) throws Exception;
}
