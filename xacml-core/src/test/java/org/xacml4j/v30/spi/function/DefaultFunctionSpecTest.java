package org.xacml4j.v30.spi.function;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

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
}
