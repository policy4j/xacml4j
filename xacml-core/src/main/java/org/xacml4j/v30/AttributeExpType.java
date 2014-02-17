package org.xacml4j.v30;



public interface AttributeExpType extends ValueType
{
	/**
	 * Gets data type identifier
	 *
	 * @return data type identifier
	 */
	String getDataTypeId();

	/**
	 * Creates an attribute from a given object.
	 *
	 * @param object an object
	 * @param params  an additional parameters used to
	 * build a value of this type
	 * @return {@link AttributeExp}
	 * @exception IllegalArgumentException if attribute value of
	 * this type can not be created from a given value
	 * and parameters
	 */
//	AttributeExp create(Object object);
	
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
	BagOfAttributeExp bagOf(Iterable<AttributeExp> values);

	/**
	 * Creates a bag from the given array of
	 * {@link AttributeExp} instances
	 *
	 * @param values an array of attribute values
	 * @return {@link BagOfAttributeExp}
	 * @exception IllegalArgumentException if bag
	 * can not be created from a given values
	 */
//	BagOfAttributeExp bagOf(Object ...values);

	/**
	 * Creates an empty bag
	 *
	 * @return {@link BagOfAttributeExp} an empty
	 * bag
	 */
	BagOfAttributeExp emptyBag();
}
