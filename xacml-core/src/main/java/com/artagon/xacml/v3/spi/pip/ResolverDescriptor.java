package com.artagon.xacml.v3.spi.pip;

import java.util.List;

import com.artagon.xacml.v3.AttributeCategory;
import com.artagon.xacml.v3.AttributeReferenceKey;

public interface ResolverDescriptor 
{	
	/**
	 * An unique identifier for
	 * this attribute resolver 
	 * 
	 * @return an unique identifier
	 */
	String getId();
	
	/**
	 * Gets resolver name
	 * 
	 * @return resolver name
	 */
	String getName();
	
	/**
	 * Gets resolver category
	 * 
	 * @return {@link AttributeCategory}
	 */
	AttributeCategory getCategory();
	
	/**
	 * Gets key references for a resolver
	 * 
	 * @return list of {@link AttributeReferenceKey}
	 */
	List<AttributeReferenceKey> getKeyRefs();
	
	/**
	 * Test if attributes resolved by resolver
	 * can be cached by PIP
	 * 
	 * @return <code>true</code> if attributes can be cached
	 */
	boolean isCachable();

	/**
	 * Gets preferred cache TTL for an attributes resolved
	 * via this resolver
	 * 
	 * @return a TTL in seconds or <code>0</code>
	 */
	int getPreferreredCacheTTL();
}
