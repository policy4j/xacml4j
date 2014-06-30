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
import org.xacml4j.v30.BagOfAttributeExp;


public class IntegerTypeTest
{
	@Test
	public void testCreate()
	{
		AttributeExp v0 = IntegerExp.valueOf((short)2);
		AttributeExp v1 = IntegerExp.valueOf((byte)2);
		AttributeExp v2 = IntegerExp.valueOf(2);
		AttributeExp v3 = IntegerExp.valueOf(2l);
		assertEquals(v3, v0);
		assertEquals(v3, v1);
		assertEquals(v3, v2);
	}

	@Test
	public void testEquals()
	{
		IntegerExp v0 = IntegerExp.valueOf(3l);
		IntegerExp v1 = IntegerExp.valueOf(2l);
		IntegerExp v2 = IntegerExp.valueOf(3l);
		assertEquals(v0, v2);
		assertFalse(v1.equals(v2));
	}
	
	
	@Test
	public void testBag()
	{
		IntegerExp v0 = IntegerExp.valueOf(3l);
		BagOfAttributeExp bag = IntegerExp.bag().value(1, 4).attribute(v0).build();
		assertTrue(bag.contains(IntegerExp.valueOf(3l)));
		assertTrue(bag.contains(IntegerExp.valueOf(1)));
		assertTrue(bag.contains(IntegerExp.valueOf(4)));
		assertEquals(IntegerExp.emptyBag(), IntegerExp.emptyBag());
	}
}
