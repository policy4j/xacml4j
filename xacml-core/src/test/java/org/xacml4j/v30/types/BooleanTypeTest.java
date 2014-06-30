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
import org.xacml4j.v30.BagOfAttributeExp;

import com.google.common.base.Optional;


public class BooleanTypeTest
{
	@Test
	public void testValueOf()
	{
		BooleanExp a = BooleanExp.valueOf("false");
		assertFalse(a.getValue());
		BooleanExp a1 = BooleanExp.valueOf("true");
		assertTrue(a1.getValue());
	}

	@Test
	public void fromToXacmlString()
	{
		BooleanExp v = BooleanExp.valueOf("True");
		assertEquals(Boolean.TRUE, v.getValue());
		v = BooleanExp.valueOf("TRUE");
		assertEquals(Boolean.TRUE, v.getValue());
		v = BooleanExp.valueOf("FALSE");
		assertEquals(Boolean.FALSE, v.getValue());
		v = BooleanExp.valueOf("False");
		assertEquals(Boolean.FALSE, v.getValue());
	}

	@Test
	public void toXacmlString()
	{
		Optional<TypeToString> c = TypeToString.Types.getIndex().get(XacmlTypes.BOOLEAN);
		BooleanExp v1 = BooleanExp.valueOf(Boolean.TRUE);
		BooleanExp v2 = BooleanExp.valueOf(Boolean.FALSE);
		assertEquals("true", c.get().toString(v1));
		assertEquals("false", c.get().toString(v2));
	}

	@Test
	public void testEquals()
	{
		BooleanExp v1 = BooleanExp.valueOf(Boolean.TRUE);
		BooleanExp v2 = BooleanExp.valueOf(Boolean.FALSE);
		BooleanExp v3 = BooleanExp.valueOf(Boolean.TRUE);
		BooleanExp v4 = BooleanExp.valueOf(Boolean.FALSE);
		assertEquals(v1, v3);
		assertEquals(v2, v4);
	}

	@Test
	public void testBagOf()
	{
		BagOfAttributeExp b1 = BooleanExp.bag().value("true", "false").build();
		BagOfAttributeExp b2 = BooleanExp.bag().value(true, false).build();
		assertEquals(2, b1.size());
		assertEquals(b1, b2);
		assertTrue(b1.contains(BooleanExp.valueOf(true)));
		assertTrue(b1.contains(BooleanExp.valueOf(false)));

	}
}
