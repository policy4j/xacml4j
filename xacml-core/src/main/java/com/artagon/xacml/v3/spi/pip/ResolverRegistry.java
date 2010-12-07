package com.artagon.xacml.v3.spi.pip;

import com.artagon.xacml.v3.AttributeCategory;
import com.artagon.xacml.v3.AttributeDesignatorKey;
import com.artagon.xacml.v3.EvaluationContext;

public interface ResolverRegistry 
{
	/**
	 * Gets an {@link AttributeResolver} for a given
	 * evaluation context and given {@link AttributeDesignatorKey}
	 * 
	 * @param context
	 * @param key
	 * @return
	 */
	AttributeResolver getAttributeResolver(
			EvaluationContext context, AttributeDesignatorKey key);
	
	
	/**
	 * Gets an {@link ContentResolver} for a given
	 * evaluation context and given {@link AttributeCategory}
	 * 
	 * @param context
	 * @param key
	 * @return
	 */
	ContentResolver getContentResolver(
			EvaluationContext contetx, AttributeCategory category);
	
	/**
	 * Adds top level attribute resolver
	 * 
	 * @param r a top level attribute resolver
	 */
	void addResolver(AttributeResolver r);
	
	
	/**
	 * Adds top level content resolver
	 * 
	 * @param r a top level content resolver
	 */
	void addResolver(ContentResolver r);
	
	/**
	 * Adds an attribute resolver bound to the specific
	 * policy identifier and all child policies
	 * 
	 * @param policyId a policy identifier
	 * @param r an attribute resolver
	 */
	void addResolver(String policyId, AttributeResolver r);
	
	/**
	 * Adds a content resolver bound to the specific
	 * policy identifier and all child policies
	 * 
	 * @param policyId a policy identifier
	 * @param r a content resolver
	 */
	void addResolver(String policyId, ContentResolver r);
	
	/**
	 * Gets attribute resolve by identifier
	 * @param id a resolver identifier
	 * @return {@link AttributeResolver} or <code>null</code>
	 * if not resolver found
	 */
	AttributeResolver getAttributeResolver(String id);
	
	/**
	 * Gets content resolver by identifier
	 * @param id a resolver identifier
	 * @return {@link ContentResolver} or <code>null</code>
	 * if not resolver found
	 */
	ContentResolver getContentResolver(String id);
}
