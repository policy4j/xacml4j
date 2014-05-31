
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

import java.util.List;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xacml4j.util.Invocation;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueExpression;
import org.xacml4j.v30.pdp.FunctionInvocationException;
import org.xacml4j.v30.pdp.FunctionSpec;
import org.xacml4j.v30.types.IntegerExp;
import org.xacml4j.v30.types.StringExp;

import com.google.common.collect.ImmutableList;

public class DefaultFunctionInocationTest
{
	private Invocation<ValueExpression> inv0;
	private Invocation<ValueExpression> inv1;
	private FunctionSpec spec;
	private IMocksControl c;
	private FunctionInvocation f0;
	private FunctionInvocation f1;
	private EvaluationContext context;

	@SuppressWarnings("unchecked")
	@Before
	public void init(){
		this.c = createControl();
		this.inv0 = c.createMock(Invocation.class);
		this.inv1 = c.createMock(Invocation.class);
		this.spec = c.createMock(FunctionSpec.class);
		this.f0 = new DefaultFunctionInvocation(inv0, false);
		this.f1 = new DefaultFunctionInvocation(inv1, true);
		this.context = c.createMock(EvaluationContext.class);
	}

	@Test
	public void testInvokeNoEvaluationContextFuncionIsNotVariadic() throws Exception
	{
		List<Expression>  p = ImmutableList.<Expression>builder()
		.add(IntegerExp.valueOf(1))
		.add(IntegerExp.valueOf(2))
		.build();
		expect(spec.getNumberOfParams()).andReturn(2);
		expect(spec.isVariadic()).andReturn(false).times(2);
		expect(inv0.invoke(p.toArray())).andReturn(IntegerExp.valueOf(1));
		c.replay();
		ValueExpression v = f0.invoke(spec, context, p);
		assertEquals(IntegerExp.valueOf(1), v);
		c.verify();
	}

	@Test(expected=FunctionInvocationException.class)
	public void testInvokeInvocationThrowsRuntimeException() throws Exception
	{
		List<Expression>  p = ImmutableList.<Expression>builder()
		.add(IntegerExp.valueOf(1))
		.add(IntegerExp.valueOf(2))
		.build();
		expect(spec.getNumberOfParams()).andReturn(2);
		expect(spec.isVariadic()).andReturn(false).times(2);
		expect(inv0.invoke(p.toArray())).andThrow(new RuntimeException());
		c.replay();
		f0.invoke(spec, context, p);
		c.verify();
	}

	@Test
	public void testInvokeWithEvaluationContextFuncionIsNotVariadic() throws Exception
	{
		List<Expression>  p = ImmutableList.<Expression>builder()
		.add(IntegerExp.valueOf(1))
		.add(IntegerExp.valueOf(2))
		.build();
		expect(spec.getNumberOfParams()).andReturn(2);
		expect(spec.isVariadic()).andReturn(false).times(2);
		expect(inv1.invoke(new Object[]{context,
				IntegerExp.valueOf(1),
				IntegerExp.valueOf(2)})).andReturn(
						IntegerExp.valueOf(1));
		c.replay();
		ValueExpression v = f1.invoke(spec, context, p);
		assertEquals(IntegerExp.valueOf(1), v);
		c.verify();
	}

	@Test
	@Ignore
	public void testInvokeWithEvaluationContextFuncionIsVariadic() throws Exception
	{
		List<Expression>  p = ImmutableList.<Expression>builder()
		.add(IntegerExp.valueOf(1))
		.add(IntegerExp.valueOf(2))
		.add(StringExp.valueOf("aaa"))
		.add(StringExp.valueOf("bbb"))
		.build();
		Object[] pArray = new Object[]{
				context,
				IntegerExp.valueOf(1),
				IntegerExp.valueOf(2),
				new Object[]{
					StringExp.valueOf("aaa"),
					StringExp.valueOf("bbb")}
				};
		expect(spec.getNumberOfParams()).andReturn(3);
		expect(spec.isVariadic()).andReturn(true).times(2);
		expect(inv1.invoke(pArray))
		.andReturn(IntegerExp.valueOf(1));
		c.replay();
		ValueExpression v = f1.invoke(spec, context, p);
		assertEquals(IntegerExp.valueOf(1), v);
		c.verify();
	}

	@Test
	@Ignore
	public void testInvokeWithNoEvaluationContextFuncionIsVariadicAndMoreThanZeroVariadicParams() throws Exception
	{
		List<Expression>  p = ImmutableList.<Expression>builder()
		.add(IntegerExp.valueOf(1))
		.add(IntegerExp.valueOf(2))
		.add(StringExp.valueOf("aaa"))
		.add(StringExp.valueOf("bbb"))
		.build();
		Object[] pArray = new Object[]{
				IntegerExp.valueOf(1),
				IntegerExp.valueOf(2),
				new Object[]{
					StringExp.valueOf("aaa"),
					StringExp.valueOf("bbb")}
				};
		expect(spec.getNumberOfParams()).andReturn(3);
		expect(spec.isVariadic()).andReturn(true).times(2);
		expect(inv0.invoke(pArray)).andReturn(IntegerExp.valueOf(1));
		c.replay();
		ValueExpression v = f0.invoke(spec, context, p);
		assertEquals(IntegerExp.valueOf(1), v);
		c.verify();
	}

	@Test
	public void testInvokeWithNoEvaluationContextFuncionIsVariadicAndZeroVariadicParams() throws Exception
	{
		List<Expression>  p = ImmutableList.<Expression>builder()
		.add(IntegerExp.valueOf(1))
		.add(IntegerExp.valueOf(2))
		.build();
		Object[] pArray = new Object[]{
				IntegerExp.valueOf(1),
				IntegerExp.valueOf(2),
				null};
		expect(spec.getNumberOfParams()).andReturn(3);
		expect(spec.isVariadic()).andReturn(true).times(2);
		expect(inv0.invoke(pArray)).andReturn(IntegerExp.valueOf(1));
		c.replay();
		ValueExpression v = f0.invoke(spec, context, p);
		assertEquals(IntegerExp.valueOf(1), v);
		c.verify();
	}
}
