package org.xacml4j.v30.pdp;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.xacml4j.v30.types.BooleanType.BOOLEAN;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;



public class VariableReferenceTest
{
	private VariableDefinition varDef;
	private VariableReference varRef;
	private Expression expression;
	private EvaluationContext context;
	
	@Before
	public void init()
	{
		this.expression = createStrictMock(Expression.class);
		this.context = createStrictMock(EvaluationContext.class);
		this.varDef = new VariableDefinition("testId", expression);
		this.varRef = new VariableReference(varDef);
	}
	
	@Test
	public void testVariableEvaluationValueNotAvailableInContext() throws EvaluationException
	{
		expect(context.getVariableEvaluationResult("testId")).andReturn(null);
		expect(expression.evaluate(context)).andReturn(BOOLEAN.create(true));
		context.setVariableEvaluationResult("testId", BOOLEAN.create(true));
		replay(context, expression);
		assertEquals(BOOLEAN.create(true), varRef.evaluate(context));
		verify(context, expression);
	}
	
	@Test
	public void testVariableEvaluationValueAvailableInContext() throws EvaluationException
	{
		expect(context.getVariableEvaluationResult("testId")).andReturn(BOOLEAN.create(false));
		replay(context, expression);
		assertEquals(BOOLEAN.create(false), varRef.evaluate(context));
		verify(context, expression);
	}
}
