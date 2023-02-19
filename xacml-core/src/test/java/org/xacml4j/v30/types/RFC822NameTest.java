package org.xacml4j.v30.types;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
