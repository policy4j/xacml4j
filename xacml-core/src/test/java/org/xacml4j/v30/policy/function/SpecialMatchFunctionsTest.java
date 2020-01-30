package org.xacml4j.v30.policy.function;

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
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.spi.function.AnnotationBasedFunctionProvider;
import org.xacml4j.v30.FunctionProvider;
import org.xacml4j.v30.types.*;


public class SpecialMatchFunctionsTest
{
	private FunctionProvider p;

	@Before
	public void init() throws Exception
	{
		this.p = FunctionProvider.builder().withStandardFunctions().build();
	}

	@Test
	public void testIfFunctionImplemented()
	{
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-match"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:x500Name-match"));
	}

	@Test
	public void testRFC822NameMatch()
	{
		StringValue p = XacmlTypes.STRING.of(".sun.com");
		RFC822NameValue n = XacmlTypes.RFC822NAME.of("test@east.sun.com");
		assertEquals(XacmlTypes.BOOLEAN.of(true), SpecialMatchFunctions.rfc822NameMatch(p, n));

		p = XacmlTypes.STRING.of("sun.com");
		n = XacmlTypes.RFC822NAME.of("test@sun.com");
		assertEquals(XacmlTypes.BOOLEAN.of(true), SpecialMatchFunctions.rfc822NameMatch(p, n));
	}

	@Test
	public void testX500NameMatch()
	{
		X500NameValue a = XacmlTypes.X500NAME.of("ou=org,o=com");
		X500NameValue b = XacmlTypes.X500NAME.of("cn=test, ou=org,o=com");

		assertEquals(XacmlTypes.BOOLEAN.of(true), SpecialMatchFunctions.x500NameMatch(a, b));

		a = XacmlTypes.X500NAME.of("ou=org,o=com");
		b = XacmlTypes.X500NAME.of("cn=test, ou=ORG,o=Com");

		assertEquals(XacmlTypes.BOOLEAN.of(true), SpecialMatchFunctions.x500NameMatch(a, b));

		a = XacmlTypes.X500NAME.of("ou=org1,o=com");
		b = XacmlTypes.X500NAME.of("cn=test, ou=ORG,o=com");

		assertEquals(XacmlTypes.BOOLEAN.of(false), SpecialMatchFunctions.x500NameMatch(a, b));

	}
}
