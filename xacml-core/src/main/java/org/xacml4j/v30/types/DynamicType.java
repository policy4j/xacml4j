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

import java.util.Set;

import org.xacml4j.v30.ValueExpTypeInfo;

import com.google.common.collect.ImmutableSet;

/**
 * A data type that is known only at evaluation. It is the type of an
 * expression whose data type is a value: the bag of the
 * {@code attribute-designator} and {@code attribute-selector} functions
 * has the data type that their third argument names (XACML v3.0 Related
 * and Nested Entities Profile, section 6.1), and that argument can be an
 * expression.
 * <p>
 * The shape stays known. {@link #INSTANCE} is a single value of a data
 * type that is not known yet; {@code INSTANCE.bagType()} is a bag of it.
 * <p>
 * The dynamic type is {@linkplain #isConsistent consistent} with every
 * data type of the same shape. An expression of dynamic type is accepted
 * where a parameter needs a data type, and its value is checked against
 * the parameter when it is evaluated.
 * <p>
 * No value has the dynamic type: a value always has its data type. The
 * dynamic type is not in the registry of data types, so a policy and a
 * request can not name it.
 */
public enum DynamicType implements ValueType
{
	INSTANCE;

	private static final String TYPE_ID = "urn:xacml4j:data-type:dynamic";

	@Override
	public String getTypeId(){
		return TYPE_ID;
	}

	@Override
	public String getShortTypeId(){
		return "dynamic";
	}

	@Override
	public Set<String> getTypeIdAliases(){
		return ImmutableSet.of();
	}

	/**
	 * @exception UnsupportedOperationException always: no value has the
	 * dynamic type
	 */
	@Override
	public <T extends Value> T ofAny(Object v, Object... params){
		throw new UnsupportedOperationException("No value has the dynamic type");
	}

	/**
	 * Tests if a type is the dynamic type or a bag of it.
	 *
	 * @param type the type of an expression
	 * @return {@code true} if the data type is known only at evaluation
	 */
	public static boolean isDynamic(ValueExpTypeInfo type){
		return type != null && type.getValueType() == INSTANCE;
	}

	/**
	 * Tests if an expression of the type {@code actual} can stand where
	 * the type {@code expected} is needed: the types are equal, or they
	 * have the same shape (both a single value, or both a bag) and the
	 * data type of one of them is dynamic.
	 *
	 * @param expected the type that is needed
	 * @param actual the type of the expression
	 * @return {@code true} if the types are consistent
	 */
	public static boolean isConsistent(ValueExpTypeInfo expected, ValueExpTypeInfo actual){
		return expected.equals(actual) ||
				(actual != null && expected.isBag() == actual.isBag() &&
						(isDynamic(expected) || isDynamic(actual)));
	}

	@Override
	public String toString(){
		return getShortTypeId();
	}
}
