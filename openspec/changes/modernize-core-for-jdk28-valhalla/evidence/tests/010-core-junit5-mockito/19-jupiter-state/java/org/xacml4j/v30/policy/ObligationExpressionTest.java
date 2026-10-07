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

import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xacml4j.v30.AttributeAssignment;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.CoreException;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.types.XacmlTypes;


@ExtendWith(MockitoExtension.class)
public class ObligationExpressionTest
{
	private EvaluationContext context;

	@BeforeEach
	public void init(){
		this.context = mock(EvaluationContext.class);
	}

	@Test
	public void testCreateObligationExpression() throws CoreException
	{
		AttributeAssignmentExpression attrExp = AttributeAssignmentExpression.builder("id").expression(XacmlTypes.STRING.ofAny("value")).build();

		assertEquals("id", attrExp.getAttributeId());
		ObligationExpression exp = ObligationExpression.builder("test",Effect.DENY).attribute(attrExp).build();

		assertTrue(exp.isApplicable(Decision.DENY));
		assertFalse(exp.isApplicable(Decision.PERMIT));
		assertFalse(exp.isApplicable(Decision.INDETERMINATE));
		assertEquals("test", exp.getId());
		verifyNoMoreInteractions(context);
	}

	@Test
	public void testEvaluateObligationExpression() throws CoreException
	{
		AttributeAssignmentExpression attrExp0 = AttributeAssignmentExpression
				.builder("attributeId0")
				.category(CategoryId.SUBJECT_ACCESS)
				.expression(XacmlTypes.INTEGER.ofAny(1))
				.issuer("issuer0")
				.build();
		AttributeAssignmentExpression attrExp1 = AttributeAssignmentExpression
				.builder("attributeId1")
				.category(CategoryId.RESOURCE)
				.expression(XacmlTypes.BOOLEAN.ofAny(false))
				.issuer("issuer1")
				.build();

		ObligationExpression exp = ObligationExpression.builder("test",Effect.DENY).attribute(attrExp0, attrExp1).build();

		Obligation obligation = exp.evaluate(context);
		Iterator<AttributeAssignment> it = obligation.getAttributes().iterator();
		AttributeAssignment a0 = it.next();
		assertEquals(Effect.DENY, obligation.getFulfillOn());
		assertEquals("issuer0", a0.getIssuer());
		assertEquals("attributeId0", a0.getAttributeId());
		assertEquals(CategoryId.SUBJECT_ACCESS, a0.getCategory().get());
		assertEquals(XacmlTypes.INTEGER.ofAny(1), a0.getAttribute());
		AttributeAssignment a1 = it.next();
		assertEquals("issuer1", a1.getIssuer());
		assertEquals("attributeId1", a1.getAttributeId());
		assertEquals(CategoryId.RESOURCE, a1.getCategory().get());
		assertEquals(XacmlTypes.BOOLEAN.ofAny(false), a1.getAttribute());
		verifyNoMoreInteractions(context);
	}

	@Test
	public void testAttributeAssignmentThrowsEvaluationException() throws CoreException
	{
		Expression expression = mock(Expression.class);
		AttributeAssignmentExpression attrExp0 = AttributeAssignmentExpression
				.builder("attributeId0")
				.category(CategoryId.SUBJECT_ACCESS)
				.expression(XacmlTypes.INTEGER.ofAny(1))
				.issuer("issuer0")
				.build();
		AttributeAssignmentExpression attrExp1 = AttributeAssignmentExpression
				.builder("attributeId1")
				.category(CategoryId.RESOURCE)
				.expression(expression)
				.issuer("issuer1")
				.build();

		when(expression.evaluate(context)).thenThrow(new EvaluationException(Status.processingError().build(), new NullPointerException()));
		InOrder order = inOrder(context, expression);

		ObligationExpression exp = ObligationExpression.builder("test",Effect.DENY).attribute(attrExp0, attrExp1).build();

		assertThrows(EvaluationException.class, () -> exp.evaluate(context));
		order.verify(expression).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, expression);
	}
}
