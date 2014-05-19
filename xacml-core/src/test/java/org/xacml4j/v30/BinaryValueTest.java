package org.xacml4j.v30;

/*
 * #%L
 * Artagon XACML 3.0 Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Artagon
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

public class BinaryValueTest
{
	@Test
	public void testCreateFromBinaryAndEncodeToBase64()
	{
		byte[] v0 = {0, 1, 3, 5};
		byte[] v1 = {0, 1, 3, 5};
		BinaryValue value1 = BinaryValue.valueOf(v0);
		BinaryValue value2 = BinaryValue.valueOf(v1);
		assertEquals(value1, value2);
		assertEquals("AAEDBQ==", value1.toBase64Encoded());
	}

	@Test
	public void testCreateFromZeroLenghtArray()
	{
		byte[] v = {};
		BinaryValue bv = BinaryValue.valueOf(v);
		assertEquals("", bv.toBase64Encoded());
		assertEquals("", bv.toHexEncoded());
	}

	@Test(expected=NullPointerException.class)
	public void testCreateFromNull()
	{
		BinaryValue.valueOf(null);
	}

	@Test
	public void testCreateFromBinaryAndEncodeToHex()
	{
		byte[] v0 = {0, 1, 3, 5};
		byte[] v1 = {0, 1, 3, 5};
		BinaryValue value1 = BinaryValue.valueOf(v0);
		BinaryValue value2 = BinaryValue.valueOf(v1);
		assertEquals(value1, value2);
		assertEquals("00010305", value1.toHexEncoded());
	}
}
