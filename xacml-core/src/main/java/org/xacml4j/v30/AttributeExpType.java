package org.xacml4j.v30;

import java.util.Set;

public interface AttributeExpType extends ValueType
{
	/**
	 * Gets fully qualified data type identifier
	 *
	 * @return data type identifier
	 */
	String getDataTypeId();
	
	/**
	 * Gets "short" version of
	 * the data type identifier
	 * 
	 * @return short version of the data type
	 * identifier
	 */
	String getShortDataTypeId();
	
	Set<String> getDataTypeIdAliases();
	
	/**
	 * A factory method to create an 
	 * {@link AttributeExp} of this type
	 * from a given object
	 * 
	 * @param v a value
	 * @return {@link AttributeExp} value
	 */
	AttributeExp create(Object v);
	
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
	 * Creates an empty bag
	 *
	 * @return {@link BagOfAttributeExp} an empty
	 * bag
	 */
	BagOfAttributeExp emptyBag();
}
