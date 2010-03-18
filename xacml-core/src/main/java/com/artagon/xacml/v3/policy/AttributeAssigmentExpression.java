package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;

public interface AttributeAssigmentExpression extends PolicyElement
{

	/**
	 * Gets attribute identifier
	 * 
	 * @return attribute identifier
	 */
	String getAttributeId();

	/**
	 * An optional category of the attribute. 
	 * If category is not specified, the attribute has no category
	 * 
	 * @return category identifier or <code>null</code>
	 */
	AttributeCategoryId getCategory();

	/**
	 * Gets an issuer of the attribute.
	 * If issuer is not specified, the attribute
	 * has not issuer
	 * 
	 * @return attribute issuer or <code>null</code>
	 */
	String getIssuer();

	AttributeValue evaluate(EvaluationContext context)
			throws EvaluationException;

}