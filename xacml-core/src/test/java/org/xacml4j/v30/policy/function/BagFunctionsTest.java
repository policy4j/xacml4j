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

import org.junit.Test;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.spi.function.AnnotationBasedFunctionProvider;
import org.xacml4j.v30.spi.function.FunctionProvider;
import org.xacml4j.v30.types.AnyURIExp;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.DoubleExp;
import org.xacml4j.v30.types.IntegerExp;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.XacmlTypes;


public class BagFunctionsTest
{

	@Test
	public void testFunctionIfImplemented() throws Exception
	{
		FunctionProvider f = new AnnotationBasedFunctionProvider(
				BagFunctions.class);
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-bag"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-bag"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:double-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:double-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:double-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:double-bag"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:date-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:date-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:date-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:date-bag"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:dateTime-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:dateTime-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:dateTime-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:dateTime-bag"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:time-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:time-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:time-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:time-bag"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:anyURI-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:anyURI-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:anyURI-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:anyURI-bag"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:hexBinary-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:hexBinary-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:hexBinary-bag"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:base64Binary-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:base64Binary-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:base64Binary-bag"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:dayTimeDuration-bag"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:yearMonthDuration-bag"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:x500Name-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:x500Name-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:x500Name-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:x500Name-bag"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:1.0:function:rfc822Name-bag"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:2.0:function:ipAddress-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:2.0:function:ipAddress-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:2.0:function:ipAddress-bag"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:2.0:function:dnsName-one-and-only"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:2.0:function:dnsName-bag-size"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:2.0:function:dnsName-is-in"));
		assertNotNull(f.getFunction("urn:oasis:names:tc:xacml:2.0:function:dnsName-bag"));
	}

	@Test
	public void testStringBagFunctions() throws EvaluationException
	{
		StringExp v0 = StringExp.valueOf("a");
		StringExp v1 = StringExp.valueOf("b");
		BagOfAttributeExp bag = XacmlTypes.STRING.bagOf(v0);
		assertEquals(v0, BagFunctions.stringOneAndOnly(bag));
		assertEquals(IntegerExp.valueOf(1), BagFunctions.stringBagSize(bag));
		assertEquals(BooleanExp.valueOf(true), BagFunctions.stringIsIn(v0, bag));
		assertEquals(BooleanExp.valueOf(false), BagFunctions.stringIsIn(v1, bag));
		assertEquals(XacmlTypes.STRING.bagOf(v0, v1), BagFunctions.stringBag(v0, v1));
	}

	@Test
	public void testBooleanBagFunctions() throws EvaluationException
	{
		BooleanExp v0 = BooleanExp.valueOf(true);
		BooleanExp v1 = BooleanExp.valueOf(false);
		BagOfAttributeExp bag = XacmlTypes.BOOLEAN.bagOf(v0);
		assertEquals(v0, BagFunctions.booleanOneAndOnly(bag));
		assertEquals(IntegerExp.valueOf(1), BagFunctions.booleanBagSize(bag));
		assertEquals(BooleanExp.valueOf(true), BagFunctions.booleanIsIn(v0, bag));
		assertEquals(BooleanExp.valueOf(false), BagFunctions.booleanIsIn(v1, bag));
		assertEquals(XacmlTypes.BOOLEAN.bagOf(v0, v1), BagFunctions.booleanBag(v0, v1));
	}

	@Test
	public void testIntegerBagFunctions() throws EvaluationException
	{
		IntegerExp v0 = IntegerExp.valueOf(1);
		IntegerExp v1 = IntegerExp.valueOf(2);
		BagOfAttributeExp bag = v0.toBag();
		assertEquals(v0, BagFunctions.integerOneAndOnly(bag));
		assertEquals(IntegerExp.valueOf(1), BagFunctions.integerBagSize(bag));
		assertEquals(BooleanExp.create(true), BagFunctions.integerIsIn(v0, bag));
		assertEquals(BooleanExp.create(false), BagFunctions.integerIsIn(v1, bag));
		assertEquals(XacmlTypes.INTEGER.bagOf(v0, v1), BagFunctions.integerBag(v0, v1));
	}

	@Test
	public void testDoubleBagFunctions() throws EvaluationException
	{
		DoubleExp v0 = DoubleExp.valueOf(1);
		DoubleExp v1 = DoubleExp.valueOf(2);
		BagOfAttributeExp bag = v0.toBag();
		assertEquals(v0, BagFunctions.doubleOneAndOnly(bag));
		assertEquals(IntegerExp.valueOf(1), BagFunctions.doubleBagSize(bag));
		assertEquals(BooleanExp.create(true), BagFunctions.doubleIsIn(v0, bag));
		assertEquals(BooleanExp.create(false), BagFunctions.doubleIsIn(v1, bag));
		assertEquals(XacmlTypes.DOUBLE.bagOf(v0, v1), BagFunctions.doubleBag(v0, v1));
	}

	@Test
	public void testAnyURIBagFunctions() throws EvaluationException
	{
		AnyURIExp v0 = AnyURIExp.valueOf("http://www.test0.org");
		AnyURIExp v1 = AnyURIExp.valueOf("http://www.test1.org");
		AnyURIExp v2 = AnyURIExp.valueOf("http://www.test2.org");
		BagOfAttributeExp bag = v0.toBag();
		assertEquals(v0, BagFunctions.anyURIOneAndOnly(bag));
		assertEquals(IntegerExp.valueOf(1), BagFunctions.anyURIBagSize(bag));
		assertEquals(BooleanExp.create(true), BagFunctions.anyURIIsIn(v0, bag));
		assertEquals(BooleanExp.create(false), BagFunctions.anyURIIsIn(v1, bag));
		assertEquals(XacmlTypes.ANYURI.bagOf(v0, v1), BagFunctions.anyURIBag(v0, v1));

		assertEquals(BooleanExp.create(true), BagFunctions.anyURIIsIn(v0, BagFunctions.anyURIBag(v0, v1)));
		assertEquals(BooleanExp.create(false), BagFunctions.anyURIIsIn(v2, BagFunctions.anyURIBag(v0, v1)));
	}
}
