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
import org.xacml4j.v30.AttributeValue;
import org.xacml4j.v30.BagOfAttributeValues;


public class IntegerTypeTest
{
	@Test
	public void testCreate()
	{
		AttributeValue v0 = XacmlTypes.INTEGER.of((short)2);
		AttributeValue v1 = XacmlTypes.INTEGER.of((byte)2);
		AttributeValue v2 = XacmlTypes.INTEGER.of(2);
		AttributeValue v3 = XacmlTypes.INTEGER.of(2l);
		assertEquals(v3, v0);
		assertEquals(v3, v1);
		assertEquals(v3, v2);
	}

	@Test
	public void testEquals()
	{
		AttributeValue v0 = XacmlTypes.INTEGER.of(3l);
		AttributeValue v1 = XacmlTypes.INTEGER.of(2l);
		AttributeValue v2 = XacmlTypes.INTEGER.of(3l);
		assertEquals(v0, v2);
		assertFalse(v1.equals(v2));
	}
	
	
	@Test
	public void testBag()
	{
		IntegerValue v0 = XacmlTypes.INTEGER.of(3l);
		BagOfAttributeValues bag = XacmlTypes.INTEGER.bag().value(1, 4).attribute(v0).build();
		assertTrue(bag.contains(XacmlTypes.INTEGER.of(3l)));
		assertTrue(bag.contains(XacmlTypes.INTEGER.of(1)));
		assertTrue(bag.contains(XacmlTypes.INTEGER.of(4)));
		assertEquals(XacmlTypes.INTEGER.emptyBag(), XacmlTypes.INTEGER.emptyBag());
	}
}
