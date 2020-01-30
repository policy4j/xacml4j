package org.xacml4j.v30;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;

/**
 * Represents XACML value type class
 *
 * @author Giedrius Trumpickas
 */
public interface AttributeValueType extends ValueType, Function<Object, AttributeValue>
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
	String getAbbrevDataTypeId();

	/**
	 * Gets all aliases for this data type
	 *
	 * @return {@link Set} of aliases for this type
	 */
	Set<String> getDataTypeIdAliases();

	@Override
	default ValueType getDataType(){
		return this;
	}

	default ValueType toBag(){
		return bagType();
	}

	/**
	 * A factory method to of an
	 * {@link AttributeValue} of this type
	 * from a given object
	 *
	 * @param v a value
	 * @return {@link AttributeValue} value
	 * @exception IllegalArgumentException
	 */
	default <T extends AttributeValue> T of(Object v){
		return of(v, (Object[]) null);
	}

	/**
	 * A factory method to parse
	 * instances of {@Link AttributeValue}
	 *
	 * @param v a value
	 * @param params additional parameters
	 * @param <T> defaultProvider of {@link AttributeValue}
	 * @return
	 */
	<T extends AttributeValue> T of(Object v, Object ...params);

	default boolean isBag() {
		return false;
	}

	/**
	 * Creates type representing collection of
	 * attribute values of this
	 * data type
	 *
	 * @return {@link BagOfAttributeValuesType} defaultProvider
	 */
	default BagOfAttributeValuesType bagType(){
		return new BagOfAttributeValuesType(this);
    }

	/**
	 * Creates a new bag builder {@link BagOfAttributeValues.Builder}
	 * for this type
	 *
	 * @return {@link BagOfAttributeValues.Builder}
	 */
	default BagOfAttributeValues.Builder bag(){
	    return new BagOfAttributeValues.Builder(bagType());
    }

	/**
	 * Shortcut for {}
	 *
	 * @return
	 */
	default BagOfAttributeValues emptyBag(){
		return bag().build();
	}

	/**
	 * Builds bag from given
	 * {@link AttributeValue} instances
	 *
	 * @param attributeExps
	 * @return {@link BagOfAttributeValues}s
	 */
	default BagOfAttributeValues bagOf(
			AttributeValue...attributeExps){
		return bag()
				.attribute(
						attributeExps)
				.build();
	}

	@Override
	default AttributeValue apply(Object o){
		if(o == null){
			throw XacmlSyntaxException
					.invalidAttributeValue(
							null, this);
		}
		if(o instanceof Object[]){
			Object[] array = (Object[])o;
			if(array.length == 0){
				throw XacmlSyntaxException
						.invalidAttributeValue(
								Arrays.toString(
										(Object[])o), this);
			}
			if(array.length == 1){
				return of(o);
			}
			return of(array[0],
					Arrays.copyOfRange(array,
							1, array.length));
		}
		return of(o);
	}

}
