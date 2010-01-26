package com.artagon.xacml.v30.policy.type;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.policy.type.DataTypes;
import com.artagon.xacml.v30.policy.type.HexBinaryType;
import com.artagon.xacml.v30.policy.type.HexBinaryType.HexBinaryValue;


public class HexTypeTest 
{
	private HexBinaryType t;
	
	@Before
	public void init(){
		this.t = DataTypes.HEXBINARY.getType();
	}
	

	
	@Test
	public void testCreateValueFromBinaryArray()
	{
		byte[] v0 = {0, 1, 3, 5};
		byte[] v1 = {0, 1, 3, 5};
		HexBinaryValue value1 = t.create(v0);
		HexBinaryValue value2 = t.create(v1);
		assertEquals(value1, value2);
		assertEquals(value1.toXacmlString(), value2.toXacmlString());
		assertEquals("00010305", value1.toXacmlString());
	}
	
	@Test
	public void testCreateValueFromString()
	{
		byte[] data = {0, 1, 3, 5};
		HexBinaryValue value1 = t.create("00010305");
		HexBinaryValue value2 = t.create(data);
		assertEquals(value1, value2);
	}
}
