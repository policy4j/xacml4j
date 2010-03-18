package com.artagon.xacml.v3.policy.impl;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.policy.Condition;
import com.artagon.xacml.v3.policy.ConditionResult;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.FunctionInvocationException;
import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.policy.PolicyException;
import com.artagon.xacml.v3.policy.PolicySyntaxException;
import com.artagon.xacml.v3.policy.type.DataTypes;

public class DefaultConditionTest 
{
	private Expression exp;
	private EvaluationContext context;
	
	@Before
	public void init(){
		this.exp = createStrictMock(Expression.class);
		this.context = createStrictMock(EvaluationContext.class);
	}
	
	@Test(expected=PolicySyntaxException.class)
	public void testCreateWithExpWhichReturnsNonBooleanValue() 
		throws PolicyException
	{
		expect(exp.getEvaluatesTo()).andReturn(DataTypes.INTEGER.getType()).times(1, 2);
		replay(exp);
		new DefaultCondition(exp);
		verify(exp);
	}
	
	@Test
	public void testExpressionThrowsEvaluationException() throws PolicyException
	{
		expect(exp.getEvaluatesTo()).andReturn(DataTypes.BOOLEAN.getType());
		expect(exp.evaluate(context)).andThrow(new FunctionInvocationException(context, 
				createStrictMock(FunctionSpec.class), new NullPointerException()));
		replay(exp, context);
		Condition c = new DefaultCondition(exp);
		assertEquals(ConditionResult.INDETERMINATE, c.evaluate(context));
		verify(exp, context);
	}
	
	@Test
	public void testExpressionThrowsRuntimeException() throws PolicyException
	{
		expect(exp.getEvaluatesTo()).andReturn(DataTypes.BOOLEAN.getType());
		expect(exp.evaluate(context)).andThrow(new IllegalArgumentException());
		replay(exp, context);
		Condition c = new DefaultCondition(exp);
		assertEquals(ConditionResult.INDETERMINATE, c.evaluate(context));
		verify(exp, context);
	}
	
	@Test
	public void testExpressionEvaluatesToFalse() throws PolicyException
	{
		expect(exp.getEvaluatesTo()).andReturn(DataTypes.BOOLEAN.getType());
		expect(exp.evaluate(context)).andReturn(DataTypes.BOOLEAN.create(false));
		replay(exp, context);
		Condition c = new DefaultCondition(exp);
		assertEquals(ConditionResult.FALSE, c.evaluate(context));
		verify(exp, context);
	}
	
	@Test
	public void testExpressionEvaluatesToTrue() throws PolicyException
	{
		expect(exp.getEvaluatesTo()).andReturn(DataTypes.BOOLEAN.getType());
		expect(exp.evaluate(context)).andReturn(DataTypes.BOOLEAN.create(true));
		replay(exp, context);
		Condition c = new DefaultCondition(exp);
		assertEquals(ConditionResult.TRUE, c.evaluate(context));
		verify(exp, context);
	}
}
