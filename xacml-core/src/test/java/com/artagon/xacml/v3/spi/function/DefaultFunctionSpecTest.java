package com.artagon.xacml.v3.spi.function;

import static com.artagon.xacml.v3.types.BooleanType.BOOLEAN;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.policy.FunctionInvocationException;
import com.artagon.xacml.v3.policy.FunctionSpec;



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
		this.b = FunctionSpecBuilder.create("testId");
		this.context = createStrictMock(EvaluationContext.class);
	}
	
	@Test
	public void testInvokeSpec() throws EvaluationException
	{
		Expression[] params = {BOOLEAN.create(false)};
		FunctionSpec spec = b.withParam(BOOLEAN).build(resolver, invocation);
		expect(invocation.invoke(spec, context, params)).andReturn(BOOLEAN.create(true));
		replay(invocation);
		replay(resolver);
		assertEquals(BOOLEAN.create(true), spec.invoke(context, params));
	}
	
	@Test(expected=FunctionInvocationException.class)
	public void testInvokeSpecFailsWithInvocationException() throws EvaluationException
	{
		Expression[] params = {BOOLEAN.create(false)};
		FunctionSpec spec = b.withParam(BOOLEAN).build(resolver, invocation);
		expect(invocation.invoke(spec, context, params)).andThrow(new FunctionInvocationException(context, spec, "Fail"));
		replay(invocation);
		replay(resolver);
		spec.invoke(context, params);
	}
	
	@Test(expected=FunctionInvocationException.class)
	public void testInvokeSpecFailsWithRuntimeException() throws EvaluationException
	{
		Expression[] params = {BOOLEAN.create(false)};
		FunctionSpec spec = b.withParam(BOOLEAN).build(resolver, invocation);
		expect(invocation.invoke(spec, context, params)).andThrow(new NullPointerException("Fail"));
		replay(invocation);
		replay(resolver);
		spec.invoke(context, params);
	}
}
