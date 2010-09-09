package com.artagon.xacml.v3.spi.pip;

import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeReferenceEvaluationException;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.RequestContextAttributesCallback;

public interface AttributeResolver 
{
	/**
	 * Gets attribute resolver descriptor
	 * 
	 * @return {@link AttributeResolverDescriptor}
	 */
	AttributeResolverDescriptor getDescriptor();
	
	/**
	 * Resolves given {@link AttributeDesignator} to
	 * a 
	 * @param context
	 * @param ref
	 * @param callback
	 * @return
	 * @exception {@link AttributeReferenceEvaluationException}
	 */
	BagOfAttributeValues<AttributeValue> resolve(
			PolicyInformationPointContext context,
			AttributeDesignator ref, 
			RequestContextAttributesCallback callback) 
				throws Exception;
	
	/**
	 * Tests if this resolver is capable of resolving
	 * given attribute desiginator
	 * 
	 * @param ref an attribute designator
	 * @return <code>true</code> if this resolver
	 * can resolve given designator
	 */
	boolean canResolve(AttributeDesignator ref);
}
