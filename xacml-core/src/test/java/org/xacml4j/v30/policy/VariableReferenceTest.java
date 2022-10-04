package org.xacml4j.v30.policy;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.types.XacmlTypes;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;



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
		expect(expression.evaluate(context)).andReturn(XacmlTypes.BOOLEAN.of(true));
		context.setVariableEvaluationResult("testId", XacmlTypes.BOOLEAN.of(true));
		replay(context, expression);
		assertEquals(XacmlTypes.BOOLEAN.of(true), varRef.evaluate(context));
		verify(context, expression);
	}

	@Test
	public void testVariableEvaluationValueAvailableInContext() throws EvaluationException
	{
		expect(context.getVariableEvaluationResult("testId")).andReturn(XacmlTypes.BOOLEAN.of(false));
		replay(context, expression);
		assertEquals(XacmlTypes.BOOLEAN.of(false), varRef.evaluate(context));
		verify(context, expression);
	}
}
