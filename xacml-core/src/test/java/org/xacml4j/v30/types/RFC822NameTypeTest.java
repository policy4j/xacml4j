package org.xacml4j.v30.types;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xacml4j.v30.AttributeExp;


public class RFC822NameTypeTest
{

	@Test
	public void testEquals()
	{
		AttributeExp n0 = RFC822NameExp.valueOf("test0@test.org");
		AttributeExp n1 = RFC822NameExp.valueOf("test1@test.org");
		AttributeExp n2 = RFC822NameExp.valueOf("test0@TEST.org");
		AttributeExp n3 = RFC822NameExp.valueOf("TEST0@test.org");
		assertFalse(n0.equals(n1));
		assertTrue(n0.equals(n2));
		assertFalse(n0.equals(n3));
	}
}
