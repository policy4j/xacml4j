package com.artagon.xacml.policy.type;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.policy.Attribute;
import com.artagon.xacml.policy.type.StringType;
import com.artagon.xacml.policy.type.StringTypeImpl;

public class StringTypeTest 
{
	private StringType t;
	
	@Before
	public void init(){
		this.t = new StringTypeImpl();
	}
	
	@Test
	public void testEquals()
	{
		Attribute v0 = t.create("v0");
		Attribute v1 = t.create("v1");
		assertFalse(v0.equals(v1));
		Attribute v2 = t.create("v0");
		assertFalse(v1.equals(v2));
		assertTrue(v0.equals(v2));
	}
}
