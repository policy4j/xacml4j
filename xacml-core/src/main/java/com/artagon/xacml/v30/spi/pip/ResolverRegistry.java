package com.artagon.xacml.v30.spi.pip;

import com.artagon.xacml.v30.AttributeDesignatorKey;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.core.AttributeCategory;

public interface ResolverRegistry 
{
	/**
	 * Gets a matching {@link AttributeResolver} for a given
	 * evaluation context and given {@link AttributeDesignatorKey}
	 * 
	 * @param context an evaluation context
	 * @param key an attribute designator key
	 * @return instance of {@link AttributeResolver}
	 * or <ii>null</ii> if not matching resolver is found
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
	void addAttributeResolver(AttributeResolver r);
	
	void addAttributeResolvers(Iterable<AttributeResolver> resolvers);
	void addAttributeResolvers(String policyId, Iterable<AttributeResolver> resolvers);
	
	void addContentResolvers(Iterable<ContentResolver> resolvers);
	void addContentResolvers(String policyId, Iterable<ContentResolver> resolvers);
	
	/**
	 * Adds top level content resolver
	 * 
	 * @param r a top level content resolver
	 */
	void addContentResolver(ContentResolver r);
	
	/**
	 * Adds an attribute resolver bound to the specific
	 * policy identifier and all child policies
	 * 
	 * @param policyId a policy identifier
	 * @param r an attribute resolver
	 */
	void addAttributeResolver(String policyId, AttributeResolver r);
	
	/**
	 * Adds a content resolver bound to the specific
	 * policy identifier and all child policies
	 * 
	 * @param policyId a policy identifier
	 * @param r a content resolver
	 */
	void addContentResolver(String policyId, ContentResolver r);
	
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
