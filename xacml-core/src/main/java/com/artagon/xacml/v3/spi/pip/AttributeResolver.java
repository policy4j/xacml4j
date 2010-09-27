package com.artagon.xacml.v3.spi.pip;

import com.artagon.xacml.v3.AttributeCategory;
import com.artagon.xacml.v3.AttributeDesignator;
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
	 BagOfAttributeValues resolve(
			PolicyInformationPointContext context,
			AttributeCategory category,
			String attributeId,
			AttributeValueType dataType,
			String issuer) throws Exception;

}
