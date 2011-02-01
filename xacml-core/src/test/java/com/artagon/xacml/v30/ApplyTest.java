package com.artagon.xacml.v30;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.types.BooleanType;
import com.artagon.xacml.v30.types.IntegerType;

public class ApplyTest 
{
	private FunctionSpec function;
	private EvaluationContext context;
	
	@Before
	public void init(){
		this.function = createStrictMock(FunctionSpec.class);
		this.context = createStrictMock(EvaluationContext.class);
	}
	
	@Test
	public void testApplyEvaluationWithValidFunctionAndValidParameters() throws XacmlException
	{
		Expression[] params = {IntegerType.INTEGER.create(10L), IntegerType.INTEGER.create(11L)};
		expect(function.validateParameters(params)).andReturn(true);
		replay(function);
		Apply apply = new Apply(function, params);
		verify(function);
		reset(function);
		expect(function.invoke(context, params)).andReturn(BooleanType.BOOLEAN.create(false));
		replay(function);
		ValueExpression v = apply.evaluate(context);
		assertNotNull(v);
		assertEquals(BooleanType.BOOLEAN.create(false), v);
		verify(function);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCreateApplyWithValidFunctionAndInvalidParameters() throws XacmlException
	{
		expect(function.validateParameters(IntegerType.INTEGER.create(10L))).andReturn(false);
		expect(function.getId()).andReturn("testId");
		replay(function);
		new Apply(function, IntegerType.INTEGER.create(10L));
		verify(function);
	}
	
	@Test(expected=EvaluationException.class)
	public void testApplyEvaluationFunctionThrowsRuntimeException() throws XacmlException
	{
		expect(function.validateParameters(IntegerType.INTEGER.create(10L))).andReturn(true);
		expect(function.invoke(context, IntegerType.INTEGER.create(10L)))
		.andThrow(new IllegalArgumentException());
		replay(function);
		Apply apply = new Apply(function, IntegerType.INTEGER.create(10L));
		apply.evaluate(context);
		verify(function);
	}
	
	@Test(expected=EvaluationException.class)
	public void testApplyEvaluationFunctionParamValidationFails() throws XacmlException
	{
		expect(function.validateParameters(IntegerType.INTEGER.create(10L))).andReturn(true);
		expect(function.invoke(context, IntegerType.INTEGER.create(10L)))
		.andThrow(new IllegalArgumentException());
		replay(function);
		Apply apply = new Apply(function, IntegerType.INTEGER.create(10L));
		apply.evaluate(context);
		verify(function);
	}
	
	@Test(expected=FunctionInvocationException.class)
	public void testApplyEvaluationFunctionThrowsFunctionInvocationException() throws XacmlException
	{
		expect(function.validateParameters(IntegerType.INTEGER.create(10L))).andReturn(true);
		expect(function.invoke(context, IntegerType.INTEGER.create(10L))).
		andThrow(new FunctionInvocationException(context, function, new IllegalArgumentException()));
		replay(function);
		Apply apply = new Apply(function, IntegerType.INTEGER.create(10L));
		apply.evaluate(context);
		verify(function);
	}
}
