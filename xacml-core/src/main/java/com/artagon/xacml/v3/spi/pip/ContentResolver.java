package com.artagon.xacml.v3.spi.pip;

import org.w3c.dom.Node;

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
	 * @return {@link Node} or <code>null</code>
	 * @throws Exception if an error occurs while resolving content
	 */
	Node resolve(PolicyInformationPointContext context) throws Exception;
}
