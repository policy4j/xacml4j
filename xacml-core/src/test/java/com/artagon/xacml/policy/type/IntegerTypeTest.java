package com.artagon.xacml.policy.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.policy.Attribute;
import com.artagon.xacml.policy.type.IntegerType.IntegerValue;

public class IntegerTypeTest 
{
	private IntegerType t;
	
	@Before
	public void init(){
		this.t = new IntegerTypeImpl();
	}
	
	@Test
	public void testTypeEquals()
	{
		IntegerType t0 = new IntegerTypeImpl();
		IntegerType t1 = new IntegerTypeImpl();
		assertEquals(t0, t1);
	}
	
	@Test
	public void testCreate()
	{
		Attribute v0 = t.create((short)2);
		Attribute v1 = t.create((byte)2);
		Attribute v2 = t.create((int)2);
		Attribute v3 = t.create(2l);
		assertEquals(v3, v0);
		assertEquals(v3, v1);
		assertEquals(v3, v2);
	}
	
	@Test
	public void testEquals()
	{
		IntegerValue v0 = t.create(3l);
		IntegerValue v1 = t.create(2l);
		IntegerValue v2 = t.create(3l);
		assertEquals(v0, v2);
		assertFalse(v1.equals(v2));
	}
}
