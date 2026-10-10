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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xacml4j.v30.ValueExpTypeInfo;

/**
 * Bead policy4j-the.4.61: the dynamic type is a data type that is known
 * only at evaluation. It is consistent with every data type of the same
 * shape, it is not a registered data type, and no value has it.
 */
public class DynamicTypeTest
{
	private static final ValueExpTypeInfo DYNAMIC = DynamicType.INSTANCE;
	private static final ValueExpTypeInfo DYNAMIC_BAG = DynamicType.INSTANCE.bagType();
	private static final ValueExpTypeInfo STRING = XacmlTypes.STRING;
	private static final ValueExpTypeInfo INTEGER = XacmlTypes.INTEGER;
	private static final ValueExpTypeInfo STRING_BAG = XacmlTypes.STRING.bagType();
	private static final ValueExpTypeInfo INTEGER_BAG = XacmlTypes.INTEGER.bagType();

	@ParameterizedTest(name = "{0}: {3}")
	@MethodSource("pairs")
	public void consistency(String name, ValueExpTypeInfo expected, ValueExpTypeInfo actual, boolean consistent)
	{
		assertEquals(consistent, DynamicType.isConsistent(expected, actual));
		// The relation is symmetric.
		assertEquals(consistent, DynamicType.isConsistent(actual, expected));
	}

	static Stream<Arguments> pairs()
	{
		return Stream.of(
				Arguments.of("string, string", STRING, STRING, true),
				Arguments.of("string, integer", STRING, INTEGER, false),
				Arguments.of("string, dynamic", STRING, DYNAMIC, true),
				Arguments.of("dynamic, dynamic", DYNAMIC, DYNAMIC, true),
				Arguments.of("bag of string, bag of string", STRING_BAG, STRING_BAG, true),
				Arguments.of("bag of string, bag of integer", STRING_BAG, INTEGER_BAG, false),
				Arguments.of("bag of string, bag of dynamic", STRING_BAG, DYNAMIC_BAG, true),
				Arguments.of("bag of dynamic, bag of dynamic", DYNAMIC_BAG, DYNAMIC_BAG, true),
				// The shape is known also when the data type is not.
				Arguments.of("string, bag of dynamic", STRING, DYNAMIC_BAG, false),
				Arguments.of("bag of string, dynamic", STRING_BAG, DYNAMIC, false),
				Arguments.of("dynamic, bag of dynamic", DYNAMIC, DYNAMIC_BAG, false),
				Arguments.of("string, bag of string", STRING, STRING_BAG, false));
	}

	@Test
	public void dynamicTypeAndItsBagAreDynamic()
	{
		assertTrue(DynamicType.isDynamic(DYNAMIC));
		assertTrue(DynamicType.isDynamic(DYNAMIC_BAG));
		assertFalse(DynamicType.isDynamic(STRING));
		assertFalse(DynamicType.isDynamic(STRING_BAG));
	}

	/** The shape of an expression of dynamic type is known: a single value, or a bag. */
	@Test
	public void shapeIsKnown()
	{
		assertFalse(DYNAMIC.isBag());
		assertTrue(DYNAMIC_BAG.isBag());
		assertSame(DynamicType.INSTANCE, DYNAMIC.getValueType());
		assertSame(DynamicType.INSTANCE, DYNAMIC_BAG.getValueType());
		assertEquals(DYNAMIC_BAG, DYNAMIC.toBagType());
		assertEquals(DYNAMIC_BAG, DynamicType.INSTANCE.bagType());
		assertNotEquals(STRING_BAG, DYNAMIC_BAG);
	}

	@Test
	public void noMethodReturnsNull()
	{
		assertNotNull(DynamicType.INSTANCE.getTypeId());
		assertNotNull(DynamicType.INSTANCE.getShortTypeId());
		assertNotNull(DynamicType.INSTANCE.getValueType());
		assertNotNull(DynamicType.INSTANCE.toBagType());
		assertNotNull(DynamicType.INSTANCE.bagType());
		assertEquals(0, DynamicType.INSTANCE.getTypeIdAliases().size());
		assertEquals("dynamic", DynamicType.INSTANCE.toString());
	}

	/** A policy and a request name a data type by its identifier. The dynamic type has none that resolves. */
	@Test
	public void dynamicTypeIsNotARegisteredDataType()
	{
		assertEquals(Optional.empty(), XacmlTypes.getType(DynamicType.INSTANCE.getTypeId()));
		assertEquals(Optional.empty(), XacmlTypes.getType(DynamicType.INSTANCE.getShortTypeId()));
	}

	/** A value always has its data type, and so has a bag. */
	@Test
	public void noValueAndNoBagHasTheDynamicType()
	{
		assertThrows(UnsupportedOperationException.class, () -> DynamicType.INSTANCE.ofAny("a"));
		assertThrows(UnsupportedOperationException.class, () -> DynamicType.INSTANCE.apply("a"));
		assertThrows(IllegalArgumentException.class, () -> DynamicType.INSTANCE.bagBuilder());
		assertThrows(IllegalArgumentException.class, () -> DynamicType.INSTANCE.emptyBag());
		assertThrows(IllegalArgumentException.class, () -> DynamicType.INSTANCE.bagType().builder());
		assertThrows(IllegalArgumentException.class, () -> DynamicType.INSTANCE.bagType().emptyBag());
	}
}
