package com.artagon.xacml.v3;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.policy.type.DataTypes;

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
		Expression[] params = {DataTypes.INTEGER.create(10L), DataTypes.INTEGER.create(11L)};
		function.validateParametersAndThrow(params);
		replay(function);
		Apply apply = new Apply(function, params);
		verify(function);
		reset(function);
		expect(function.invoke(context, params)).andReturn(DataTypes.BOOLEAN.create(Boolean.FALSE));
		replay(function);
		Value v = apply.evaluate(context);
		assertNotNull(v);
		assertEquals(DataTypes.BOOLEAN.create(Boolean.FALSE), v);
		verify(function);
	}
	
	@Test(expected=PolicySyntaxException.class)
	public void testCreateApplyWithValidFunctionAndInvalidParameters() throws XacmlException
	{
		function.validateParametersAndThrow(DataTypes.INTEGER.create(10L));
		expectLastCall().andThrow(new PolicySyntaxException("Bad"));
		replay(function);
		new Apply(function, DataTypes.INTEGER.create(10L));
		verify(function);
	}
	
	@Test(expected=EvaluationException.class)
	public void testApplyEvaluationFunctionThrowsRuntimeException() throws XacmlException
	{
		function.validateParametersAndThrow(DataTypes.INTEGER.create(10L));
		expect(function.invoke(context, DataTypes.INTEGER.create(10L)))
		.andThrow(new IllegalArgumentException());
		replay(function);
		Apply apply = new Apply(function, DataTypes.INTEGER.create(10L));
		apply.evaluate(context);
		verify(function);
	}
	
	@Test(expected=EvaluationException.class)
	public void testApplyEvaluationFunctionParamValidationFails() throws XacmlException
	{
		function.validateParametersAndThrow(DataTypes.INTEGER.create(10L));
		expect(function.invoke(context, DataTypes.INTEGER.create(10L)))
		.andThrow(new IllegalArgumentException());
		replay(function);
		Apply apply = new Apply(function, DataTypes.INTEGER.create(10L));
		apply.evaluate(context);
		verify(function);
	}
	
	@Test(expected=FunctionInvocationException.class)
	public void testApplyEvaluationFunctionThrowsFunctionInvocationException() throws XacmlException
	{
		function.validateParametersAndThrow(DataTypes.INTEGER.create(10L));
		expect(function.invoke(context, DataTypes.INTEGER.create(10L))).
		andThrow(new FunctionInvocationException(context, function, new IllegalArgumentException()));
		replay(function);
		Apply apply = new Apply(function, DataTypes.INTEGER.create(10L));
		apply.evaluate(context);
		verify(function);
	}
}
