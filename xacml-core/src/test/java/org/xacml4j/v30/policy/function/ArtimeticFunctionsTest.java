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
import org.xacml4j.v30.types.XacmlTypes;

public class ArtimeticFunctionsTest
{
	@Test
	public void testAddIntegerFunction()
	{
		assertEquals(XacmlTypes.INTEGER.of(3),
				ArithmeticFunctions.addInteger(XacmlTypes.INTEGER.of(1),
						XacmlTypes.INTEGER.of(2)));
	}

	@Test
	public void testAddDoubleFunction()
	{
		assertEquals(XacmlTypes.DOUBLE.of(3.3 + 4.5),
				ArithmeticFunctions.addDouble(XacmlTypes.DOUBLE.of(3.3),
						XacmlTypes.DOUBLE.of(4.5)));
	}

	@Test
	public void testDivideIntegerFunction()
	{
		assertEquals(XacmlTypes.DOUBLE.of(2),
				ArithmeticFunctions.divideInteger(XacmlTypes.INTEGER.of(4),
				XacmlTypes.INTEGER.of(2)));
	}
}
