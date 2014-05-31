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
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xacml4j.v30.BagOfAttributeExp;


public class Base64BinaryTypeTest
{

	@Test(expected=IllegalArgumentException.class)
	public void testIncorrectlyBase64EncodedString(){
		Base64BinaryExp.valueOf("AAEDBQ+++");
	}

	@Test
	public void testCreateValueFromBinaryArray()
	{
		byte[] v0 = {0, 1, 3, 5};
		byte[] v1 = {0, 1, 3, 5};
		Base64BinaryExp value1 = Base64BinaryExp.valueOf(v0);
		Base64BinaryExp value2 = Base64BinaryExp.valueOf(v1);
		
		BagOfAttributeExp b = Base64BinaryExp.bag().value(v0, v1).build();
		assertEquals(value1, value2);
		assertEquals(StringExp.valueOf("AAEDBQ=="), value1.toStringExp());
		assertTrue(b.contains(Base64BinaryExp.valueOf(v0)));
		assertTrue(b.contains(Base64BinaryExp.valueOf(v1)));
	}

	@Test
	public void testCreateValueFromString()
	{
		byte[] data = {0, 1, 3, 5};
		Base64BinaryExp value1 = Base64BinaryExp.valueOf("AAEDBQ==");
		Base64BinaryExp value2 = Base64BinaryExp.valueOf(data);
		assertEquals(value1, value2);
	}
}


