package org.xacml4j.v30.policy.function;

/*
 * #%L
 * Artagon XACML 3.0 Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Artagon
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



public class NumericComparisionFunctionTest
{
	@Test
	public void testGreatherThanDouble()
	{
		DoubleExp a = DoubleExp.valueOf(35.0);
		DoubleExp b = DoubleExp.valueOf(35.0);
		assertEquals(BooleanExp.valueOf(false), NumericComparisionFunctions.greatherThanDouble(a, b));
		a = DoubleExp.valueOf(35.1);
		b = DoubleExp.valueOf(35.0);
		assertEquals(BooleanExp.valueOf(true), NumericComparisionFunctions.greatherThanDouble(a, b));
		a = DoubleExp.valueOf(35.1);
		b = DoubleExp.valueOf(35.2);
		assertEquals(BooleanExp.valueOf(false), NumericComparisionFunctions.greatherThanDouble(a, b));
	}

	@Test
	public void testGreatherThanOrEqualDouble()
	{
		DoubleExp a = DoubleExp.valueOf(35.0);
		DoubleExp b = DoubleExp.valueOf(35.0);
		assertEquals(BooleanExp.valueOf(true), NumericComparisionFunctions.greatherThanOrEqualDouble(a, b));
		a = DoubleExp.valueOf(35.1);
		b = DoubleExp.valueOf(35.0);
		assertEquals(BooleanExp.valueOf(true), NumericComparisionFunctions.greatherThanOrEqualDouble(a, b));
		a = DoubleExp.valueOf(35.1);
		b = DoubleExp.valueOf(35.2);
		assertEquals(BooleanExp.valueOf(false), NumericComparisionFunctions.greatherThanOrEqualDouble(a, b));
	}

	@Test
	public void testGreatherThanInteger()
	{
		IntegerExp a = IntegerExp.valueOf(35);
		IntegerExp b = IntegerExp.valueOf(35);
		assertEquals(BooleanExp.valueOf(false), NumericComparisionFunctions.greatherThanInteger(a, b));
		a = IntegerExp.valueOf(36);
		b = IntegerExp.valueOf(35);
		assertEquals(BooleanExp.valueOf(true), NumericComparisionFunctions.greatherThanInteger(a, b));
		a = IntegerExp.valueOf(35);
		b = IntegerExp.valueOf(36);
		assertEquals(BooleanExp.valueOf(false), NumericComparisionFunctions.greatherThanInteger(a, b));
	}

	@Test
	public void testGreatherThanOrEqualsInteger()
	{
		IntegerExp a = IntegerExp.valueOf(35);
		IntegerExp b = IntegerExp.valueOf(35);
		assertEquals(BooleanExp.valueOf(true), NumericComparisionFunctions.greatherThanOrEqualInteger(a, b));
		a = IntegerExp.valueOf(36);
		b = IntegerExp.valueOf(35);
		assertEquals(BooleanExp.valueOf(true), NumericComparisionFunctions.greatherThanOrEqualInteger(a, b));
		a = IntegerExp.valueOf(35);
		b = IntegerExp.valueOf(36);
		assertEquals(BooleanExp.valueOf(false), NumericComparisionFunctions.greatherThanOrEqualInteger(a, b));
	}
}
