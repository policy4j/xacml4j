package com.artagon.xacml.v3.policy;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

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
	public void testApplyWithValidFunctionAndValidParameters() throws EvaluationException
	{
		Expression[] params = {DataTypes.INTEGER.create(10L), DataTypes.INTEGER.create(11L)};
		expect(function.validateParameters(params)).andReturn(true);
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
	
	@Test(expected=IllegalArgumentException.class)
	public void testApplyWithValidFunctionAndInValidParameters() throws EvaluationException
	{
		expect(function.validateParameters(DataTypes.INTEGER.create(10L))).andReturn(false);
		replay(function);
		new DefaultApply(function, DataTypes.INTEGER.create(10L));
		verify(function);
	}
	
	@Test(expected=EvaluationException.class)
	public void testApplyFunctionThrowsRuntimeException() throws EvaluationException
	{
		expect(function.validateParameters(DataTypes.INTEGER.create(10L))).andReturn(true);
		expect(function.invoke(context, DataTypes.INTEGER.create(10L)))
		.andThrow(new IllegalArgumentException());
		replay(function);
		Apply apply = new DefaultApply(function, DataTypes.INTEGER.create(10L));
		apply.evaluate(context);
		verify(function);
	}
	
	@Test(expected=FunctionInvocationException.class)
	public void testApplyFunctionThrowsFunctionInvocationException() throws EvaluationException
	{
		expect(function.validateParameters(DataTypes.INTEGER.create(10L))).andReturn(true);
		expect(function.invoke(context, DataTypes.INTEGER.create(10L)))
		.andThrow(new FunctionInvocationException("Failed to invoke"));
		replay(function);
		Apply apply = new DefaultApply(function, DataTypes.INTEGER.create(10L));
		apply.evaluate(context);
		verify(function);
	}
}
