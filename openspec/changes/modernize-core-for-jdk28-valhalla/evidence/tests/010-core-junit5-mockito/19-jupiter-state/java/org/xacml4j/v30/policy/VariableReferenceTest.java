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

import static org.mockito.Mockito.*;

import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.types.XacmlTypes;


@ExtendWith(MockitoExtension.class)
public class VariableReferenceTest
{
	private VariableDefinition varDef;
	private VariableReference varRef;
	private Expression expression;
	private EvaluationContext context;

	@BeforeEach
	public void init()
	{
		this.expression = mock(Expression.class);
		this.context = mock(EvaluationContext.class);
		this.varDef = new VariableDefinition("testId", expression);
		this.varRef = new VariableReference(varDef);
	}

	@Test
	public void testVariableEvaluationValueNotAvailableInContext() throws EvaluationException
	{
		when(context.getVariableEvaluationResult("testId")).thenReturn(null);
		when(expression.evaluate(context)).thenReturn(XacmlTypes.BOOLEAN.ofAny(true));
		doNothing().when(context).setVariableEvaluationResult("testId", XacmlTypes.BOOLEAN.ofAny(true));
		InOrder order = inOrder(expression, context);
		assertEquals(XacmlTypes.BOOLEAN.ofAny(true), varRef.evaluate(context));
		order.verify(context).getVariableEvaluationResult("testId");
		order.verify(expression).evaluate(context);
		order.verify(context).setVariableEvaluationResult("testId", XacmlTypes.BOOLEAN.ofAny(true));
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(expression, context);
	}

	@Test
	public void testVariableEvaluationValueAvailableInContext() throws EvaluationException
	{
		when(context.getVariableEvaluationResult("testId")).thenReturn(XacmlTypes.BOOLEAN.ofAny(false));
		InOrder order = inOrder(expression, context);
		assertEquals(XacmlTypes.BOOLEAN.ofAny(false), varRef.evaluate(context));
		order.verify(context).getVariableEvaluationResult("testId");
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(expression, context);
	}
}
