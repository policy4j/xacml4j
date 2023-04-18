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

import org.junit.Test;
import org.xacml4j.v30.Binary;

public class BinaryTest
{
	@Test
	public void testCreateFromBinaryAndEncodeToBase64()
	{
		byte[] v0 = {0, 1, 3, 5};
		byte[] v1 = {0, 1, 3, 5};
		Binary value1 = Binary.of(v0);
		Binary value2 = Binary.of(v1);
		assertEquals(value1, value2);
		assertEquals("AAEDBQ==", value1.toBase64Encoded());
	}

	@Test
	public void testCreateFromZeroLenghtArray()
	{
		byte[] v = {};
		Binary bv = Binary.of(v);
		assertEquals("", bv.toBase64Encoded());
		assertEquals("", bv.toHexEncoded());
	}

	@Test(expected=NullPointerException.class)
	public void testCreateFromNull()
	{
		Binary.of(null);
	}

	@Test
	public void testCreateFromBinaryAndEncodeToHex()
	{
		byte[] v0 = {0, 1, 3, 5};
		byte[] v1 = {0, 1, 3, 5};
		Binary value1 = Binary.of(v0);
		Binary value2 = Binary.of(v1);
		assertEquals(value1, value2);
		assertEquals("00010305", value1.toHexEncoded());
	}
}
