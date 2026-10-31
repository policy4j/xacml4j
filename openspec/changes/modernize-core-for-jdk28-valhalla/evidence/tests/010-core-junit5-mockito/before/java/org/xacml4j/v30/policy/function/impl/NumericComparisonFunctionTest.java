package org.xacml4j.v30.policy.function.impl;

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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.xacml4j.v30.types.DoubleVal;
import org.xacml4j.v30.types.IntegerVal;
import org.xacml4j.v30.types.XacmlTypes;


public class NumericComparisonFunctionTest
{
	@Test
	public void testGreaterThanDouble()
	{
		DoubleVal a = XacmlTypes.DOUBLE.ofAny(35.0);
		DoubleVal b = XacmlTypes.DOUBLE.ofAny(35.0);
		assertEquals(XacmlTypes.BOOLEAN.ofAny(false), NumericComparisonFunctions.greaterThanDouble(a, b));
		a = XacmlTypes.DOUBLE.ofAny(35.1);
		b = XacmlTypes.DOUBLE.ofAny(35.0);
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true), NumericComparisonFunctions.greaterThanDouble(a, b));
		a = XacmlTypes.DOUBLE.ofAny(35.1);
		b = XacmlTypes.DOUBLE.ofAny(35.2);
		assertEquals(XacmlTypes.BOOLEAN.ofAny(false), NumericComparisonFunctions.greaterThanDouble(a, b));
	}

	@Test
	public void testGreaterThanOrEqualDouble()
	{
		DoubleVal a = XacmlTypes.DOUBLE.ofAny(35.0);
		DoubleVal b = XacmlTypes.DOUBLE.ofAny(35.0);
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true), NumericComparisonFunctions.greaterThanOrEqualDouble(a, b));
		a = XacmlTypes.DOUBLE.ofAny(35.1);
		b = XacmlTypes.DOUBLE.ofAny(35.0);
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true), NumericComparisonFunctions.greaterThanOrEqualDouble(a, b));
		a = XacmlTypes.DOUBLE.ofAny(35.1);
		b = XacmlTypes.DOUBLE.ofAny(35.2);
		assertEquals(XacmlTypes.BOOLEAN.ofAny(false), NumericComparisonFunctions.greaterThanOrEqualDouble(a, b));
	}

	@Test
	public void testGreaterThanInteger()
	{
		IntegerVal a = XacmlTypes.INTEGER.ofAny(35);
		IntegerVal b = XacmlTypes.INTEGER.ofAny(35);
		assertEquals(XacmlTypes.BOOLEAN.ofAny(false), NumericComparisonFunctions.greaterThanInteger(a, b));
		a = XacmlTypes.INTEGER.ofAny(36);
		b = XacmlTypes.INTEGER.ofAny(35);
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true), NumericComparisonFunctions.greaterThanInteger(a, b));
		a = XacmlTypes.INTEGER.ofAny(35);
		b = XacmlTypes.INTEGER.ofAny(36);
		assertEquals(XacmlTypes.BOOLEAN.ofAny(false), NumericComparisonFunctions.greaterThanInteger(a, b));
	}

	@Test
	public void testGreaterThanOrEqualsInteger()
	{
		IntegerVal a = XacmlTypes.INTEGER.ofAny(35);
		IntegerVal b = XacmlTypes.INTEGER.ofAny(35);
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true), NumericComparisonFunctions.greaterThanOrEqualInteger(a, b));
		a = XacmlTypes.INTEGER.ofAny(36);
		b = XacmlTypes.INTEGER.ofAny(35);
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true), NumericComparisonFunctions.greaterThanOrEqualInteger(a, b));
		a = XacmlTypes.INTEGER.ofAny(35);
		b = XacmlTypes.INTEGER.ofAny(36);
		assertEquals(XacmlTypes.BOOLEAN.ofAny(false), NumericComparisonFunctions.greaterThanOrEqualInteger(a, b));
	}
}
