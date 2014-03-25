package org.xacml4j.v30.types;


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


