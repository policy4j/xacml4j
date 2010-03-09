package com.artagon.xacml.v3.policy.impl;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.Expression;
import com.artagon.xacml.v3.policy.FunctionInvocation;
import com.artagon.xacml.v3.policy.FunctionInvocationException;
import com.artagon.xacml.v3.policy.FunctionReturnTypeResolver;
import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.policy.FunctionSpecBuilder;
import com.artagon.xacml.v3.policy.type.DataTypes;


public class DefaultFunctionSpecTest
{
	private FunctionInvocation invocation;
	private FunctionReturnTypeResolver resolver;
	private FunctionSpecBuilder b;
	private EvaluationContext context;
	
	@Before
	public void init(){
		this.invocation = createStrictMock(FunctionInvocation.class);
		this.resolver = createStrictMock(FunctionReturnTypeResolver.class);
		this.b = new DefaultFunctionSpecBuilder("testId");
		this.context = createStrictMock(EvaluationContext.class);
	}
	
	@Test
	public void testInvokeSpec() throws EvaluationException
	{
		Expression[] params = {DataTypes.BOOLEAN.create(false)};
		FunctionSpec spec = b.withParam(DataTypes.BOOLEAN.getType()).build(resolver, invocation);
		expect(invocation.invoke(spec, context, params)).andReturn(DataTypes.BOOLEAN.create(true));
		replay(invocation);
		replay(resolver);
		assertEquals(DataTypes.BOOLEAN.create(true), spec.invoke(context, params));
	}
	
	@Test(expected=FunctionInvocationException.class)
	public void testInvokeSpecFailsWithInvocationException() throws EvaluationException
	{
		Expression[] params = {DataTypes.BOOLEAN.create(false)};
		FunctionSpec spec = b.withParam(DataTypes.BOOLEAN.getType()).build(resolver, invocation);
		expect(invocation.invoke(spec, context, params)).andThrow(new FunctionInvocationException(spec, "Fail"));
		replay(invocation);
		replay(resolver);
		spec.invoke(context, params);
	}
	
	@Test(expected=FunctionInvocationException.class)
	public void testInvokeSpecFailsWithRuntimeException() throws EvaluationException
	{
		Expression[] params = {DataTypes.BOOLEAN.create(false)};
		FunctionSpec spec = b.withParam(DataTypes.BOOLEAN.getType()).build(resolver, invocation);
		expect(invocation.invoke(spec, context, params)).andThrow(new NullPointerException("Fail"));
		replay(invocation);
		replay(resolver);
		spec.invoke(context, params);
	}
}
