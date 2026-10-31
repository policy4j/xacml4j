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

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.policy.FunctionInvocationException;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.FunctionInvocation;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.ImmutableList;

@ExtendWith(MockitoExtension.class)
public class DefaultFunctionFunctionInvocationTest
{
	private PolicyToPlatformFunctionInvocation<ValueExp> inv0;
	private PolicyToPlatformFunctionInvocation<ValueExp> inv1;
	private FunctionSpec spec;
	private FunctionInvocation f0;
	private FunctionInvocation f1;
	private EvaluationContext context;

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void init(){
		this.inv0 = mock(PolicyToPlatformFunctionInvocation.class);
		this.inv1 = mock(PolicyToPlatformFunctionInvocation.class);
		this.spec = mock(FunctionSpec.class);
		this.f0 = new DefaultFunctionInvocation(inv0, false);
		this.f1 = new DefaultFunctionInvocation(inv1, true);
		this.context = mock(EvaluationContext.class);
	}

	@Test
	public void testInvokeNoEvaluationContextFunctionIsNotVariadic() throws Exception
	{
		List<Expression>  p = ImmutableList.<Expression>builder()
		.add(XacmlTypes.INTEGER.ofAny(1))
		.add(XacmlTypes.INTEGER.ofAny(2))
		.build();
		when(spec.getNumberOfParams()).thenReturn(2);
		when(spec.isVariadic()).thenReturn(false);
		when(inv0.invoke(p.toArray())).thenReturn(XacmlTypes.INTEGER.ofAny(1));
		InOrder order = inOrder(inv0, inv1, spec, context);
		ValueExp v = f0.invoke(spec, context, p);
		assertEquals(XacmlTypes.INTEGER.ofAny(1), v);
		order.verify(spec).getNumberOfParams();
		order.verify(spec, times(2)).isVariadic();
		order.verify(inv0).invoke(p.toArray());
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(spec, times(1)).getNumberOfParams();
		verify(spec, times(2)).isVariadic();
		verify(inv0, times(1)).invoke(p.toArray());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(inv0, inv1, spec, context);
	}

	@Test
	public void testInvokeInvocationThrowsRuntimeException() throws Exception
	{
		List<Expression>  p = ImmutableList.<Expression>builder()
		.add(XacmlTypes.INTEGER.ofAny(1))
		.add(XacmlTypes.INTEGER.ofAny(2))
		.build();
		when(spec.getNumberOfParams()).thenReturn(2);
		when(spec.isVariadic()).thenReturn(false);
		when(inv0.invoke(p.toArray())).thenThrow(new RuntimeException());
		InOrder order = inOrder(inv0, inv1, spec, context);
		assertThrows(FunctionInvocationException.class, () -> f0.invoke(spec, context, p));
		order.verify(spec).getNumberOfParams();
		order.verify(spec, times(2)).isVariadic();
		order.verify(inv0).invoke(p.toArray());
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(spec, times(1)).getNumberOfParams();
		verify(spec, times(2)).isVariadic();
		verify(inv0, times(1)).invoke(p.toArray());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(inv0, inv1, spec, context);
	}

	@Test
	public void testInvokeWithEvaluationContextFunctionIsNotVariadic() throws Exception
	{
		List<Expression>  p = ImmutableList.<Expression>builder()
		.add(XacmlTypes.INTEGER.ofAny(1))
		.add(XacmlTypes.INTEGER.ofAny(2))
		.build();
		List<Object> params = ImmutableList.builder().add(context).addAll(p).build();
		when(spec.getNumberOfParams()).thenReturn(2);
		when(spec.isVariadic()).thenReturn(false);
		when(inv1.invoke(params.toArray()))
				.thenReturn(XacmlTypes.INTEGER.ofAny(1));
		InOrder order = inOrder(inv0, inv1, spec, context);
		ValueExp v = f1.invoke(spec, context, p);
		assertEquals(XacmlTypes.INTEGER.ofAny(1), v);
		order.verify(spec).getNumberOfParams();
		order.verify(spec, times(2)).isVariadic();
		order.verify(inv1).invoke(params.toArray());
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(spec, times(1)).getNumberOfParams();
		verify(spec, times(2)).isVariadic();
		verify(inv1, times(1)).invoke(params.toArray());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(inv0, inv1, spec, context);
	}

	@Test
	public void testInvokeWithEvaluationContextFunctionIsVariadic() throws Exception
	{
		List<Expression>  p = ImmutableList.<Expression>builder()
		.add(XacmlTypes.INTEGER.ofAny(1))
		.add(XacmlTypes.INTEGER.ofAny(2))
		.add(XacmlTypes.STRING.ofAny("aaa"))
		.add(XacmlTypes.STRING.ofAny("bbb"))
		.build();
		Object[] pArray = new Object[]{
				context,
				XacmlTypes.INTEGER.ofAny(1),
				XacmlTypes.INTEGER.ofAny(2),
				new Object[]{
					XacmlTypes.STRING.ofAny("aaa"),
					XacmlTypes.STRING.ofAny("bbb")}
				};
		when(spec.getNumberOfParams()).thenReturn(3);
		when(spec.isVariadic()).thenReturn(true);
		when(inv1.invoke(pArray))
		.thenReturn(XacmlTypes.INTEGER.ofAny(1));
		InOrder order = inOrder(inv0, inv1, spec, context);
		ValueExp v = f1.invoke(spec, context, p);
		assertEquals(XacmlTypes.INTEGER.ofAny(1), v);
		order.verify(spec).getNumberOfParams();
		order.verify(spec, times(2)).isVariadic();
		order.verify(inv1).invoke(pArray);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(spec, times(1)).getNumberOfParams();
		verify(spec, times(2)).isVariadic();
		verify(inv1, times(1)).invoke(pArray);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(inv0, inv1, spec, context);
	}

	@Test
	public void testInvokeWithNoEvaluationContextFunctionIsVariadicAndMoreThanZeroVariadicParams() throws Exception
	{
		List<Expression>  p = ImmutableList.<Expression>builder()
		.add(XacmlTypes.INTEGER.ofAny(1))
		.add(XacmlTypes.INTEGER.ofAny(2))
		.add(XacmlTypes.STRING.ofAny("aaa"))
		.add(XacmlTypes.STRING.ofAny("bbb"))
		.build();
		Object[] pArray = new Object[]{
				XacmlTypes.INTEGER.ofAny(1),
				XacmlTypes.INTEGER.ofAny(2),
				new Object[]{
					XacmlTypes.STRING.ofAny("aaa"),
					XacmlTypes.STRING.ofAny("bbb")}
				};
		when(spec.getNumberOfParams()).thenReturn(3);
		when(spec.isVariadic()).thenReturn(true);
		when(inv0.invoke(pArray)).thenReturn(XacmlTypes.INTEGER.ofAny(1));
		InOrder order = inOrder(inv0, inv1, spec, context);
		ValueExp v = f0.invoke(spec, context, p);
		assertEquals(XacmlTypes.INTEGER.ofAny(1), v);
		order.verify(spec).getNumberOfParams();
		order.verify(spec, times(2)).isVariadic();
		order.verify(inv0).invoke(pArray);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(spec, times(1)).getNumberOfParams();
		verify(spec, times(2)).isVariadic();
		verify(inv0, times(1)).invoke(pArray);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(inv0, inv1, spec, context);
	}

	@Test
	public void testInvokeWithNoEvaluationContextFunctionIsVariadicAndZeroVariadicParams() throws Exception
	{
		List<Expression>  p = ImmutableList.<Expression>builder()
		.add(XacmlTypes.INTEGER.ofAny(1))
		.add(XacmlTypes.INTEGER.ofAny(2))
		.build();
		Object[] pArray = new Object[]{
				XacmlTypes.INTEGER.ofAny(1),
				XacmlTypes.INTEGER.ofAny(2),
				null};
		when(spec.getNumberOfParams()).thenReturn(3);
		when(spec.isVariadic()).thenReturn(true);
		when(inv0.invoke(pArray)).thenReturn(XacmlTypes.INTEGER.ofAny(1));
		InOrder order = inOrder(inv0, inv1, spec, context);
		ValueExp v = f0.invoke(spec, context, p);
		assertEquals(XacmlTypes.INTEGER.ofAny(1), v);
		order.verify(spec).getNumberOfParams();
		order.verify(spec, times(2)).isVariadic();
		order.verify(inv0).invoke(pArray);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(spec, times(1)).getNumberOfParams();
		verify(spec, times(2)).isVariadic();
		verify(inv0, times(1)).invoke(pArray);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(inv0, inv1, spec, context);
	}
}
