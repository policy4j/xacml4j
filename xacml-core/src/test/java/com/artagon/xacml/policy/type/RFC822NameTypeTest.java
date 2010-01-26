package com.artagon.xacml.policy.type;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.policy.Attribute;

public class RFC822NameTypeTest 
{
	private RFC822NameType t;
	
	@Before
	public void init(){
		this.t = DataTypes.RFC822NAME.getType();
	}
	
	@Test
	public void testEquals()
	{
		Attribute n0 = t.create("test0@test.org");
		Attribute n1 = t.create("test1@test.org");
		Attribute n2 = t.create("test0@TEST.org");
		Attribute n3 = t.create("TEST0@test.org");
		assertFalse(n0.equals(n1));
		assertTrue(n0.equals(n2));
		assertFalse(n0.equals(n3));
	}
}
