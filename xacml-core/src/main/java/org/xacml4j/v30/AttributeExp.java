package org.xacml4j.v30;

import java.io.Serializable;


public interface AttributeExp
	extends ValueExpression, Serializable
{
	/**
	 * Gets attribute type
	 *
	 * @return {@link AttributeExpType}
	 */
	@Override
	AttributeExpType getType();

	/**
	 * Gets attribute expression value
	 *
	 * @return an attribute expression value
	 */
	Object getValue();

	/**
	 * Creates bag with this attribute
	 * in in the bag
	 *
	 * @return {@link BagOfAttributeExp}
	 */
	BagOfAttributeExp toBag();
	
	/**
	 * Converts this XACML attribute value
	 * to {@link String}
	 *
	 * @return this attribute value as {@link String}
	 */
	//String toXacmlString();

	public interface AttributeExpVisitor extends ExpressionVisitor
	{
		void visitEnter(AttributeExp v);
		void visitLeave(AttributeExp v);
	}
}
