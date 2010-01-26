package com.artagon.xacml.v3.policy.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.policy.type.BooleanType;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.BooleanType.BooleanValue;

public class BooleanTypeTest 
{
	
	private BooleanType t1;
	
	@Before
	public void init(){
		this.t1 = DataTypes.BOOLEAN.getType();
	}
		
	@Test
	public void testCreate()
	{
		Object o = Boolean.FALSE;
		BooleanValue a = t1.create(o);
		assertFalse(a.getValue());
		o = "true";
		BooleanValue a1 = t1.create(o);
		assertTrue(a1.getValue());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testFromAnyObjectWrongContentType()
	{
		Long a = 1l;
		assertFalse(t1.create(a).getValue());
	}

	@Test
	public void fromToXacmlString()
	{
		BooleanValue v = t1.fromXacmlString("True");
		assertEquals(Boolean.TRUE, v.getValue());
		v = t1.fromXacmlString("TRUE");
		assertEquals(Boolean.TRUE, v.getValue());
		v = t1.fromXacmlString("FALSE");
		assertEquals(Boolean.FALSE, v.getValue());
		v = t1.fromXacmlString("False");
		assertEquals(Boolean.FALSE, v.getValue());
	}
	
	@Test
	public void toXacmlString()
	{
		BooleanValue v1 = t1.create(Boolean.TRUE);
		BooleanValue v2 = t1.create(Boolean.FALSE);
		assertEquals("true", v1.toXacmlString());
		assertEquals("false", v2.toXacmlString());
	}
	
	@Test
	public void testEquals()
	{
		BooleanValue v1 = t1.create(Boolean.TRUE);
		BooleanValue v2 = t1.create(Boolean.FALSE);
		BooleanValue v3 = t1.create(Boolean.TRUE);
		BooleanValue v4 = t1.create(Boolean.FALSE);
		assertEquals(v1, v3);
		assertEquals(v2, v4);
	}
}
