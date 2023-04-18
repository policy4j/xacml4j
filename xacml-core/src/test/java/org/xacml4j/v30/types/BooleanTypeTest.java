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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;
import org.xacml4j.v30.BagOfValues;


public class BooleanTypeTest
{
	@Test
	public void testValueOf()
	{
		BooleanValue a = XacmlTypes.BOOLEAN.of("false");
		assertFalse(a.get());
		BooleanValue a1 = XacmlTypes.BOOLEAN.of("true");
		assertTrue(a1.get());
	}

	@Test
	public void fromToXacmlString()
	{
		BooleanValue v = XacmlTypes.BOOLEAN.of("True");
		assertEquals(Boolean.TRUE, v.get());
		v = XacmlTypes.BOOLEAN.of("TRUE");
		assertEquals(Boolean.TRUE, v.get());
		v = XacmlTypes.BOOLEAN.of("FALSE");
		assertEquals(Boolean.FALSE, v.get());
		v = XacmlTypes.BOOLEAN.of("False");
		assertEquals(Boolean.FALSE, v.get());
	}

	@Test
	public void toXacmlString()
	{
		Optional<TypeToString> c = TypeToString.forType(XacmlTypes.BOOLEAN);
		BooleanValue v1 = XacmlTypes.BOOLEAN.of(Boolean.TRUE);
		BooleanValue v2 = XacmlTypes.BOOLEAN.of(Boolean.FALSE);
		assertEquals("true", c.get().toString(v1));
		assertEquals("false", c.get().toString(v2));
	}

	@Test
	public void testEquals()
	{
		BooleanValue v1 = XacmlTypes.BOOLEAN.of(Boolean.TRUE);
		BooleanValue v2 = XacmlTypes.BOOLEAN.of(Boolean.FALSE);
		BooleanValue v3 = XacmlTypes.BOOLEAN.of(Boolean.TRUE);
		BooleanValue v4 = XacmlTypes.BOOLEAN.of(Boolean.FALSE);
		assertEquals(v1, v3);
		assertEquals(v2, v4);
	}

	@Test
	public void testBagOf()
	{
		assertNotNull(XacmlTypes.BOOLEAN.bagType());
		BagOfValues b1 = XacmlTypes.BOOLEAN.bagBuilder().value("true", "false").build();
		BagOfValues b2 = XacmlTypes.BOOLEAN.bagBuilder().value(true, false).build();
		assertEquals(2, b1.size());
		assertEquals(b1, b2);
		assertTrue(b1.contains(XacmlTypes.BOOLEAN.of(true)));
		assertTrue(b1.contains(XacmlTypes.BOOLEAN.of(false)));

	}
}
