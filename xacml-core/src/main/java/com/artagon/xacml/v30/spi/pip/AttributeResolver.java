package com.artagon.xacml.v30.spi.pip;


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
			ResolverContext context) throws Exception;
}
