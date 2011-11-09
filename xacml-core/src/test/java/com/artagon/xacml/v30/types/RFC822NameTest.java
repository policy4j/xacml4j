package com.artagon.xacml.v30.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.artagon.xacml.v30.RFC822Name;

public class RFC822NameTest 
{
	@Test
	public void testParseValidName()
	{
		RFC822Name n = RFC822Name.parse("test@test.org");
		assertEquals("test", n.getLocalPart());
		assertEquals("test.org", n.getDomainPart());
		n = RFC822Name.parse("tEst@TeSt.org");
		assertEquals("tEst", n.getLocalPart());
		assertEquals("test.org", n.getDomainPart());
	}

	/**
	 * Test case tests RegExp DoS attack vector
	 * on email validation
	 */
	@Test(expected=IllegalArgumentException.class)
	public void parseInvalidName() {
		RFC822Name.parse("13b3f46cbea4d9388066e08cc58134381ec8a7d7");
	}

	@Test
	public void testMatch()
	{
		RFC822Name n = RFC822Name.parse("test@east.test.org");
		assertTrue(n.matches("east.test.org"));
		assertTrue(n.matches("test@east.test.org"));
		assertTrue(n.matches(".test.org"));
	}
}
