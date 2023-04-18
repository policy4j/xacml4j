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
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.types.XacmlTypes;


public class SetFunctionTest
{
	private static FunctionProvider p;

	@BeforeClass
	public static void init() throws Exception
	{
		p = FunctionProviderBuilder.builder().withDefaultFunctions().build();
	}

	@Test
	public void testIfImplemented()
	{
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-intersection"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-union"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-subset"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-at-least-one-member-of"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-set-equals"));

		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:boolean-intersection"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:boolean-union"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:boolean-subset"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:boolean-at-least-one-member-of"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:boolean-set-equals"));

		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-intersection"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-union"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-subset"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-at-least-one-member-of"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-set-equals"));

		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:double-intersection"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:double-union"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:double-subset"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:double-at-least-one-member-of"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:double-set-equals"));

		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:time-intersection"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:time-union"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:time-subset"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:time-at-least-one-member-of"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:time-set-equals"));

		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:date-intersection"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:date-union"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:date-subset"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:date-at-least-one-member-of"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:date-set-equals"));

		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:dateTime-intersection"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:dateTime-union"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:dateTime-subset"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:dateTime-at-least-one-member-of"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:dateTime-set-equals"));

		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:anyURI-intersection"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:anyURI-union"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:anyURI-subset"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:anyURI-at-least-one-member-of"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:anyURI-set-equals"));

		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:hexBinary-intersection"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:hexBinary-union"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:hexBinary-subset"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:hexBinary-at-least-one-member-of"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:hexBinary-set-equals"));

		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:base64Binary-intersection"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:base64Binary-union"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:base64Binary-subset"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:base64Binary-at-least-one-member-of"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:base64Binary-set-equals"));

		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-intersection"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-union"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-subset"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-at-least-one-member-of"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-set-equals"));

		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-intersection"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-union"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-subset"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-at-least-one-member-of"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-set-equals"));

		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:x500Name-intersection"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:x500Name-union"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:x500Name-subset"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:x500Name-at-least-one-member-of"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:x500Name-set-equals"));

		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-intersection"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-union"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-subset"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-at-least-one-member-of"));
		assertNotNull(p.getFunction("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-set-equals"));
	}

	@Test
	public void testBooleanUnion()
	{
		BagOfValues a = XacmlTypes.BOOLEAN.bagBuilder().attribute(XacmlTypes.BOOLEAN.of(true), XacmlTypes.BOOLEAN.of(true)).build();
		BagOfValues b = XacmlTypes.BOOLEAN.bagBuilder().attribute(XacmlTypes.BOOLEAN.of(true), XacmlTypes.BOOLEAN.of(false)).build();
		BagOfValues c = SetFunctions.booleanUnion(a, b);
		assertEquals(2, c.size());
		assertTrue(c.contains(XacmlTypes.BOOLEAN.of(true)));
		assertTrue(c.contains(XacmlTypes.BOOLEAN.of(false)));
	}

	@Test
	public void testBooleanSetEquals()
	{
		BagOfValues a = XacmlTypes.BOOLEAN.bagBuilder().attribute(XacmlTypes.BOOLEAN.of(true), XacmlTypes.BOOLEAN.of(false)).build();
		BagOfValues b = XacmlTypes.BOOLEAN.bagBuilder().attribute(XacmlTypes.BOOLEAN.of(false), XacmlTypes.BOOLEAN.of(true)).build();

		assertEquals(XacmlTypes.BOOLEAN.of(true), SetFunctions.booleanSetEquals(a, b));
	}

	@Test
	public void testBooleanIntersection()
	{
		BagOfValues a = XacmlTypes.BOOLEAN.bagBuilder().attribute(XacmlTypes.BOOLEAN.of(true), XacmlTypes.BOOLEAN.of(false)).build();
		BagOfValues b = XacmlTypes.BOOLEAN.bagBuilder().attribute(XacmlTypes.BOOLEAN.of(true), XacmlTypes.BOOLEAN.of(true)).build();

		assertEquals(XacmlTypes.BOOLEAN.of(true).toBag(), SetFunctions.booleanIntersection(a, b));
	}

	@Test
	public void testBooleanIntercetion()
	{
		BagOfValues a = XacmlTypes.BOOLEAN.bagBuilder().attribute(XacmlTypes.BOOLEAN.of(true), XacmlTypes.BOOLEAN.of(true)).build();
		BagOfValues b = XacmlTypes.BOOLEAN.bagBuilder().attribute(XacmlTypes.BOOLEAN.of(false), XacmlTypes.BOOLEAN.of(false)).build();
		BagOfValues c = SetFunctions.booleanIntersection(a, b);
		assertEquals(0, c.size());

		b = XacmlTypes.BOOLEAN.bagBuilder().attribute(XacmlTypes.BOOLEAN.of(true), XacmlTypes.BOOLEAN.of(false)).build();
		c = SetFunctions.booleanIntersection(a, b);
		assertEquals(1, c.size());
		assertTrue(c.contains(XacmlTypes.BOOLEAN.of(true)));
	}
}
