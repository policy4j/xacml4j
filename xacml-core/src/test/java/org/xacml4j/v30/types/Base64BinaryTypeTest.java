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


import org.junit.Test;
import org.xacml4j.v30.BagOfAttributeValues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class Base64BinaryTypeTest
{

	@Test(expected=IllegalArgumentException.class)
	public void testIncorrectlyBase64EncodedString(){
		XacmlTypes.BASE64BINARY.of("AAEDBQ+++");
	}

	@Test
	public void testCreateValueFromBinaryArray()
	{
		byte[] v0 = {0, 1, 3, 5};
		byte[] v1 = {0, 1, 3, 5};
		Base64BinaryValue value1 = XacmlTypes.BASE64BINARY.of(v0);
		Base64BinaryValue value2 = XacmlTypes.BASE64BINARY.of(v1);
		
		BagOfAttributeValues b = XacmlTypes.BASE64BINARY.bag().value(v0, v1).build();
		assertEquals(value1, value2);
		assertEquals(XacmlTypes.STRING.of("AAEDBQ=="), value1.toStringExp());
		assertTrue(b.contains(XacmlTypes.BASE64BINARY.of(v0)));
		assertTrue(b.contains(XacmlTypes.BASE64BINARY.of(v1)));
	}

	@Test
	public void testCreateValueFromString()
	{
		byte[] data = {0, 1, 3, 5};
		Base64BinaryValue value1 = XacmlTypes.BASE64BINARY.of("AAEDBQ==");
		Base64BinaryValue value2 = XacmlTypes.BASE64BINARY.of(data);
		assertEquals(value1, value2);
	}
}


