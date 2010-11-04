package com.artagon.xacml.v3.spi.pip;

import com.artagon.xacml.v3.AttributeCategory;
import com.artagon.xacml.v3.AttributeReferenceKey;
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
	
	/**
	 * Gets resolver category
	 * 
	 * @return {@link AttributeCategory}
	 */
	AttributeCategory getCategory();
	
	/**
	 * Resolves keys via given evaluation context
	 * 
	 * @param context an evaluation context
	 * @return an array of resolved keys
	 * @throws EvaluationException if an error occurs
	 */
	BagOfAttributeValues[] resolveKeys(EvaluationContext context) 
		throws EvaluationException;
	
	AttributeReferenceKey getKeyAt(int index);
	
	int getKeysCount();
	
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
