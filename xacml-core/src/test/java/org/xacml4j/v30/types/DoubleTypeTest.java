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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;


public class DoubleTypeTest
{
	@Test
	public void testToXacmlString()
	{
		DoubleVal v0 = XacmlTypes.DOUBLE.ofAny(1.0d);
		DoubleVal v1 = XacmlTypes.DOUBLE.ofAny(-2.0d);
		assertEquals("1.0", v0.toStringExp().get());
		assertEquals("-2.0", v1.toStringExp().get());
	}


	@Test
	public void testEquals()
	{
		Value v0 = XacmlTypes.DOUBLE.ofAny(1.0d);
		Value v1 = XacmlTypes.DOUBLE.ofAny(2.0d);
		Value v2 = XacmlTypes.DOUBLE.ofAny(1.0d);
		assertFalse(v0.equals(v1));
		assertTrue(v0.equals(v2));
		DoubleVal v0Nan = XacmlTypes.DOUBLE.ofAny(java.lang.Double.NaN);
		DoubleVal v1Nan = XacmlTypes.DOUBLE.ofAny(java.lang.Double.NaN);
		assertEquals("NaN", v0Nan.toStringExp().get());
		assertFalse(v0.equals(v0Nan));
		assertTrue(v0Nan.equals(v0Nan));
		assertFalse(v1Nan.equals(v0));
	}
}
