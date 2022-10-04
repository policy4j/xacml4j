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

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.Value;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.policy.FunctionReference;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.spi.function.FunctionProvider;
import org.xacml4j.v30.types.BooleanValue;
import org.xacml4j.v30.types.XacmlTypes;



public class HigherOrderFunctionsTest
{
	private FunctionProvider functions;
	private EvaluationContext context;

	private FunctionSpec intToString;
	private FunctionSpec intEq;
	private FunctionSpec intGreaterThan;
	private FunctionSpec stringRegExpMatch;

	private FunctionSpec map;
	private FunctionSpec anyOf;
	private FunctionSpec allOfAny;
	private FunctionSpec anyOfAll;
	private FunctionSpec allOfAll;

	private IMocksControl c;


	@Before
	public void init() throws Exception
	{
		this.c  = createControl();
		this.functions = FunctionProvider.builder().withStandardFunctions().build();
		this.context = c.createMock(EvaluationContext.class);
		this.intToString = functions.getFunction("urn:oasis:names:tc:xacml:3.0:function:string-from-integer").get();
		this.intEq = functions.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-equal").get();
		this.intGreaterThan = functions.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than").get();
		this.stringRegExpMatch = functions.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-regexp-match").get();
		this.map = functions.getFunction("urn:oasis:names:tc:xacml:1.0:function:map").get();
		this.anyOf = functions.getFunction("urn:oasis:names:tc:xacml:1.0:function:any-of").get();
		this.allOfAny = functions.getFunction("urn:oasis:names:tc:xacml:1.0:function:all-of-any").get();
		this.anyOfAll = functions.getFunction("urn:oasis:names:tc:xacml:1.0:function:any-of-all").get();
		this.allOfAll = functions.getFunction("urn:oasis:names:tc:xacml:1.0:function:all-of-all").get();
		assertNotNull(map);
		assertNotNull(intToString);
		assertNotNull(intEq);
	}

	@Test
	public void testMapWithValidArguments() throws EvaluationException
	{
		Collection<Value> v = new LinkedList<Value>();
		v.add(XacmlTypes.INTEGER.of(10));
		v.add(XacmlTypes.INTEGER.of(20));
		v.add(XacmlTypes.INTEGER.of(30));

		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(4);

		c.replay();
		BagOfValues bag =  map.invoke(context, new FunctionReference(intToString),
		                              XacmlTypes.INTEGER.bag().attributes(v).build());
		c.verify();
		assertTrue(bag.contains(XacmlTypes.STRING.of("10")));
		assertTrue(bag.contains(XacmlTypes.STRING.of("20")));
		assertTrue(bag.contains(XacmlTypes.STRING.of("30")));
	}

	@Test
	public void testAnyOf() throws EvaluationException
	{
		Collection<Value> v = new LinkedList<Value>();
		v.add(XacmlTypes.INTEGER.of(10));
		v.add(XacmlTypes.INTEGER.of(20));
		v.add(XacmlTypes.INTEGER.of(30));

		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(3);

		c.replay();
		BooleanValue r = anyOf.invoke(context, new FunctionReference(intEq), XacmlTypes.INTEGER.of(20),
				XacmlTypes.INTEGER.bag().attributes(v).build());
		assertEquals(XacmlTypes.BOOLEAN.of(true), r);
		c.verify();
	}

	@Test
	public void testAllOfAny() throws EvaluationException
	{
		Collection<Value> a = new LinkedList<Value>();
		a.add(XacmlTypes.INTEGER.of(10));
		a.add(XacmlTypes.INTEGER.of(20));

		Collection<Value> b = new LinkedList<Value>();
		b.add(XacmlTypes.INTEGER.of(1));
		b.add(XacmlTypes.INTEGER.of(3));
		b.add(XacmlTypes.INTEGER.of(5));
		b.add(XacmlTypes.INTEGER.of(19));

		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(3);

		c.replay();
		BooleanValue r = allOfAny.invoke(context, new FunctionReference(intGreaterThan),
				XacmlTypes.INTEGER.bag().attributes(a).build(), XacmlTypes.INTEGER.bag().attributes(b).build());
		assertEquals(XacmlTypes.BOOLEAN.of(true), r);
		c.verify();
	}

	@Test
	public void testAnyOfAll() throws EvaluationException
	{
		Collection<Value> a = new LinkedList<Value>();
		a.add(XacmlTypes.INTEGER.of(3));
		a.add(XacmlTypes.INTEGER.of(5));

		Collection<Value> b = new LinkedList<Value>();
		b.add(XacmlTypes.INTEGER.of(1));
		b.add(XacmlTypes.INTEGER.of(2));
		b.add(XacmlTypes.INTEGER.of(3));
		b.add(XacmlTypes.INTEGER.of(4));

		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(8);


		c.replay();
		BooleanValue r = anyOfAll.invoke(context, new FunctionReference(intGreaterThan),
				XacmlTypes.INTEGER.bag().attributes(a).build(), XacmlTypes.INTEGER.bag().attributes(b).build());
		assertEquals(XacmlTypes.BOOLEAN.of(true), r);
		c.verify();
	}

	@Test
	public void testAnyOfAllIIC168() throws EvaluationException
	{

		Collection<Value> a = new LinkedList<Value>();
		a.add(XacmlTypes.STRING.of("   This  is n*o*t* *IT!  "));
		a.add(XacmlTypes.STRING.of("   This is not a match to IT!  "));

		Collection<Value> b = new LinkedList<Value>();
		b.add(XacmlTypes.STRING.of("   This  is IT!  "));
		b.add(XacmlTypes.STRING.of("   This  is not IT!  "));


		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(3);
		c.replay();
		BooleanValue r = anyOfAll.invoke(context, new FunctionReference(stringRegExpMatch),
				XacmlTypes.STRING.bag().attributes(a).build(), XacmlTypes.STRING.bag().attributes(b).build());
		assertEquals(XacmlTypes.BOOLEAN.of(true), r);
		c.verify();
	}

	@Test
	public void testAllOfAll() throws EvaluationException
	{
		Collection<Value> a = new LinkedList<Value>();
		a.add(XacmlTypes.INTEGER.of(5));
		a.add(XacmlTypes.INTEGER.of(6));

		Collection<Value> b = new LinkedList<Value>();
		b.add(XacmlTypes.INTEGER.of(1));
		b.add(XacmlTypes.INTEGER.of(2));
		b.add(XacmlTypes.INTEGER.of(3));
		b.add(XacmlTypes.INTEGER.of(4));

		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(9);

		c.replay();
		BooleanValue r = allOfAll.invoke(context, new FunctionReference(intGreaterThan),
				XacmlTypes.INTEGER.bag().attributes(a).build(), XacmlTypes.INTEGER.bag().attributes(b).build());
		assertEquals(XacmlTypes.BOOLEAN.of(true), r);
		c.verify();
	}
}
