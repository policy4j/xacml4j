package org.xacml4j.v30.types;

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

import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.BagOfValuesType;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.ValueExpTypeInfo;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;

/**
 * Represents XACML value type class
 *
 * @author Giedrius Trumpickas
 */
public interface ValueType extends ValueExpTypeInfo, Function<Object, Value>
{
	/**
	 * Gets fully qualified data type identifier
	 *
	 * @return data type identifier
	 */
	String getTypeId();
	
	/**
	 * Gets "short" version of
	 * the data type identifier
	 * 
	 * @return short version of the data type
	 * identifier
	 */
	String getShortTypeId();

	/**
	 * Gets all aliases for this data type
	 *
	 * @return {@link Set} of aliases for this type
	 */
	Set<String> getTypeIdAliases();

	@Override
	default ValueType getValueType(){
		return this;
	}

	default ValueExpTypeInfo toBagType(){
		return bagType();
	}

	/**
	 * A factory method to of an
	 * {@link Value} of this type
	 * from a given object
	 *
	 * @param v a value
	 * @return {@link Value} value
	 * @exception SyntaxException
	 */
	default <T extends Value> T ofAny(Object v){
		return ofAny(v, (Object[]) null);
	}

	/**
	 * A factory method to parse
	 * instances of {@Link AttributeValue}
	 *
	 * @param v a value
	 * @param params additional parameters
	 * @return {@link Value}
	 */
	<T extends Value> T ofAny(Object v, Object ...params);

	default boolean isBag() {
		return false;
	}

	/**
	 * Creates type representing collection of
	 * attribute values of this
	 * data type
	 *
	 * @return {@link BagOfValuesType} defaultProvider
	 */
	default BagOfValuesType bagType(){
		return new BagOfValuesType(this);
    }

	/**
	 * Creates a new bag builder {@link BagOfValues.Builder}
	 * for this type
	 *
	 * @return {@link BagOfValues.Builder}
	 */
	default BagOfValues.Builder bagBuilder(){
	    return new BagOfValues.Builder(bagType());
    }

	/**
	 * Shortcut for {}
	 *
	 * @return
	 */
	default BagOfValues emptyBag(){
		return bagBuilder().build();
	}

	/**
	 * Builds bag from given
	 * {@link Value} instances
	 *
	 * @param values
	 * @return {@link BagOfValues}s
	 */
	default BagOfValues bagOf(
			Value...values){
		return bagBuilder()
				.attribute(
						values)
				.build();
	}

	@Override
	default Value apply(Object o){
		if(o == null){
			throw SyntaxException
					.invalidAttributeValue(
							null, this);
		}
		if(o instanceof Object[]){
			Object[] array = (Object[])o;
			if(array.length == 0){
				throw SyntaxException
						.invalidAttributeValue(
								Arrays.toString(
										(Object[])o), this);
			}
			if(array.length == 1){
				return ofAny(o);
			}
			return ofAny(array[0],
					Arrays.copyOfRange(array,
							1, array.length));
		}
		return ofAny(o);
	}

}
