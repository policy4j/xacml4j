package org.xacml4j.v30.types;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.types.StringType;


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
		AttributeExp v0 = t.create("v0");
		AttributeExp v1 = t.create("v1");
		assertFalse(v0.equals(v1));
		AttributeExp v2 = t.create("v0");
		assertFalse(v1.equals(v2));
		assertTrue(v0.equals(v2));
	}
}
