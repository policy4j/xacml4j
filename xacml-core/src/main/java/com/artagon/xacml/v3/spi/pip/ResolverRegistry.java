package com.artagon.xacml.v3.spi.pip;

import com.artagon.xacml.v3.AttributeCategory;
import com.artagon.xacml.v3.AttributeDesignatorKey;
import com.artagon.xacml.v3.EvaluationContext;

public interface ResolverRegistry 
{
	AttributeResolver getAttributeResolver(
			EvaluationContext context, AttributeDesignatorKey key);
	
	ContentResolver getContentResolver(
			EvaluationContext contetx, AttributeCategory category);
	
	void addResolver(AttributeResolver r);
	
	void addResolver(ContentResolver r);
	
	void addResolver(String policyId, AttributeResolver r);
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
