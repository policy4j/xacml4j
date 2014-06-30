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
import org.xacml4j.v30.types.DoubleExp;
import org.xacml4j.v30.types.IntegerExp;

public class ArtimeticFunctionsTest
{
	@Test
	public void testAddIntegerFunction()
	{
		assertEquals(IntegerExp.valueOf(3),
				ArithmeticFunctions.addInteger(IntegerExp.valueOf(1),
						IntegerExp.valueOf(2)));
	}

	@Test
	public void testAddDoubleFunction()
	{
		assertEquals(DoubleExp.valueOf(3.3 + 4.5),
				ArithmeticFunctions.addDouble(DoubleExp.valueOf(3.3),
						DoubleExp.valueOf(4.5)));
	}

	@Test
	public void testDivideIntegerFunction()
	{
		assertEquals(DoubleExp.valueOf(2),
				ArithmeticFunctions.divideInteger(IntegerExp.valueOf(4),
				IntegerExp.valueOf(2)));
	}
}
