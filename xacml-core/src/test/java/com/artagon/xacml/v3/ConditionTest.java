package com.artagon.xacml.v3;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.types.XacmlDataTypes;

public class ConditionTest 
{
	private Expression exp;
	private EvaluationContext context;
	
	@Before
	public void init(){
		this.exp = createStrictMock(Expression.class);
		this.context = createStrictMock(EvaluationContext.class);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testCreateWithExpWhichReturnsNonBooleanValue() 
		throws Exception
	{
		expect(exp.getEvaluatesTo()).andReturn(XacmlDataTypes.INTEGER.getDataType()).times(1, 2);
		replay(exp);
		new Condition(exp);
		verify(exp);
	}
	
	@Test
	public void testExpressionThrowsEvaluationException() throws Exception
	{
		expect(exp.getEvaluatesTo()).andReturn(XacmlDataTypes.BOOLEAN.getDataType()).times(2);
		expect(exp.evaluate(context)).andThrow(new FunctionInvocationException(context, 
				createStrictMock(FunctionSpec.class), new NullPointerException()));
		context.setEvaluationStatus(StatusCode.createProcessingError());
		replay(exp, context);
		Condition c = new Condition(exp);
		assertEquals(ConditionResult.INDETERMINATE, c.evaluate(context));
		verify(exp, context);
	}
	
	@Test
	public void testExpressionThrowsRuntimeException() throws Exception
	{
		expect(exp.getEvaluatesTo()).andReturn(XacmlDataTypes.BOOLEAN.getDataType()).times(2);
		expect(exp.evaluate(context)).andThrow(new IllegalArgumentException());
		context.setEvaluationStatus(StatusCode.createProcessingError());
		replay(exp, context);
		Condition c = new Condition(exp);
		assertEquals(ConditionResult.INDETERMINATE, c.evaluate(context));
		verify(exp, context);
	}
	
	@Test
	public void testExpressionEvaluatesToFalse() throws Exception
	{
		expect(exp.getEvaluatesTo()).andReturn(XacmlDataTypes.BOOLEAN.getDataType()).times(2);
		expect(exp.evaluate(context)).andReturn(XacmlDataTypes.BOOLEAN.create(false));
		replay(exp, context);
		Condition c = new Condition(exp);
		assertEquals(ConditionResult.FALSE, c.evaluate(context));
		verify(exp, context);
	}
	
	@Test
	public void testExpressionEvaluatesToTrue() throws Exception
	{
		expect(exp.getEvaluatesTo()).andReturn(XacmlDataTypes.BOOLEAN.getDataType()).times(2);
		expect(exp.evaluate(context)).andReturn(XacmlDataTypes.BOOLEAN.create(true));
		replay(exp, context);
		Condition c = new Condition(exp);
		assertEquals(ConditionResult.TRUE, c.evaluate(context));
		verify(exp, context);
	}
}
