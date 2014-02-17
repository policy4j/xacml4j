package org.xacml4j.v30.types;


import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;


public class Base64BinaryTypeTest
{
	private Base64BinaryType t;
	private Types types;
	@Before
	public void init(){
		this.t = Base64BinaryType.BASE64BINARY;
		this.types = Types.builder().defaultTypes().create();
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
		TypeToString c = types.getCapability(t, TypeToString.class);
		
		assertEquals(value1, value2);
		assertEquals(c.toString(value1), c.toString(value2));
		assertEquals("AAEDBQ==", c.toString(value1));
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


