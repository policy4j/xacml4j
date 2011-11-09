package com.artagon.xacml.v30.types;


import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;


public class Base64BinaryTypeTest 
{
	private Base64BinaryType t;
	
	@Before
	public void init(){
		this.t = Base64BinaryType.BASE64BINARY;
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testIncorrectlyBase64EncodedString(){
		t.create("AAEDBQ+++");
	}
	
	@Test
	public void testCreateValueFromBinaryArray()
	{
		byte[] v0 = {0, 1, 3, 5};
		byte[] v1 = {0, 1, 3, 5};
		Base64BinaryExp value1 = t.create(v0);
		Base64BinaryExp value2 = t.create(v1);
		assertEquals(value1, value2);
		assertEquals(value1.toXacmlString(), value2.toXacmlString());
		assertEquals("AAEDBQ==", value1.toXacmlString());
	}
	
	@Test
	public void testCreateValueFromString()
	{
		byte[] data = {0, 1, 3, 5};
		Base64BinaryExp value1 = t.create("AAEDBQ==");
		Base64BinaryExp value2 = t.create(data);
		assertEquals(value1, value2);
	}
}


