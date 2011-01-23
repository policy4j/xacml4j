package com.artagon.xacml.v30.types;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.AttributeValue;
import com.artagon.xacml.v30.types.RFC822NameType;

public class RFC822NameTypeTest 
{
	private RFC822NameType t;
	
	@Before
	public void init(){
		this.t = RFC822NameType.RFC822NAME;
	}
	
	@Test
	public void testEquals()
	{
		AttributeValue n0 = t.create("test0@test.org");
		AttributeValue n1 = t.create("test1@test.org");
		AttributeValue n2 = t.create("test0@TEST.org");
		AttributeValue n3 = t.create("TEST0@test.org");
		assertFalse(n0.equals(n1));
		assertTrue(n0.equals(n2));
		assertFalse(n0.equals(n3));
	}
}
