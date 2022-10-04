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
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.spi.function.FunctionProvider;
import org.xacml4j.v30.types.IntegerValue;
import org.xacml4j.v30.types.XacmlTypes;


public class LogicalFunctionsTest
{
	private FunctionProvider f;

	FunctionSpec andFunc;
	FunctionSpec orFunc;
	FunctionSpec notFunc;

	private IMocksControl control;

	private EvaluationContext context;

	@Before
	public void init() throws Exception
	{
		this.control = createControl();

		this.f = FunctionProvider.builder().withStandardFunctions().build();
		this.andFunc = f.getFunction("urn:oasis:names:tc:xacml:1.0:function:and").get();
		this.orFunc = f.getFunction("urn:oasis:names:tc:xacml:1.0:function:or").get();
		this.notFunc = f.getFunction("urn:oasis:names:tc:xacml:1.0:function:not").get();
		this.context = control.createMock(EvaluationContext.class);
	}


	@Test
	public void testAndFunction() throws EvaluationException
	{
		control.replay();
		assertEquals(XacmlTypes.BOOLEAN.of(false),
				LogicalFunctions.and(context, XacmlTypes.BOOLEAN.of(false), XacmlTypes.BOOLEAN.of(false)));
		control.verify();

		control.reset();
		control.replay();
		assertEquals(XacmlTypes.BOOLEAN.of(false),
				LogicalFunctions.and(context, XacmlTypes.BOOLEAN.of(true), XacmlTypes.BOOLEAN.of(false)));
		control.verify();

		control.reset();
		control.replay();
		assertEquals(XacmlTypes.BOOLEAN.of(true),
				LogicalFunctions.and(context, XacmlTypes.BOOLEAN.of(true), XacmlTypes.BOOLEAN.of(true)));
		control.verify();

		control.reset();
		control.replay();
		assertEquals(XacmlTypes.BOOLEAN.of(false),
				LogicalFunctions.and(context, XacmlTypes.BOOLEAN.of(false), XacmlTypes.BOOLEAN.of(true)));
		control.verify();
	}

	@Test
	public void testLazyAndFunctionParamEvaluation() throws EvaluationException
	{
		Expression p1 = control.createMock(Expression.class);
		Expression p2 = control.createMock(Expression.class);
		Expression p3 = control.createMock(Expression.class);

		expect(p1.evaluate(context)).andReturn(XacmlTypes.BOOLEAN.of(false));
		control.replay();

		assertEquals(XacmlTypes.BOOLEAN.of(false),
				LogicalFunctions.and(context, p1, p2, p3));
		control.verify();

		control.reset();
		expect(p1.evaluate(context)).andReturn(XacmlTypes.BOOLEAN.of(true));
		expect(p2.evaluate(context)).andReturn(XacmlTypes.BOOLEAN.of(false));
		control.replay();

		assertEquals(XacmlTypes.BOOLEAN.of(false),
				LogicalFunctions.and(context, p1, p2, p3));
		control.verify();

		control.reset();
		expect(p1.evaluate(context)).andReturn(XacmlTypes.BOOLEAN.of(true));
		expect(p2.evaluate(context)).andReturn(XacmlTypes.BOOLEAN.of(true));
		expect(p3.evaluate(context)).andReturn(XacmlTypes.BOOLEAN.of(false));
		control.replay();

		assertEquals(XacmlTypes.BOOLEAN.of(false),
				LogicalFunctions.and(context, p1, p2, p3));
		control.verify();

	}

	@Test
	public void testOrFunction() throws EvaluationException
	{
		control.replay();
		assertEquals(XacmlTypes.BOOLEAN.of(false),
				LogicalFunctions.or(context, XacmlTypes.BOOLEAN.of(false),
						XacmlTypes.BOOLEAN.of(false)));
		control.verify();
		control.reset();
		control.replay();
		assertEquals(XacmlTypes.BOOLEAN.of(true),
				LogicalFunctions.or(context, XacmlTypes.BOOLEAN.of(true), XacmlTypes.BOOLEAN.of(false)));
		control.verify();
		control.reset();
		control.replay();
		assertEquals(XacmlTypes.BOOLEAN.of(true),
				LogicalFunctions.or(context, XacmlTypes.BOOLEAN.of(true), XacmlTypes.BOOLEAN.of(true)));
		control.verify();
		control.reset();
		control.replay();
		assertEquals(XacmlTypes.BOOLEAN.of(true),
				LogicalFunctions.or(context,XacmlTypes.BOOLEAN.of(false), XacmlTypes.BOOLEAN.of(true)));
		verify(context);
	}

	@Test
	public void testLazyOrFunctionParamEvaluation() throws EvaluationException
	{
		Expression p1 = control.createMock(Expression.class);
		Expression p2 = control.createMock(Expression.class);
		Expression p3 = control.createMock(Expression.class);

		expect(p1.evaluate(context)).andReturn(XacmlTypes.BOOLEAN.of(true));
		control.replay();

		assertEquals(XacmlTypes.BOOLEAN.of(true),
				LogicalFunctions.or(context, p1, p2, p3));
		control.verify();

		control.reset();
		expect(p1.evaluate(context)).andReturn(XacmlTypes.BOOLEAN.of(false));
		expect(p2.evaluate(context)).andReturn(XacmlTypes.BOOLEAN.of(true));
		control.replay();

		assertEquals(XacmlTypes.BOOLEAN.of(true),
				LogicalFunctions.or(context, p1, p2, p3));
		control.verify();

		control.reset();
		expect(p1.evaluate(context)).andReturn(XacmlTypes.BOOLEAN.of(false));
		expect(p2.evaluate(context)).andReturn(XacmlTypes.BOOLEAN.of(false));
		control.replay();

		assertEquals(XacmlTypes.BOOLEAN.of(false),
				LogicalFunctions.or(context, p1, p2));
		control.verify();

	}

	@Test
	public void testNOfFunction() throws EvaluationException
	{
		IntegerValue n = XacmlTypes.INTEGER.of(0);
		replay(context);
		assertEquals(XacmlTypes.BOOLEAN.of(true),
				LogicalFunctions.nof(context, n));
		verify(context);
		reset(context);
		replay(context);
		n = XacmlTypes.INTEGER.of(1);
		assertEquals(XacmlTypes.BOOLEAN.of(true),
				LogicalFunctions.nof(context, n, XacmlTypes.BOOLEAN.of(true)));
		verify(context);
		reset(context);
		replay(context);
		assertEquals(XacmlTypes.BOOLEAN.of(false),
				LogicalFunctions.nof(context, n, XacmlTypes.BOOLEAN.of(false)));
		verify(context);
		reset(context);
		replay(context);
		n = XacmlTypes.INTEGER.of(2);
		assertEquals(XacmlTypes.BOOLEAN.of(true),
				LogicalFunctions.nof(context, n,
						XacmlTypes.BOOLEAN.of(true), XacmlTypes.BOOLEAN.of(true)));
		verify(context);
		reset(context);
		replay(context);
		assertEquals(XacmlTypes.BOOLEAN.of(false),
				LogicalFunctions.nof(context, n,
						XacmlTypes.BOOLEAN.of(true), XacmlTypes.BOOLEAN.of(false)));
		verify(context);
		reset(context);
		replay(context);
		n = XacmlTypes.INTEGER.of(2);
		assertEquals(XacmlTypes.BOOLEAN.of(true),
				LogicalFunctions.nof(context, n,
						XacmlTypes.BOOLEAN.of(true),
						XacmlTypes.BOOLEAN.of(false),
						XacmlTypes.BOOLEAN.of(true)));
		verify(context);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testNOfFunctionInderterminate() throws EvaluationException
	{
		IntegerValue n = XacmlTypes.INTEGER.of(4);
		replay(context);
		assertEquals(XacmlTypes.BOOLEAN.of(false),
				LogicalFunctions.nof(context, n, XacmlTypes.BOOLEAN.of(false)));
		verify(context);
	}
}
