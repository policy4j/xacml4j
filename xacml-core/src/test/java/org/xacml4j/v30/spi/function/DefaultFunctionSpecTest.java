package org.xacml4j.v30.spi.function;

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

import java.util.Arrays;
import java.util.List;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.pdp.FunctionInvocationException;
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.IntegerExp;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.ImmutableList;

public class DefaultFunctionSpecTest
{
	private FunctionInvocation invocation;
	private FunctionReturnTypeResolver resolver;
	private FunctionSpecBuilder b;
	private EvaluationContext context;
	private IMocksControl c;

	@Before
	public void init(){
		this.c = createControl();
		this.invocation = c.createMock(FunctionInvocation.class);
		this.resolver = c.createMock(FunctionReturnTypeResolver.class);
		this.b = FunctionSpecBuilder.builder("testId");
		this.context = c.createMock(EvaluationContext.class);
	}

	@Test
	public void testInvokeSpecWithListParamArguments() throws EvaluationException
	{
		List<Expression> params = ImmutableList.<Expression>builder()
		.add(BooleanExp.valueOf(false))
		.build();
		FunctionSpec spec = b.param(XacmlTypes.BOOLEAN).build(resolver, invocation);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
		expect(invocation.invoke(spec, context, params)).andReturn(BooleanExp.valueOf(true));
		c.replay();
		assertEquals(BooleanExp.valueOf(true), spec.invoke(context, params));
		c.verify();
	}

	@Test(expected=FunctionInvocationException.class)
	public void testInvokeSpecFailsWithInvocationException() throws EvaluationException
	{
		List<Expression> params = ImmutableList.<Expression>builder()
		.add(BooleanExp.valueOf(false))
		.build();
		FunctionSpec spec = b.param(XacmlTypes.BOOLEAN).build(resolver, invocation);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
		expect(invocation.invoke(spec, context, params)).andThrow(new FunctionInvocationException(spec, "Fail"));
		c.replay();
		spec.invoke(context, params);
		c.verify();
	}

	@Test(expected=FunctionInvocationException.class)
	public void testInvokeSpecFailsWithRuntimeException() throws EvaluationException
	{
		List<Expression> params = ImmutableList.<Expression>builder()
		.add(BooleanExp.valueOf(false))
		.build();
		FunctionSpec spec = b.param(XacmlTypes.BOOLEAN).build(resolver, invocation);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
		expect(invocation.invoke(spec, context, params)).andThrow(new NullPointerException("Fail"));
		c.replay();
		spec.invoke(context, params);
		c.verify();
	}
	
	@Test
	public void testOptionalParameterWithDefaultValue() throws EvaluationException
	{
		List<Expression> params = ImmutableList.<Expression>builder()
		.add(BooleanExp.valueOf(false))
		.build();
		FunctionSpec spec = b.param(XacmlTypes.BOOLEAN).optional(XacmlTypes.BOOLEAN, BooleanExp.valueOf(true)).build(XacmlTypes.BOOLEAN, invocation);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(invocation.invoke(spec, context, ImmutableList.<Expression>of(BooleanExp.FALSE, BooleanExp.TRUE))).andReturn(BooleanExp.valueOf(false));
		c.replay();
		spec.invoke(context, params);
		c.verify();
	}
	
	@Test
	public void testOptionalParameterWithNoDefaultValue() throws EvaluationException
	{
		List<Expression> params = ImmutableList.<Expression>builder()
		.add(BooleanExp.valueOf(false))
		.build();
		FunctionSpec spec = b.param(XacmlTypes.BOOLEAN).optional(XacmlTypes.BOOLEAN).build(XacmlTypes.BOOLEAN, invocation);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(invocation.invoke(spec, context, Arrays.<Expression>asList(BooleanExp.FALSE, null))).andReturn(BooleanExp.valueOf(false));
		c.replay();
		spec.invoke(context, params);
		c.verify();
	}
	
	@Test
	public void testOptionalParameterWithNoDefaultValueAndVarArg() throws EvaluationException
	{
		List<Expression> params = ImmutableList.<Expression>builder()
		.add(BooleanExp.valueOf(false))
		.build();
		FunctionSpec spec = b
				.param(XacmlTypes.BOOLEAN)
				.optional(XacmlTypes.BOOLEAN)
				.varArg(XacmlTypes.BOOLEAN, 0, 2)
				.build(XacmlTypes.BOOLEAN, invocation);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(invocation.invoke(spec, context, 
				Arrays.<Expression>asList(BooleanExp.FALSE, null, null))).andReturn(BooleanExp.valueOf(false));
		c.replay();
		spec.invoke(context, params);
		c.verify();
	}
	
	@Test(expected=FunctionInvocationException.class)
	public void testOptionalParameterWithDefaultValueAndVarArg() throws EvaluationException
	{
		List<Expression> params = ImmutableList.<Expression>builder()
		.add(BooleanExp.valueOf(false))
		.build();
		FunctionSpec spec = b
				.param(XacmlTypes.BOOLEAN)
				.optional(XacmlTypes.BOOLEAN, BooleanExp.TRUE)
				.varArg(XacmlTypes.BOOLEAN, 1, 2)
				.build(XacmlTypes.BOOLEAN, invocation);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(invocation.invoke(spec, context, 
				Arrays.<Expression>asList(BooleanExp.FALSE, BooleanExp.TRUE, null))).andReturn(BooleanExp.valueOf(false));
		c.replay();
		spec.invoke(context, params);
		c.verify();
	}
	
	@Test
	public void test2OptionalParameters() throws EvaluationException
	{
		List<Expression> params = ImmutableList
				.<Expression>builder()
				.build();
		FunctionSpec spec = b
				.optional(XacmlTypes.INTEGER)
				.optional(XacmlTypes.BOOLEAN, BooleanExp.TRUE)
				.build(XacmlTypes.BOOLEAN, invocation);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(invocation.invoke(spec, context, 
				Arrays.<Expression>asList(null, BooleanExp.TRUE)))
				.andReturn(BooleanExp.valueOf(false));
		c.replay();
		spec.invoke(context, params);
		c.verify();
	}
	
	@Test
	public void test2OptionalParameters1() throws EvaluationException
	{
		List<Expression> params = ImmutableList
				.<Expression>builder()
				.add(IntegerExp.valueOf(10))
				.add(BooleanExp.FALSE)
				.build();
		FunctionSpec spec = b
				.optional(XacmlTypes.INTEGER)
				.optional(XacmlTypes.BOOLEAN, BooleanExp.TRUE)
				.build(XacmlTypes.BOOLEAN, invocation);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(invocation.invoke(spec, context, 
				Arrays.<Expression>asList(IntegerExp.valueOf(10), BooleanExp.FALSE)))
				.andReturn(BooleanExp.valueOf(false));
		c.replay();
		spec.invoke(context, params);
		c.verify();
	}
	
	@Test
	public void test2OptionalParametersTheSameType() throws EvaluationException
	{
		List<Expression> params = ImmutableList
				.<Expression>builder()
				.build();
		FunctionSpec spec = b
				.optional(XacmlTypes.INTEGER)
				.optional(XacmlTypes.INTEGER, IntegerExp.valueOf(10))
				.build(XacmlTypes.BOOLEAN, invocation);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(invocation.invoke(spec, context, 
				Arrays.<Expression>asList(null, IntegerExp.valueOf(10))))
				.andReturn(BooleanExp.valueOf(false));
		c.replay();
		spec.invoke(context, params);
		c.verify();
	}
}
