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
