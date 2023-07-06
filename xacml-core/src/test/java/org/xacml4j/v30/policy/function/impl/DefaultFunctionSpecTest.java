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
import org.xacml4j.v30.policy.FunctionInvocationException;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.FunctionInvocation;
import org.xacml4j.v30.policy.function.FunctionReturnTypeResolver;
import org.xacml4j.v30.policy.function.FunctionSpecBuilder;
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
		.add(XacmlTypes.BOOLEAN.ofAny(false))
		.build();
		FunctionSpec spec = b.param(XacmlTypes.BOOLEAN).build(resolver, invocation);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
		expect(invocation.invoke(spec, context, params)).andReturn(XacmlTypes.BOOLEAN.ofAny(true));
		c.replay();
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true), spec.invokeWithList(context, params));
		c.verify();
	}

	@Test(expected=FunctionInvocationException.class)
	public void testInvokeSpecFailsWithInvocationException() throws EvaluationException
	{
		List<Expression> params = ImmutableList.<Expression>builder()
		.add(XacmlTypes.BOOLEAN.ofAny(false))
		.build();
		FunctionSpec spec = b.param(XacmlTypes.BOOLEAN).build(resolver, invocation);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
		expect(invocation.invoke(spec, context, params)).andThrow(new FunctionInvocationException(spec, "Fail"));
		c.replay();
		spec.invokeWithList(context, params);
		c.verify();
	}

	@Test(expected=FunctionInvocationException.class)
	public void testInvokeSpecFailsWithRuntimeException() throws EvaluationException
	{
		List<Expression> params = ImmutableList.<Expression>builder()
		.add(XacmlTypes.BOOLEAN.ofAny(false))
		.build();
		FunctionSpec spec = b.param(XacmlTypes.BOOLEAN).build(resolver, invocation);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
		expect(invocation.invoke(spec, context, params)).andThrow(new NullPointerException("Fail"));
		c.replay();
		spec.invokeWithList(context, params);
		c.verify();
	}
	
	@Test
	public void testOptionalParameterWithDefaultValue() throws EvaluationException
	{
		List<Expression> params = ImmutableList.<Expression>builder()
		.add(XacmlTypes.BOOLEAN.ofAny(false))
		.build();
		FunctionSpec spec = b.param(XacmlTypes.BOOLEAN).optional(XacmlTypes.BOOLEAN, XacmlTypes.BOOLEAN.ofAny(true)).build(XacmlTypes.BOOLEAN, invocation);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(invocation.invoke(spec, context, ImmutableList.<Expression>of(XacmlTypes.BOOLEAN.ofAny(false), XacmlTypes.BOOLEAN.ofAny(true)))).andReturn(XacmlTypes.BOOLEAN.ofAny(false));
		c.replay();
		spec.invokeWithList(context, params);
		c.verify();
	}
	
	@Test
	public void testOptionalParameterWithNoDefaultValue() throws EvaluationException
	{
		List<Expression> params = ImmutableList.<Expression>builder()
		.add(XacmlTypes.BOOLEAN.ofAny(false))
		.build();
		FunctionSpec spec = b.param(XacmlTypes.BOOLEAN).optional(XacmlTypes.BOOLEAN).build(XacmlTypes.BOOLEAN, invocation);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(invocation.invoke(spec, context, Arrays.<Expression>asList(XacmlTypes.BOOLEAN.ofAny(false), null))).andReturn(XacmlTypes.BOOLEAN.ofAny(false));
		c.replay();
		spec.invokeWithList(context, params);
		c.verify();
	}
	
	@Test
	public void testOptionalParameterWithNoDefaultValueAndVarArg() throws EvaluationException
	{
		List<Expression> params = ImmutableList.<Expression>builder()
		.add(XacmlTypes.BOOLEAN.ofAny(false))
		.build();
		FunctionSpec spec = b
				.param(XacmlTypes.BOOLEAN)
				.optional(XacmlTypes.BOOLEAN)
				.varArg(XacmlTypes.BOOLEAN, 0, 2)
				.build(XacmlTypes.BOOLEAN, invocation);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(invocation.invoke(spec, context, 
				Arrays.<Expression>asList(XacmlTypes.BOOLEAN.ofAny(false), null, null))).andReturn(XacmlTypes.BOOLEAN.ofAny(false));
		c.replay();
		spec.invokeWithList(context, params);
		c.verify();
	}
	
	@Test(expected=FunctionInvocationException.class)
	public void testOptionalParameterWithDefaultValueAndVarArg() throws EvaluationException
	{
		List<Expression> params = ImmutableList.<Expression>builder()
		.add(XacmlTypes.BOOLEAN.ofAny(false))
		.build();
		FunctionSpec spec = b
				.param(XacmlTypes.BOOLEAN)
				.optional(XacmlTypes.BOOLEAN, XacmlTypes.BOOLEAN.ofAny(true))
				.varArg(XacmlTypes.BOOLEAN, 1, 2)
				.build(XacmlTypes.BOOLEAN, invocation);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(invocation.invoke(spec, context, 
				Arrays.<Expression>asList(XacmlTypes.BOOLEAN.ofAny(false), XacmlTypes.BOOLEAN.ofAny(true), null))).andReturn(XacmlTypes.BOOLEAN.ofAny(false));
		c.replay();
		spec.invokeWithList(context, params);
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
				.optional(XacmlTypes.BOOLEAN, XacmlTypes.BOOLEAN.ofAny(true))
				.build(XacmlTypes.BOOLEAN, invocation);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(invocation.invoke(spec, context, 
				Arrays.<Expression>asList(null, XacmlTypes.BOOLEAN.ofAny(true))))
				.andReturn(XacmlTypes.BOOLEAN.ofAny(false));
		c.replay();
		spec.invokeWithList(context, params);
		c.verify();
	}
	
	@Test
	public void test2OptionalParameters1() throws EvaluationException
	{
		List<Expression> params = ImmutableList
				.<Expression>builder()
				.add(XacmlTypes.INTEGER.ofAny(10))
				.add(XacmlTypes.BOOLEAN.ofAny(false))
				.build();
		FunctionSpec spec = b
				.optional(XacmlTypes.INTEGER)
				.optional(XacmlTypes.BOOLEAN, XacmlTypes.BOOLEAN.ofAny(true))
				.build(XacmlTypes.BOOLEAN, invocation);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(invocation.invoke(spec, context, 
				Arrays.<Expression>asList(XacmlTypes.INTEGER.ofAny(10), XacmlTypes.BOOLEAN.ofAny(false))))
				.andReturn(XacmlTypes.BOOLEAN.ofAny(false));
		c.replay();
		spec.invokeWithList(context, params);
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
				.optional(XacmlTypes.INTEGER, XacmlTypes.INTEGER.ofAny(10))
				.build(XacmlTypes.BOOLEAN, invocation);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(true);
		expect(invocation.invoke(spec, context, 
				Arrays.<Expression>asList(null, XacmlTypes.INTEGER.ofAny(10))))
				.andReturn(XacmlTypes.BOOLEAN.ofAny(false));
		c.replay();
		spec.invokeWithList(context, params);
		c.verify();
	}
}
