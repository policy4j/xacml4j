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

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.policy.FunctionInvocationException;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.FunctionInvocation;
import org.xacml4j.v30.policy.function.FunctionReturnTypeResolver;
import org.xacml4j.v30.policy.function.FunctionSpecBuilder;
import org.xacml4j.v30.types.XacmlTypes;
import org.xacml4j.v30.types.Value;

import com.google.common.collect.ImmutableList;

@ExtendWith(MockitoExtension.class)
public class DefaultFunctionSpecTest
{
	private FunctionInvocation invocation;
	private FunctionReturnTypeResolver resolver;
	private FunctionSpecBuilder b;
	private EvaluationContext context;

	@BeforeEach
	public void init(){
		this.invocation = mock(FunctionInvocation.class);
		this.resolver = mock(FunctionReturnTypeResolver.class);
		this.b = FunctionSpecBuilder.builder("testId");
		this.context = mock(EvaluationContext.class);
	}

	@Test
	public void testInvokeSpecWithListParamArguments() throws EvaluationException
	{
		List<Expression> params = ImmutableList.<Expression>builder()
		.add(XacmlTypes.BOOLEAN.ofAny(false))
		.build();
		FunctionSpec spec = b.param(XacmlTypes.BOOLEAN).build(resolver, invocation);
		when(context.isValidateFuncParamsAtRuntime()).thenReturn(false);
		when(invocation.invoke(spec, context, params)).thenReturn(XacmlTypes.BOOLEAN.ofAny(true));
		InOrder order = inOrder(invocation, resolver, context);
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true), spec.<Value>invokeWithList(context, params));
		order.verify(context).isValidateFuncParamsAtRuntime();
		order.verify(invocation).invoke(spec, context, params);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(context, times(1)).isValidateFuncParamsAtRuntime();
		verify(invocation, times(1)).invoke(spec, context, params);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(invocation, resolver, context);
	}

	@Test
	public void testInvokeSpecFailsWithInvocationException() throws EvaluationException
	{
		List<Expression> params = ImmutableList.<Expression>builder()
		.add(XacmlTypes.BOOLEAN.ofAny(false))
		.build();
		FunctionSpec spec = b.param(XacmlTypes.BOOLEAN).build(resolver, invocation);
		when(context.isValidateFuncParamsAtRuntime()).thenReturn(false);
		when(invocation.invoke(spec, context, params)).thenThrow(new FunctionInvocationException(spec, "Fail"));
		InOrder order = inOrder(invocation, resolver, context);
		assertThrows(FunctionInvocationException.class, () -> spec.invokeWithList(context, params));
		order.verify(context).isValidateFuncParamsAtRuntime();
		order.verify(invocation).invoke(spec, context, params);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(context, times(1)).isValidateFuncParamsAtRuntime();
		verify(invocation, times(1)).invoke(spec, context, params);
		verifyNoMoreInteractions(invocation, resolver);
	}

	@Test
	public void testInvokeSpecFailsWithRuntimeException() throws EvaluationException
	{
		List<Expression> params = ImmutableList.<Expression>builder()
		.add(XacmlTypes.BOOLEAN.ofAny(false))
		.build();
		FunctionSpec spec = b.param(XacmlTypes.BOOLEAN).build(resolver, invocation);
		when(context.isValidateFuncParamsAtRuntime()).thenReturn(false);
		when(invocation.invoke(spec, context, params)).thenThrow(new NullPointerException("Fail"));
		InOrder order = inOrder(invocation, resolver, context);
		assertThrows(FunctionInvocationException.class, () -> spec.invokeWithList(context, params));
		order.verify(context).isValidateFuncParamsAtRuntime();
		order.verify(invocation).invoke(spec, context, params);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(context, times(1)).isValidateFuncParamsAtRuntime();
		verify(invocation, times(1)).invoke(spec, context, params);
		verifyNoMoreInteractions(invocation, resolver);
	}
	
	@Test
	public void testOptionalParameterWithDefaultValue() throws EvaluationException
	{
		List<Expression> params = ImmutableList.<Expression>builder()
		.add(XacmlTypes.BOOLEAN.ofAny(false))
		.build();
		FunctionSpec spec = b.param(XacmlTypes.BOOLEAN).optional(XacmlTypes.BOOLEAN, XacmlTypes.BOOLEAN.ofAny(true)).build(XacmlTypes.BOOLEAN, invocation);
		when(context.isValidateFuncParamsAtRuntime()).thenReturn(true);
		when(invocation.invoke(spec, context, ImmutableList.<Expression>of(XacmlTypes.BOOLEAN.ofAny(false), XacmlTypes.BOOLEAN.ofAny(true)))).thenReturn(XacmlTypes.BOOLEAN.ofAny(false));
		InOrder order = inOrder(invocation, resolver, context);
		spec.invokeWithList(context, params);
		order.verify(context).isValidateFuncParamsAtRuntime();
		order.verify(invocation).invoke(spec, context, ImmutableList.<Expression>of(XacmlTypes.BOOLEAN.ofAny(false), XacmlTypes.BOOLEAN.ofAny(true)));
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(context, times(1)).isValidateFuncParamsAtRuntime();
		verify(invocation, times(1)).invoke(spec, context, ImmutableList.<Expression>of(XacmlTypes.BOOLEAN.ofAny(false), XacmlTypes.BOOLEAN.ofAny(true)));
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(invocation, resolver, context);
	}
	
	@Test
	public void testOptionalParameterWithNoDefaultValue() throws EvaluationException
	{
		List<Expression> params = ImmutableList.<Expression>builder()
		.add(XacmlTypes.BOOLEAN.ofAny(false))
		.build();
		FunctionSpec spec = b.param(XacmlTypes.BOOLEAN).optional(XacmlTypes.BOOLEAN).build(XacmlTypes.BOOLEAN, invocation);
		when(context.isValidateFuncParamsAtRuntime()).thenReturn(true);
		when(invocation.invoke(spec, context, Arrays.<Expression>asList(XacmlTypes.BOOLEAN.ofAny(false), null))).thenReturn(XacmlTypes.BOOLEAN.ofAny(false));
		InOrder order = inOrder(invocation, resolver, context);
		spec.invokeWithList(context, params);
		order.verify(context).isValidateFuncParamsAtRuntime();
		order.verify(invocation).invoke(spec, context, Arrays.<Expression>asList(XacmlTypes.BOOLEAN.ofAny(false), null));
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(context, times(1)).isValidateFuncParamsAtRuntime();
		verify(invocation, times(1)).invoke(spec, context, Arrays.<Expression>asList(XacmlTypes.BOOLEAN.ofAny(false), null));
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(invocation, resolver, context);
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
		when(context.isValidateFuncParamsAtRuntime()).thenReturn(true);
		when(invocation.invoke(spec, context,
				Arrays.<Expression>asList(XacmlTypes.BOOLEAN.ofAny(false), null, null))).thenReturn(XacmlTypes.BOOLEAN.ofAny(false));
		InOrder order = inOrder(invocation, resolver, context);
		spec.invokeWithList(context, params);
		order.verify(context).isValidateFuncParamsAtRuntime();
		order.verify(invocation).invoke(spec, context,
				Arrays.<Expression>asList(XacmlTypes.BOOLEAN.ofAny(false), null, null));
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(context, times(1)).isValidateFuncParamsAtRuntime();
		verify(invocation, times(1)).invoke(spec, context,
				Arrays.<Expression>asList(XacmlTypes.BOOLEAN.ofAny(false), null, null));
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(invocation, resolver, context);
	}
	
	@Test
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
		when(context.isValidateFuncParamsAtRuntime()).thenReturn(true);
		InOrder order = inOrder(invocation, resolver, context);
		assertThrows(FunctionInvocationException.class, () -> spec.invokeWithList(context, params));
		order.verify(context).isValidateFuncParamsAtRuntime();
		verify(invocation, never()).invoke(spec, context,
				Arrays.<Expression>asList(XacmlTypes.BOOLEAN.ofAny(false), XacmlTypes.BOOLEAN.ofAny(true), null));
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(context, times(1)).isValidateFuncParamsAtRuntime();
		verifyNoMoreInteractions(invocation, resolver);
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
		when(context.isValidateFuncParamsAtRuntime()).thenReturn(true);
		when(invocation.invoke(spec, context,
				Arrays.<Expression>asList(null, XacmlTypes.BOOLEAN.ofAny(true))))
				.thenReturn(XacmlTypes.BOOLEAN.ofAny(false));
		InOrder order = inOrder(invocation, resolver, context);
		spec.invokeWithList(context, params);
		order.verify(context).isValidateFuncParamsAtRuntime();
		order.verify(invocation).invoke(spec, context,
				Arrays.<Expression>asList(null, XacmlTypes.BOOLEAN.ofAny(true)));
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(context, times(1)).isValidateFuncParamsAtRuntime();
		verify(invocation, times(1)).invoke(spec, context,
				Arrays.<Expression>asList(null, XacmlTypes.BOOLEAN.ofAny(true)));
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(invocation, resolver, context);
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
		when(context.isValidateFuncParamsAtRuntime()).thenReturn(true);
		when(invocation.invoke(spec, context,
				Arrays.<Expression>asList(XacmlTypes.INTEGER.ofAny(10), XacmlTypes.BOOLEAN.ofAny(false))))
				.thenReturn(XacmlTypes.BOOLEAN.ofAny(false));
		InOrder order = inOrder(invocation, resolver, context);
		spec.invokeWithList(context, params);
		order.verify(context).isValidateFuncParamsAtRuntime();
		order.verify(invocation).invoke(spec, context,
				Arrays.<Expression>asList(XacmlTypes.INTEGER.ofAny(10), XacmlTypes.BOOLEAN.ofAny(false)));
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(context, times(1)).isValidateFuncParamsAtRuntime();
		verify(invocation, times(1)).invoke(spec, context,
				Arrays.<Expression>asList(XacmlTypes.INTEGER.ofAny(10), XacmlTypes.BOOLEAN.ofAny(false)));
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(invocation, resolver, context);
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
		when(context.isValidateFuncParamsAtRuntime()).thenReturn(true);
		when(invocation.invoke(spec, context,
				Arrays.<Expression>asList(null, XacmlTypes.INTEGER.ofAny(10))))
				.thenReturn(XacmlTypes.BOOLEAN.ofAny(false));
		InOrder order = inOrder(invocation, resolver, context);
		spec.invokeWithList(context, params);
		order.verify(context).isValidateFuncParamsAtRuntime();
		order.verify(invocation).invoke(spec, context,
				Arrays.<Expression>asList(null, XacmlTypes.INTEGER.ofAny(10)));
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(context, times(1)).isValidateFuncParamsAtRuntime();
		verify(invocation, times(1)).invoke(spec, context,
				Arrays.<Expression>asList(null, XacmlTypes.INTEGER.ofAny(10)));
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(invocation, resolver, context);
	}
}
