package com.artagon.xacml.v30.spi.function;

import static com.artagon.xacml.v30.types.BooleanType.BOOLEAN;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.pdp.EvaluationContext;
import com.artagon.xacml.v30.pdp.EvaluationException;
import com.artagon.xacml.v30.pdp.Expression;
import com.artagon.xacml.v30.pdp.FunctionInvocationException;
import com.artagon.xacml.v30.pdp.FunctionSpec;
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
		.add(BOOLEAN.create(false))
		.build();
		FunctionSpec spec = b.withParam(BOOLEAN).build(resolver, invocation);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
		expect(invocation.invoke(spec, context, params)).andReturn(BOOLEAN.create(true));
		c.replay();
		assertEquals(BOOLEAN.create(true), spec.invoke(context, params));
		c.verify();
	}
	
	@Test(expected=FunctionInvocationException.class)
	public void testInvokeSpecFailsWithInvocationException() throws EvaluationException
	{
		List<Expression> params = ImmutableList.<Expression>builder()
		.add(BOOLEAN.create(false))
		.build();
		FunctionSpec spec = b.withParam(BOOLEAN).build(resolver, invocation);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
		expect(invocation.invoke(spec, context, params)).andThrow(new FunctionInvocationException(context, spec, "Fail"));
		c.replay();
		spec.invoke(context, params);
		c.verify();
	}
	
	@Test(expected=FunctionInvocationException.class)
	public void testInvokeSpecFailsWithRuntimeException() throws EvaluationException
	{
		List<Expression> params = ImmutableList.<Expression>builder()
		.add(BOOLEAN.create(false))
		.build();
		FunctionSpec spec = b.withParam(BOOLEAN).build(resolver, invocation);
		expect(context.isValidateFuncParamsAtRuntime()).andReturn(false);
		expect(invocation.invoke(spec, context, params)).andThrow(new NullPointerException("Fail"));
		c.replay();
		spec.invoke(context, params);
		c.verify();
	}
}
