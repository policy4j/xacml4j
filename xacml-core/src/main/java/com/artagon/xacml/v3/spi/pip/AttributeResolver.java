package com.artagon.xacml.v3.spi.pip;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeDesignator;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.AttributeValueType;
import com.artagon.xacml.v3.BagOfAttributeValues;

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
			AttributeCategoryId category,
			String attributeId,
			AttributeValueType dataType,
			String issuer) 
				throws Exception;
	
	/**
	 * Tests if this resolver is capable of resolving
	 * given attribute
	 * 
	 * @param category an attribute category
	 * @param attributeId an attribute identifier
	 * @param dataType an attribute data type
	 * @param issuer an optional attribute issuer
	 * @return <code>true</code> if this resolver
	 * can resolve given designator
	 */
	boolean canResolve(AttributeCategoryId category,
			String attributeId,
			AttributeValueType dataType,
			String issuer);
}
