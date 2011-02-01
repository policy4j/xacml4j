package com.artagon.xacml.v30.types;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.AttributeValue;

public class StringTypeTest 
{
	private StringType t;
	
	@Before
	public void init(){
		this.t = StringType.STRING;
	}
	
	@Test
	public void testEquals()
	{
		AttributeValue v0 = t.create("v0");
		AttributeValue v1 = t.create("v1");
		assertFalse(v0.equals(v1));
		AttributeValue v2 = t.create("v0");
		assertFalse(v1.equals(v2));
		assertTrue(v0.equals(v2));
	}
}
