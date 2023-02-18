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

import com.google.common.collect.ImmutableList;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueExpression;
import org.xacml4j.v30.policy.FunctionInvocationException;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.DefaultFunctionInvocation;
import org.xacml4j.v30.policy.function.FunctionInvocation;
import org.xacml4j.v30.policy.function.PolicyToPlatformFunctionInvocation;
import org.xacml4j.v30.types.XacmlTypes;

import java.util.Arrays;
import java.util.List;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

public class DefaultFunctionFunctionInvocationTest
{
	private PolicyToPlatformFunctionInvocation<ValueExpression> inv0;
	private PolicyToPlatformFunctionInvocation<ValueExpression> inv1;
	private FunctionSpec spec;
	private IMocksControl c;
	private FunctionInvocation f0;
	private FunctionInvocation f1;
	private EvaluationContext context;

	@SuppressWarnings("unchecked")
	@Before
	public void init(){
		this.c = createControl();
		this.inv0 = c.createMock(PolicyToPlatformFunctionInvocation.class);
		this.inv1 = c.createMock(PolicyToPlatformFunctionInvocation.class);
		this.spec = c.createMock(FunctionSpec.class);
		this.f0 = new DefaultFunctionInvocation(inv0, false);
		this.f1 = new DefaultFunctionInvocation(inv1, true);
		this.context = c.createMock(EvaluationContext.class);
	}

	@Test
	public void testInvokeNoEvaluationContextFunctionIsNotVariadic() throws Exception
	{
		List<Expression>  p = ImmutableList.<Expression>builder()
		.add(XacmlTypes.INTEGER.of(1))
		.add(XacmlTypes.INTEGER.of(2))
		.build();
		expect(spec.getNumberOfParams()).andReturn(2);
		expect(spec.isVariadic()).andReturn(false).times(2);
		expect(inv0.invoke(p.toArray())).andReturn(XacmlTypes.INTEGER.of(1));
		c.replay();
		ValueExpression v = f0.invoke(spec, context, p);
		assertEquals(XacmlTypes.INTEGER.of(1), v);
		c.verify();
	}

	@Test(expected=FunctionInvocationException.class)
	public void testInvokeInvocationThrowsRuntimeException() throws Exception
	{
		List<Expression>  p = ImmutableList.<Expression>builder()
		.add(XacmlTypes.INTEGER.of(1))
		.add(XacmlTypes.INTEGER.of(2))
		.build();
		expect(spec.getNumberOfParams()).andReturn(2);
		expect(spec.isVariadic()).andReturn(false).times(2);
		expect(inv0.invoke(p.toArray())).andThrow(new RuntimeException());
		c.replay();
		f0.invoke(spec, context, p);
		c.verify();
	}

	@Test
	public void testInvokeWithEvaluationContextFunctionIsNotVariadic() throws Exception
	{
		List<Expression>  p = ImmutableList.<Expression>builder()
		.add(XacmlTypes.INTEGER.of(1))
		.add(XacmlTypes.INTEGER.of(2))
		.build();
		List<Object> params = ImmutableList.builder().add(context).addAll(p).build();
		expect(spec.getNumberOfParams()).andReturn(2);
		expect(spec.isVariadic()).andReturn(false).times(2);
		expect(inv1.invoke(params.toArray()))
				.andReturn(XacmlTypes.INTEGER.of(1));
		c.replay();
		ValueExpression v = f1.invoke(spec, context, p);
		assertEquals(XacmlTypes.INTEGER.of(1), v);
		c.verify();
	}

	@Test
	public void testInvokeWithEvaluationContextFunctionIsVariadic() throws Exception
	{
		List<Expression>  p = ImmutableList.<Expression>builder()
		.add(XacmlTypes.INTEGER.of(1))
		.add(XacmlTypes.INTEGER.of(2))
		.add(XacmlTypes.STRING.of("aaa"))
		.add(XacmlTypes.STRING.of("bbb"))
		.build();
		Object[] pArray = new Object[]{
				context,
				XacmlTypes.INTEGER.of(1),
				XacmlTypes.INTEGER.of(2),
				new Object[]{
					XacmlTypes.STRING.of("aaa"),
					XacmlTypes.STRING.of("bbb")}
				};
		expect(spec.getNumberOfParams()).andReturn(3);
		expect(spec.isVariadic()).andReturn(true).times(2);
		expect(inv1.invoke(pArray))
		.andReturn(XacmlTypes.INTEGER.of(1));
		c.replay();
		ValueExpression v = f1.invoke(spec, context, p);
		assertEquals(XacmlTypes.INTEGER.of(1), v);
		c.verify();
	}

	@Test
	public void testInvokeWithNoEvaluationContextFunctionIsVariadicAndMoreThanZeroVariadicParams() throws Exception
	{
		List<Expression>  p = ImmutableList.<Expression>builder()
		.add(XacmlTypes.INTEGER.of(1))
		.add(XacmlTypes.INTEGER.of(2))
		.add(XacmlTypes.STRING.of("aaa"))
		.add(XacmlTypes.STRING.of("bbb"))
		.build();
		Object[] pArray = new Object[]{
				XacmlTypes.INTEGER.of(1),
				XacmlTypes.INTEGER.of(2),
				new Object[]{
					XacmlTypes.STRING.of("aaa"),
					XacmlTypes.STRING.of("bbb")}
				};
		expect(spec.getNumberOfParams()).andReturn(3);
		expect(spec.isVariadic()).andReturn(true).times(2);
		expect(inv0.invoke(pArray)).andReturn(XacmlTypes.INTEGER.of(1));
		c.replay();
		ValueExpression v = f0.invoke(spec, context, p);
		assertEquals(XacmlTypes.INTEGER.of(1), v);
		c.verify();
	}

	@Test
	public void testInvokeWithNoEvaluationContextFunctionIsVariadicAndZeroVariadicParams() throws Exception
	{
		List<Expression>  p = ImmutableList.<Expression>builder()
		.add(XacmlTypes.INTEGER.of(1))
		.add(XacmlTypes.INTEGER.of(2))
		.build();
		Object[] pArray = new Object[]{
				XacmlTypes.INTEGER.of(1),
				XacmlTypes.INTEGER.of(2),
				null};
		expect(spec.getNumberOfParams()).andReturn(3);
		expect(spec.isVariadic()).andReturn(true).times(2);
		expect(inv0.invoke(pArray)).andReturn(XacmlTypes.INTEGER.of(1));
		c.replay();
		ValueExpression v = f0.invoke(spec, context, p);
		assertEquals(XacmlTypes.INTEGER.of(1), v);
		c.verify();
	}
}
