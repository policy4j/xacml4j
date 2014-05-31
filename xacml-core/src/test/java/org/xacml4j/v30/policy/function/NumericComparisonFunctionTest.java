package org.xacml4j.v30.policy.function;

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
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.DoubleExp;
import org.xacml4j.v30.types.IntegerExp;



public class NumericComparisonFunctionTest
{
	@Test
	public void testGreaterThanDouble()
	{
		DoubleExp a = DoubleExp.valueOf(35.0);
		DoubleExp b = DoubleExp.valueOf(35.0);
		assertEquals(BooleanExp.valueOf(false), NumericComparisonFunctions.greaterThanDouble(a, b));
		a = DoubleExp.valueOf(35.1);
		b = DoubleExp.valueOf(35.0);
		assertEquals(BooleanExp.valueOf(true), NumericComparisonFunctions.greaterThanDouble(a, b));
		a = DoubleExp.valueOf(35.1);
		b = DoubleExp.valueOf(35.2);
		assertEquals(BooleanExp.valueOf(false), NumericComparisonFunctions.greaterThanDouble(a, b));
	}

	@Test
	public void testGreaterThanOrEqualDouble()
	{
		DoubleExp a = DoubleExp.valueOf(35.0);
		DoubleExp b = DoubleExp.valueOf(35.0);
		assertEquals(BooleanExp.valueOf(true), NumericComparisonFunctions.greaterThanOrEqualDouble(a, b));
		a = DoubleExp.valueOf(35.1);
		b = DoubleExp.valueOf(35.0);
		assertEquals(BooleanExp.valueOf(true), NumericComparisonFunctions.greaterThanOrEqualDouble(a, b));
		a = DoubleExp.valueOf(35.1);
		b = DoubleExp.valueOf(35.2);
		assertEquals(BooleanExp.valueOf(false), NumericComparisonFunctions.greaterThanOrEqualDouble(a, b));
	}

	@Test
	public void testGreaterThanInteger()
	{
		IntegerExp a = IntegerExp.valueOf(35);
		IntegerExp b = IntegerExp.valueOf(35);
		assertEquals(BooleanExp.valueOf(false), NumericComparisonFunctions.greaterThanInteger(a, b));
		a = IntegerExp.valueOf(36);
		b = IntegerExp.valueOf(35);
		assertEquals(BooleanExp.valueOf(true), NumericComparisonFunctions.greaterThanInteger(a, b));
		a = IntegerExp.valueOf(35);
		b = IntegerExp.valueOf(36);
		assertEquals(BooleanExp.valueOf(false), NumericComparisonFunctions.greaterThanInteger(a, b));
	}

	@Test
	public void testGreaterThanOrEqualsInteger()
	{
		IntegerExp a = IntegerExp.valueOf(35);
		IntegerExp b = IntegerExp.valueOf(35);
		assertEquals(BooleanExp.valueOf(true), NumericComparisonFunctions.greaterThanOrEqualInteger(a, b));
		a = IntegerExp.valueOf(36);
		b = IntegerExp.valueOf(35);
		assertEquals(BooleanExp.valueOf(true), NumericComparisonFunctions.greaterThanOrEqualInteger(a, b));
		a = IntegerExp.valueOf(35);
		b = IntegerExp.valueOf(36);
		assertEquals(BooleanExp.valueOf(false), NumericComparisonFunctions.greaterThanOrEqualInteger(a, b));
	}
}
