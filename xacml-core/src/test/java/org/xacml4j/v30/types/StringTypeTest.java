package org.xacml4j.v30.types;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xacml4j.v30.AttributeExp;


public class StringTypeTest
{

	@Test
	public void testEquals()
	{
		AttributeExp v0 = StringExp.valueOf("v0");
		AttributeExp v1 = StringExp.valueOf("v1");
		assertFalse(v0.equals(v1));
		AttributeExp v2 = StringExp.valueOf("v0");
		assertFalse(v1.equals(v2));
		assertTrue(v0.equals(v2));
	}
}
