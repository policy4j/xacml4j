package com.artagon.xacml.v3.spi.pip;

import org.w3c.dom.Node;

import com.artagon.xacml.v3.BagOfAttributeValues;

public interface ResolverResultCacheProvider 
{
	/**
	 * Gets content from the cache for a give resolver
	 * and request context keys
	 * 
	 * @param d a content resolver descriptor
	 * @param keys a request context keys
	 * @return {@link Node} or <code>null</code>
	 */
	Node get(ContentResolverDescriptor d, BagOfAttributeValues[] keys);
	
	void put(ContentResolverDescriptor d, BagOfAttributeValues[] keys, Node content);
	
	AttributeSet get(AttributeResolverDescriptor d, BagOfAttributeValues[] keys);
	
	void put(AttributeResolverDescriptor d, BagOfAttributeValues[] keys, AttributeSet v);
}
