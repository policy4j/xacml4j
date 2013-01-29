package org.xacml4j.v30.spi.pip;


public interface AttributeResolver 
	extends AttributeResolverMBean
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
