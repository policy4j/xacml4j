package com.artagon.xacml.v3.policy;

import static com.artagon.xacml.v3.types.BooleanType.BOOLEAN;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.policy.VariableDefinition;


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
		expect(expression.evaluate(context)).andReturn(BOOLEAN.create(true));
		context.setVariableEvaluationResult("testId", BOOLEAN.create(true));
		replay(context, expression);
		assertEquals(BOOLEAN.create(true), varDef.evaluate(context));
		verify(context, expression);
	}
	
	@Test
	public void testVariableEvaluationValueAvailableInContext() throws EvaluationException
	{
		expect(context.getVariableEvaluationResult("testId")).andReturn(BOOLEAN.create(false));
		replay(context, expression);
		assertEquals(BOOLEAN.create(false), varDef.evaluate(context));
		verify(context, expression);
	}
}
