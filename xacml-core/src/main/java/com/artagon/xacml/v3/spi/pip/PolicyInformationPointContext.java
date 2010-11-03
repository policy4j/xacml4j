package com.artagon.xacml.v3.spi.pip;

import java.util.Calendar;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;

public interface PolicyInformationPointContext 
{
	/**
	 * Gets current date/time
	 * 
	 * @return {@link Calendar} instance
	 * representing current date/time
	 */
	Calendar getCurrentDateTime();
	
	/**
	 * Gets context key at the given index
	 * 
	 * @param index an index
	 * @return {@link BagOfAttributeValues} or <code>null</code>
	 * if key is is not available for a given index
	 */
	BagOfAttributeValues getKeyValues(int index);
	
	/**
	 * Gets a single value from a bag
	 * of values for a resolver key at the given
	 * index
	 * 
	 * @param <V>
	 * @param index an index
	 * @return a key value or <code>null</code>
	 */
	<V extends AttributeValue> V getKeyValue(int index);
}
