package com.artagon.xacml.v3.policy.impl;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.VariableDefinition;
import com.artagon.xacml.v3.policy.type.DataTypes;


public class VariableDefinitionTest
{
	private VariableDefinition varDef;
	private Expression expression;
	private EvaluationContext context;
	
	@Before
	public void init()
	{
		this.expression = createStrictMock(Expression.class);
		this.context = createStrictMock(EvaluationContext.class);
		this.varDef = new VariableDefinition("testId", expression);
	}
	
	@Test
	public void testVariableEvaluationValueNotAvailableInContext() throws EvaluationException
	{
		expect(context.getVariableEvaluationResult("testId")).andReturn(null);
		expect(expression.evaluate(context)).andReturn(DataTypes.BOOLEAN.create(true));
		context.setVariableEvaluationResult("testId", DataTypes.BOOLEAN.create(true));
		replay(context, expression);
		assertEquals(DataTypes.BOOLEAN.create(true), varDef.evaluate(context));
		verify(context, expression);
	}
	
	@Test
	public void testVariableEvaluationValueAvailableInContext() throws EvaluationException
	{
		expect(context.getVariableEvaluationResult("testId")).andReturn(DataTypes.BOOLEAN.create(false));
		replay(context, expression);
		assertEquals(DataTypes.BOOLEAN.create(false), varDef.evaluate(context));
		verify(context, expression);
	}
}
