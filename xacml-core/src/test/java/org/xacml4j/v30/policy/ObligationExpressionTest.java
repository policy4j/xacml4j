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

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.*;
import org.xacml4j.v30.types.XacmlTypes;

import java.util.Iterator;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.*;


public class ObligationExpressionTest
{
	private IMocksControl c;
	private EvaluationContext context;

	@Before
	public void init(){
		this.c = createControl();
		this.context = c.createMock(EvaluationContext.class);
	}

	@Test
	public void testCreateObligationExpression() throws CoreException
	{
		AttributeAssignmentExpression attrExp = c.createMock(AttributeAssignmentExpression.class);

		expect(attrExp.getAttributeId()).andReturn("id");
		c.replay();
		ObligationExpression exp = ObligationExpression.builder("test",Effect.DENY).attribute(attrExp).build();

		assertTrue(exp.isApplicable(Decision.DENY));
		assertFalse(exp.isApplicable(Decision.PERMIT));
		assertFalse(exp.isApplicable(Decision.INDETERMINATE));
		assertEquals("test", exp.getId());
		c.verify();
	}

	@Test
	public void testEvaluateObligationExpression() throws CoreException
	{
		AttributeAssignmentExpression attrExp0 = AttributeAssignmentExpression
				.builder("attributeId0")
				.category(CategoryId.SUBJECT_ACCESS)
				.expression(XacmlTypes.INTEGER.of(1))
				.issuer("issuer0")
				.build();
		AttributeAssignmentExpression attrExp1 = AttributeAssignmentExpression
				.builder("attributeId1")
				.category(CategoryId.RESOURCE)
				.expression(XacmlTypes.BOOLEAN.of(false))
				.issuer("issuer1")
				.build();

		c.replay();
		ObligationExpression exp = ObligationExpression.builder("test",Effect.DENY).attribute(attrExp0, attrExp1).build();

		Obligation obligation = exp.evaluate(context);
		Iterator<AttributeAssignment> it = obligation.getAttributes().iterator();
		AttributeAssignment a0 = it.next();
		assertEquals(Effect.DENY, obligation.getFulfillOn());
		assertEquals("issuer0", a0.getIssuer());
		assertEquals("attributeId0", a0.getAttributeId());
		assertEquals(CategoryId.SUBJECT_ACCESS, a0.getCategory());
		assertEquals(XacmlTypes.INTEGER.of(1), a0.getAttribute());
		AttributeAssignment a1 = it.next();
		assertEquals("issuer1", a1.getIssuer());
		assertEquals("attributeId1", a1.getAttributeId());
		assertEquals(CategoryId.RESOURCE, a1.getCategory());
		assertEquals(XacmlTypes.BOOLEAN.of(false), a1.getAttribute());
		c.verify();
	}

	@Test(expected=EvaluationException.class)
	public void testAttributeAssignmentThrowsEvaluationException() throws CoreException
	{
		Expression expression = c.createMock(Expression.class);
		AttributeAssignmentExpression attrExp0 = AttributeAssignmentExpression
				.builder("attributeId0")
				.category(CategoryId.SUBJECT_ACCESS)
				.expression(XacmlTypes.INTEGER.of(1))
				.issuer("issuer0")
				.build();
		AttributeAssignmentExpression attrExp1 = AttributeAssignmentExpression
				.builder("attributeId1")
				.category(CategoryId.RESOURCE)
				.expression(expression)
				.issuer("issuer1")
				.build();

		expect(expression.evaluate(context)).andThrow(new EvaluationException(Status.processingError().build(), new NullPointerException()));
		c.replay();

		ObligationExpression exp = ObligationExpression.builder("test",Effect.DENY).attribute(attrExp0, attrExp1).build();

		exp.evaluate(context);
		c.verify();
	}
}
