package com.artagon.xacml.v3.spi.pip;

import com.artagon.xacml.v3.AttributeCategory;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;

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
	
	
	AttributeCategory getCategory();
	
	/**
	 * Resolves resolver keys via given evaluation context
	 * 
	 * @param context an evaluation context
	 * @return an array of resolved keys
	 * @throws EvaluationException if an error occurs
	 */
	BagOfAttributeValues[] resolveKeys(EvaluationContext context) 
		throws EvaluationException;
	
	
	/**
	 * Test if attributes resolved by resolver
	 * can be cached by PIP
	 * 
	 * @return <code>true</code> if attributes can be cached
	 */
	boolean isCachingEnabled();
	
	
	/**
	 * Gets preferred cache TTL for an attributes resolved
	 * via this resolver
	 * 
	 * @return a TTL in milliseconds or <code>-1</code>
	 */
	long getPreferreredCacheTTL();
}
