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
import org.xacml4j.v30.AttributeExp;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.pdp.FunctionReference;
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.spi.function.AnnotationBasedFunctionProvider;
import org.xacml4j.v30.spi.function.FunctionProvider;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.IntegerExp;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.XacmlTypes;



public class HigherOrderFunctionsTest
{
	private FunctionProvider higherOrderFunctions;
	private FunctionProvider stringFunctions;
	private FunctionProvider equalityFunctions;
	private FunctionProvider numericComparisonFunctions;
	private FunctionProvider regExpFunctions;
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
		this.higherOrderFunctions = new AnnotationBasedFunctionProvider(HigherOrderFunctions.class);
		this.stringFunctions = new AnnotationBasedFunctionProvider(StringFunctions.class);
		this.equalityFunctions = new AnnotationBasedFunctionProvider(EqualityPredicates.class);
		this.numericComparisonFunctions = new AnnotationBasedFunctionProvider(NumericComparisonFunctions.class);
		this.regExpFunctions = new AnnotationBasedFunctionProvider(RegularExpressionFunctions.class);
		this.context = c.createMock(EvaluationContext.class);
		this.intToString = stringFunctions.getFunction("urn:oasis:names:tc:xacml:3.0:function:string-from-integer");
		this.intEq = equalityFunctions.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-equal");
		this.intGreaterThan = numericComparisonFunctions.getFunction("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than");
		this.stringRegExpMatch = regExpFunctions.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-regexp-match");
		this.map = higherOrderFunctions.getFunction("urn:oasis:names:tc:xacml:1.0:function:map");
		this.anyOf = higherOrderFunctions.getFunction("urn:oasis:names:tc:xacml:1.0:function:any-of");
		this.allOfAny = higherOrderFunctions.getFunction("urn:oasis:names:tc:xacml:1.0:function:all-of-any");
		this.anyOfAll = higherOrderFunctions.getFunction("urn:oasis:names:tc:xacml:1.0:function:any-of-all");
		this.allOfAll = higherOrderFunctions.getFunction("urn:oasis:names:tc:xacml:1.0:function:all-of-all");
		assertNotNull(map);
		assertNotNull(intToString);
		assertNotNull(intEq);
	}

	@Test
	public void testMapWithValidArguments() throws EvaluationException
	{
		Collection<AttributeExp> v = new LinkedList<AttributeExp>();
		v.add(IntegerExp.valueOf(10));
		v.add(IntegerExp.valueOf(20));
		v.add(IntegerExp.valueOf(30));

		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(4);

		c.replay();
		BagOfAttributeExp bag =  map.invoke(context, new FunctionReference(intToString),
				XacmlTypes.INTEGER.bagOf(v));
		c.verify();
		assertTrue(bag.contains(StringExp.valueOf("10")));
		assertTrue(bag.contains(StringExp.valueOf("20")));
		assertTrue(bag.contains(StringExp.valueOf("30")));
	}

	@Test
	public void testAnyOf() throws EvaluationException
	{
		Collection<AttributeExp> v = new LinkedList<AttributeExp>();
		v.add(IntegerExp.valueOf(10));
		v.add(IntegerExp.valueOf(20));
		v.add(IntegerExp.valueOf(30));

		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(3);

		c.replay();
		BooleanExp r = anyOf.invoke(context, new FunctionReference(intEq), IntegerExp.valueOf(20),
				XacmlTypes.INTEGER.bagOf(v));
		assertEquals(BooleanExp.valueOf(true), r);
		c.verify();
	}

	@Test
	public void testAllOfAny() throws EvaluationException
	{
		Collection<AttributeExp> a = new LinkedList<AttributeExp>();
		a.add(IntegerExp.valueOf(10));
		a.add(IntegerExp.valueOf(20));

		Collection<AttributeExp> b = new LinkedList<AttributeExp>();
		b.add(IntegerExp.valueOf(1));
		b.add(IntegerExp.valueOf(3));
		b.add(IntegerExp.valueOf(5));
		b.add(IntegerExp.valueOf(19));

		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(3);

		c.replay();
		BooleanExp r = allOfAny.invoke(context, new FunctionReference(intGreaterThan),
				XacmlTypes.INTEGER.bagOf(a), XacmlTypes.INTEGER.bagOf(b));
		assertEquals(BooleanExp.valueOf(true), r);
		c.verify();
	}

	@Test
	public void testAnyOfAll() throws EvaluationException
	{
		Collection<AttributeExp> a = new LinkedList<AttributeExp>();
		a.add(IntegerExp.valueOf(3));
		a.add(IntegerExp.valueOf(5));

		Collection<AttributeExp> b = new LinkedList<AttributeExp>();
		b.add(IntegerExp.valueOf(1));
		b.add(IntegerExp.valueOf(2));
		b.add(IntegerExp.valueOf(3));
		b.add(IntegerExp.valueOf(4));

		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(8);


		c.replay();
		BooleanExp r = anyOfAll.invoke(context, new FunctionReference(intGreaterThan),
				XacmlTypes.INTEGER.bagOf(a), XacmlTypes.INTEGER.bagOf(b));
		assertEquals(BooleanExp.valueOf(true), r);
		c.verify();
	}

	@Test
	public void testAnyOfAllIIC168() throws EvaluationException
	{

		Collection<AttributeExp> a = new LinkedList<AttributeExp>();
		a.add(StringExp.valueOf("   This  is n*o*t* *IT!  "));
		a.add(StringExp.valueOf("   This is not a match to IT!  "));

		Collection<AttributeExp> b = new LinkedList<AttributeExp>();
		b.add(StringExp.valueOf("   This  is IT!  "));
		b.add(StringExp.valueOf("   This  is not IT!  "));


		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(3);
		c.replay();
		BooleanExp r = anyOfAll.invoke(context, new FunctionReference(stringRegExpMatch),
				XacmlTypes.STRING.bagOf(a), XacmlTypes.STRING.bagOf(b));
		assertEquals(BooleanExp.valueOf(true), r);
		c.verify();
	}

	@Test
	public void testAllOfAll() throws EvaluationException
	{
		Collection<AttributeExp> a = new LinkedList<AttributeExp>();
		a.add(IntegerExp.valueOf(5));
		a.add(IntegerExp.valueOf(6));

		Collection<AttributeExp> b = new LinkedList<AttributeExp>();
		b.add(IntegerExp.valueOf(1));
		b.add(IntegerExp.valueOf(2));
		b.add(IntegerExp.valueOf(3));
		b.add(IntegerExp.valueOf(4));

		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false).times(9);

		c.replay();
		BooleanExp r = allOfAll.invoke(context, new FunctionReference(intGreaterThan),
				XacmlTypes.INTEGER.bagOf(a), XacmlTypes.INTEGER.bagOf(b));
		assertEquals(BooleanExp.valueOf(true), r);
		c.verify();
	}
}
