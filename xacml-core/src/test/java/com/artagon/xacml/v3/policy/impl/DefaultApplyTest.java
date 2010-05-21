package com.artagon.xacml.v3.policy.impl;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.easymock.EasyMock.expectLastCall;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.Apply;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.FunctionInvocationException;
import com.artagon.xacml.v3.FunctionSpec;
import com.artagon.xacml.v3.PolicySyntaxException;
import com.artagon.xacml.v3.Value;
import com.artagon.xacml.v3.XacmlException;
import com.artagon.xacml.v3.policy.type.DataTypes;

public class DefaultApplyTest 
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
		function.validateParameters(params);
		replay(function);
		Apply apply = new DefaultApply(function, params);
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
		function.validateParameters(DataTypes.INTEGER.create(10L));
		expectLastCall().andThrow(new PolicySyntaxException("Bad"));
		replay(function);
		new DefaultApply(function, DataTypes.INTEGER.create(10L));
		verify(function);
	}
	
	@Test(expected=EvaluationException.class)
	public void testApplyEvaluationFunctionThrowsRuntimeException() throws XacmlException
	{
		function.validateParameters(DataTypes.INTEGER.create(10L));
		expect(function.invoke(context, DataTypes.INTEGER.create(10L)))
		.andThrow(new IllegalArgumentException());
		replay(function);
		Apply apply = new DefaultApply(function, DataTypes.INTEGER.create(10L));
		apply.evaluate(context);
		verify(function);
	}
	
	@Test(expected=EvaluationException.class)
	public void testApplyEvaluationFunctionParamValidationFails() throws XacmlException
	{
		function.validateParameters(DataTypes.INTEGER.create(10L));
		expect(function.invoke(context, DataTypes.INTEGER.create(10L)))
		.andThrow(new IllegalArgumentException());
		replay(function);
		Apply apply = new DefaultApply(function, DataTypes.INTEGER.create(10L));
		apply.evaluate(context);
		verify(function);
	}
	
	@Test(expected=FunctionInvocationException.class)
	public void testApplyEvaluationFunctionThrowsFunctionInvocationException() throws XacmlException
	{
		function.validateParameters(DataTypes.INTEGER.create(10L));
		expect(function.invoke(context, DataTypes.INTEGER.create(10L))).andReturn(DataTypes.BOOLEAN.create(Boolean.FALSE));
		replay(function);
		Apply apply = new DefaultApply(function, DataTypes.INTEGER.create(10L));
		apply.evaluate(context);
		verify(function);
	}
}
