package com.artagon.xacml.v30;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BinaryValueTest 
{
	@Test
	public void testCreateFromBinaryAndEncodeToBase64()
	{
		byte[] v0 = {0, 1, 3, 5};
		byte[] v1 = {0, 1, 3, 5};
		BinaryValue value1 = BinaryValue.fromBytes(v0);
		BinaryValue value2 = BinaryValue.fromBytes(v1);
		assertEquals(value1, value2);
		assertEquals("AAEDBQ==", value1.toBase64Encoded());
	}
	
	@Test
	public void testCreateFromZeroLenghtArray()
	{
		byte[] v = {};
		BinaryValue bv = BinaryValue.fromBytes(v);
		assertEquals("", bv.toBase64Encoded());
		assertEquals("", bv.toHexEncoded());
	}
	
	@Test(expected=NullPointerException.class)
	public void testCreateFromNull()
	{
		BinaryValue.fromBytes(null);
	}
	
	@Test
	public void testCreateFromBinaryAndEncodeToHex()
	{
		byte[] v0 = {0, 1, 3, 5};
		byte[] v1 = {0, 1, 3, 5};
		BinaryValue value1 = BinaryValue.fromBytes(v0);
		BinaryValue value2 = BinaryValue.fromBytes(v1);
		assertEquals(value1, value2);
		assertEquals("00010305", value1.toHexEncoded());
	}
}
