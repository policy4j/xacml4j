package com.artagon.xacml.v3.spi.pip;


public interface AttributeResolver 
{
	/**
	 * Gets a descriptor {@link AttributeResolverDescriptor}
	 * for this resolver
	 * 
	 * @return {@link AttributeResolverDescriptor}
	 */
	AttributeResolverDescriptor getDescriptor();
	
	
	AttributeSet resolve(
			PolicyInformationPointContext context) throws Exception;
}
