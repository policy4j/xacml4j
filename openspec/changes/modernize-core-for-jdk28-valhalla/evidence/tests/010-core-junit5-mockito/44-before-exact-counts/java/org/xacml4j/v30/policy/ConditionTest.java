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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.types.Value;
import org.xacml4j.v30.types.XacmlTypes;


/**
 * @author Giedrius Trumpickas
 */
@ExtendWith(MockitoExtension.class)
public class ConditionTest
{
	private Expression exp;
	private EvaluationContext context;
	private Rule rule;

	@BeforeEach
	public void init(){
		this.exp = mock(Expression.class);
		this.context = mock(EvaluationContext.class);
		this.rule = Rule.builder("testRule", org.xacml4j.v30.Effect.DENY).build();
	}

	@Test
	public void testCreateWithExpWhichReturnsNonBooleanValue() {
		when(exp.getEvaluatesTo()).thenReturn(XacmlTypes.INTEGER);

		InOrder order = inOrder(exp, context);
		assertThrows(IllegalArgumentException.class, () -> new Condition(exp));
		order.verify(exp).getEvaluatesTo();
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(exp, context);
	}

	@Test
	public void testExpressionThrowsEvaluationException() {

		when(exp.getEvaluatesTo()).thenReturn(XacmlTypes.BOOLEAN);
		when(exp.evaluate(context)).thenThrow(new FunctionInvocationException(
				mock(FunctionSpec.class), new NullPointerException()));
		ArgumentCaptor<Status> statusCapture = ArgumentCaptor.forClass(Status.class);
		doNothing().when(context).setEvaluationStatus(statusCapture.capture());

		InOrder order = inOrder(exp, context);
		Condition c = Condition.condition(exp);
		assertEquals(ConditionResult.INDETERMINATE, c.evaluate(context));
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, statusCapture.getValue().getStatusCode().getValue());
		order.verify(exp).getEvaluatesTo();
		order.verify(exp).evaluate(context);
		order.verify(context).setEvaluationStatus(statusCapture.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(exp, context);
	}

	@Test
	public void testExpressionThrowsRuntimeException() {
		when(exp.getEvaluatesTo()).thenReturn(XacmlTypes.BOOLEAN);
		when(exp.evaluate(context)).thenThrow(new IllegalArgumentException());

		ArgumentCaptor<Status> statusCapture = ArgumentCaptor.forClass(Status.class);
		ArgumentCaptor<Rule> ruleCapture = ArgumentCaptor.forClass(Rule.class);
		when(context.getCurrentRule()).thenReturn(rule);
		doNothing().when(context).setEvaluationStatus(ruleCapture.capture(), statusCapture.capture());

		InOrder order = inOrder(exp, context);
		Condition c = new Condition(exp);
		assertEquals(ConditionResult.INDETERMINATE, c.evaluate(context));
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, statusCapture.getValue().getStatusCode().getValue());
		order.verify(exp).getEvaluatesTo();
		order.verify(exp).evaluate(context);
		order.verify(context).getCurrentRule();
		order.verify(context).setEvaluationStatus(ruleCapture.capture(), statusCapture.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(exp, context);
		assertSame(rule, ruleCapture.getValue());
	}

	@Test
	public void testExpressionEvaluatesToFalse() {
		when(exp.getEvaluatesTo()).thenReturn(XacmlTypes.BOOLEAN);
		when(exp.evaluate(context)).thenReturn(XacmlTypes.BOOLEAN.ofAny(false));

		InOrder order = inOrder(exp, context);
		Condition c = new Condition(exp);
		assertEquals(ConditionResult.FALSE, c.evaluate(context));
		order.verify(exp).getEvaluatesTo();
		order.verify(exp).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(exp, context);
	}

	@Test
	public void testExpressionEvaluatesToTrue() {
		when(exp.getEvaluatesTo()).thenReturn(XacmlTypes.BOOLEAN);
		when(exp.evaluate(context)).thenReturn(XacmlTypes.BOOLEAN.ofAny(true));

		InOrder order = inOrder(exp, context);
		Condition c = new Condition(exp);
		assertEquals(ConditionResult.TRUE, c.evaluate(context));
		order.verify(exp).getEvaluatesTo();
		order.verify(exp).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(exp, context);
	}

	@Test
	public void testObjectMethods() {
		Value exp1 = org.xacml4j.v30.types.BooleanVal.of(true);
		Value exp2 = org.xacml4j.v30.types.BooleanVal.of(false);

		assertEquals(XacmlTypes.BOOLEAN, exp1.getEvaluatesTo());
		assertEquals(XacmlTypes.BOOLEAN, exp2.getEvaluatesTo());


		Condition c1 = new Condition(exp1);
		Condition c2 = new Condition(exp1);
		Condition c3 = new Condition(exp2);

		assertTrue(c1.equals(c2));
		assertFalse(c1.equals(c3));
		assertFalse(c1.equals(exp1));
		assertFalse(c1.equals(null));

		assertEquals(c1.hashCode(), c2.hashCode());
		assertEquals(c1.toString(), c2.toString());
		verifyNoMoreInteractions(exp, context);
	}
}
