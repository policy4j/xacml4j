package com.artagon.xacml.v3;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.types.XacmlDataTypes;


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
		this.b = new FunctionSpecBuilder("testId");
		this.context = createStrictMock(EvaluationContext.class);
	}
	
	@Test
	public void testInvokeSpec() throws EvaluationException
	{
		Expression[] params = {XacmlDataTypes.BOOLEAN.create(false)};
		FunctionSpec spec = b.withParam(XacmlDataTypes.BOOLEAN.getType()).build(resolver, invocation);
		expect(invocation.invoke(spec, context, params)).andReturn(XacmlDataTypes.BOOLEAN.create(true));
		replay(invocation);
		replay(resolver);
		assertEquals(XacmlDataTypes.BOOLEAN.create(true), spec.invoke(context, params));
	}
	
	@Test(expected=FunctionInvocationException.class)
	public void testInvokeSpecFailsWithInvocationException() throws EvaluationException
	{
		Expression[] params = {XacmlDataTypes.BOOLEAN.create(false)};
		FunctionSpec spec = b.withParam(XacmlDataTypes.BOOLEAN.getType()).build(resolver, invocation);
		expect(invocation.invoke(spec, context, params)).andThrow(new FunctionInvocationException(context, spec, "Fail"));
		replay(invocation);
		replay(resolver);
		spec.invoke(context, params);
	}
	
	@Test(expected=FunctionInvocationException.class)
	public void testInvokeSpecFailsWithRuntimeException() throws EvaluationException
	{
		Expression[] params = {XacmlDataTypes.BOOLEAN.create(false)};
		FunctionSpec spec = b.withParam(XacmlDataTypes.BOOLEAN.getType()).build(resolver, invocation);
		expect(invocation.invoke(spec, context, params)).andThrow(new NullPointerException("Fail"));
		replay(invocation);
		replay(resolver);
		spec.invoke(context, params);
	}
}
