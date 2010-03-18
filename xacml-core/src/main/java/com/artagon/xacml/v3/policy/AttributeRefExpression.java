package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.AttributeCategoryId;

public interface AttributeRefExpression extends Expression 
{
	/**
	 * Governs whether this reference evaluates 
	 * to an empty bag or {@link EvaluationException}
	 * is thrown during this reference evaluation
	 * 
	 * @return <code>true</code> if attribute
	 * must be present
	 */
	boolean isMustBePresent();
	
	/**
	 * Gets bag returned by this reference
	 * attribute XACML primitive data type
	 * 
	 * @return {@link AttributeValueType}
	 */
	AttributeValueType getDataType();

	/**
	 * Gets attribute category.
	 * 
	 * @return attribute category
	 */
	AttributeCategoryId getCategory();

}