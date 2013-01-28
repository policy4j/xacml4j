package org.xacml4j.v30;

import java.util.Collection;


public interface AttributeExpType  extends ValueType
{
	/**
	 * Gets data type identifier
	 *
	 * @return data type identifier
	 */
	String getDataTypeId();

	/**
	 * Parses given XACML string representation of an attribute
	 * value to an actual {@link BaseAttribute} of this type
	 *
	 * @param v an XACML string value representation of this type
	 * @param params an additional parameters used to create
	 * value of this type from given string
	 * @return {@link BaseAttribute} instance of this type
	 * @exception IllegalArgumentException if given value does not
	 * represent value of this type
	 */
	AttributeExp fromXacmlString(String v, Object ...params);

	/**
	 * Creates an attribute from a given object.
	 * Object can be instance of {@link this#getValueClazz()}
	 * or can be instance of {@link String}
	 *
	 * @param object an object
	 * @param params  an additional parameters used to
	 * create a value of this type
	 * @return {@link AttributeExp}
	 * @exception IllegalArgumentException if attribute value of
	 * this type can not be created from a given value
	 * and parameters
	 */
	AttributeExp create(Object object, Object ...params);

	/**
	 * Creates type representing collection of
	 * attribute values of this
	 * data type
	 *
	 * @return {@link BagOfAttributeExpType} instance
	 */
	BagOfAttributeExpType bagType();

	BagOfAttributeExp.Builder bag();

	/**
	 * Creates a bag from the given array of
	 * {@link AttributeExp} instances
	 *
	 * @param values an array of bag values
	 * @return {@link BagOfAttributeExp}
	 * @exception IllegalArgumentException if bag
	 * can not be created from a given values
	 */
	BagOfAttributeExp bagOf(AttributeExp ...values);


	/**
	 * Creates a bag from the given collection of
	 * {@link AttributeExp} instances
	 *
	 * @param values a collection of values
	 * @return {@link BagOfAttributeExp}
	 * @exception IllegalArgumentException if bag
	 * can not be created from a given values
	 */
	BagOfAttributeExp bagOf(Collection<AttributeExp> values);

	/**
	 * Creates a bag from the given array of
	 * {@link AttributeExp} instances
	 *
	 * @param values an array of attribute values
	 * @return {@link BagOfAttributeExp}
	 * @exception IllegalArgumentException if bag
	 * can not be created from a given values
	 */
	BagOfAttributeExp bagOf(Object ...values);

	/**
	 * Creates an empty bag
	 *
	 * @return {@link BagOfAttributeExp} an empty
	 * bag
	 */
	BagOfAttributeExp emptyBag();
}
