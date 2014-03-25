package org.xacml4j.v30.types;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.xacml4j.v30.BagOfAttributeExp;


public class HexTypeTest
{
	
	@Test
	public void testCreateValueFromBinaryArray()
	{
		byte[] v0 = {0, 1, 3, 5};
		byte[] v1 = {0, 1, 3, 5};
		HexBinaryExp value1 = HexBinaryExp.valueOf(v0);
		HexBinaryExp value2 = HexBinaryExp.valueOf(v1);
		assertEquals(value1, value2);
	}

	@Test
	public void testCreateValueFromString()
	{
		byte[] data = {0, 1, 3, 5};
		HexBinaryExp value1 = HexBinaryExp.valueOf("00010305");
		HexBinaryExp value2 = HexBinaryExp.valueOf(data);
		assertEquals(value1, value2);
		BagOfAttributeExp bag = HexBinaryExp.bag().value(data, "00010305").build();
		assertEquals(XacmlTypes.HEXBINARY, bag.getDataType());
	}
}
