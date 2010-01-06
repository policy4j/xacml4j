package com.artagon.xacml.policy.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.policy.Attribute;
import com.artagon.xacml.policy.type.DoubleType;
import com.artagon.xacml.policy.type.DoubleType.DoubleValue;
import com.artagon.xacml.policy.type.DoubleTypeImpl;

public class DoubleTypeTest 
{
	private DoubleType t1;
	private DoubleType t2;
	
	@Before
	public void init(){
		this.t1 = new DoubleTypeImpl();
		this.t2 = new DoubleTypeImpl();
	}
	
	@Test
	public void testToXacmlString()
	{
		Attribute v0 = t1.create(1.0d);
		Attribute v1 = t1.create(-2.0d);
		assertEquals("1.0", v0.toXacmlString());
		assertEquals("-2.0", v1.toXacmlString());
	}
	
	@Test
	public void testFromAnyObject()
	{
		Object o = 0.2d;
		DoubleValue a = t1.create(o);
		assertEquals(o, a.getValue());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testFromAnyObjectWrongContentType()
	{
		Object o = 10;
		t1.create(o);
	}
	
	@Test
	public void testTypeEquals()
	{
		assertEquals(t1, t2);
		System.out.println(t1.toString());
		System.out.println(t2.toString());
	}
	
	@Test
	public void testEquals()
	{
		Attribute v0 = t1.create(1.0d);
		Attribute v1 = t1.create(2.0d);
		Attribute v2 = t1.create(1.0d);
		assertFalse(v0.equals(v1));
		assertTrue(v0.equals(v2));
		Attribute v0Nan = t1.create(Double.NaN);
		Attribute v1Nan = t1.create(Double.NaN);
		assertFalse(v0.equals(v0Nan));
		assertTrue(v0Nan.equals(v0Nan));
		assertFalse(v1Nan.equals(v0));
	}
}
