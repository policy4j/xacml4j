package org.xacml4j.v30.policy.function.impl;

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
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.types.RFC822Name;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.X500Name;
import org.xacml4j.v30.types.XacmlTypes;


public class SpecialMatchFunctionsTest
{
	private FunctionProvider p;

	@Before
	public void init() throws Exception
	{
		this.p = FunctionProviderBuilder.builder().withDefaultFunctions().build();
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
		StringVal p = XacmlTypes.STRING.ofAny(".sun.com");
		RFC822Name n = XacmlTypes.RFC822NAME.ofAny("test@east.sun.com");
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true), SpecialMatchFunctions.rfc822NameMatch(p, n));

		p = XacmlTypes.STRING.ofAny("sun.com");
		n = XacmlTypes.RFC822NAME.ofAny("test@sun.com");
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true), SpecialMatchFunctions.rfc822NameMatch(p, n));
	}

	@Test
	public void testX500NameMatch()
	{
		X500Name a = XacmlTypes.X500NAME.ofAny("ou=org,o=com");
		X500Name b = XacmlTypes.X500NAME.ofAny("cn=test, ou=org,o=com");

		assertEquals(XacmlTypes.BOOLEAN.ofAny(true), SpecialMatchFunctions.x500NameMatch(a, b));

		a = XacmlTypes.X500NAME.ofAny("ou=org,o=com");
		b = XacmlTypes.X500NAME.ofAny("cn=test, ou=ORG,o=Com");

		assertEquals(XacmlTypes.BOOLEAN.ofAny(true), SpecialMatchFunctions.x500NameMatch(a, b));

		a = XacmlTypes.X500NAME.ofAny("ou=org1,o=com");
		b = XacmlTypes.X500NAME.ofAny("cn=test, ou=ORG,o=com");

		assertEquals(XacmlTypes.BOOLEAN.ofAny(false), SpecialMatchFunctions.x500NameMatch(a, b));

	}
}
