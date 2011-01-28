package com.artagon.xacml.v30.spi.pip;

import org.w3c.dom.Node;

public interface PolicyInformationPointCacheProvider 
{
	/**
	 * Gets content from the cache for a give resolver
	 * and request context keys
	 * 
	 * @param d a content resolver descriptor
	 * @param keys a request context keys
	 * @return {@link Node} or <code>null</code>
	 */
	Content getContent(ResolverContext context);
	
	void putContent(ResolverContext context, Content content);
	
	AttributeSet getAttributes(ResolverContext context);
	
	void putAttributes(ResolverContext context, AttributeSet v);
}
