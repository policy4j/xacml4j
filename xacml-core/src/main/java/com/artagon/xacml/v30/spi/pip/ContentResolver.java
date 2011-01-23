package com.artagon.xacml.v30.spi.pip;


public interface ContentResolver 
{
	/**
	 * Gets content resolver descriptor
	 * 
	 * @return a content resolver descriptor
	 */
	ContentResolverDescriptor getDescriptor();
	
	/**
	 * Resolves a content
	 * 
	 * @param context a policy information point context
	 * @return {@link Content} or <code>null</code>
	 * @throws Exception if an error occurs while resolving content
	 */
	Content resolve(ResolverContext context) throws Exception;
}
