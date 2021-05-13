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
import org.xacml4j.v30.AttributeExp;


public class DoubleTypeTest
{
	@Test
	public void testToXacmlString()
	{
		assertEquals("1.0", DoubleExp.valueOf(1.0d).toStringExp().getValue());
		assertEquals("-2.0", DoubleExp.valueOf(-2.0d).toStringExp().getValue());
	}


	@Test
	public void testEquals()
	{
		AttributeExp v0 = DoubleExp.valueOf(1.0d);
		AttributeExp v1 = DoubleExp.valueOf(2.0d);
		AttributeExp v2 = DoubleExp.valueOf(1.0d);
		assertFalse(v0.equals(v1));
		assertTrue(v0.equals(v2));
		DoubleExp v0Nan = DoubleExp.valueOf(Double.NaN);
		DoubleExp v1Nan = DoubleExp.valueOf(Double.NaN);
		assertEquals("NaN", v0Nan.toStringExp().getValue());
		assertFalse(v0.equals(v0Nan));
		assertTrue(v0Nan.equals(v0Nan));
		assertFalse(v1Nan.equals(v0));
	}
}
