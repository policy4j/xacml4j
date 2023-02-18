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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;


public class AdviceExpressionTest
{
	private EvaluationContext context;
	private AttributeAssignmentExpression attrExp0;
	private AttributeAssignmentExpression attrExp1;
	private IMocksControl c;

	private Expression expression;

	@Before
	public void init(){
		this.c = createControl();
		this.context = createStrictMock(EvaluationContext.class);
		this.context = c.createMock(EvaluationContext.class);

		attrExp0 = AttributeAssignmentExpression
				.builder("attributeId0")
						.category(CategoryId.SUBJECT_ACCESS)
								.issuer("issuer0")
						.expression(XacmlTypes.INTEGER.of(1))
						.build();
		this.expression = c.createMock(Expression.class);
		attrExp1 = AttributeAssignmentExpression
				.builder("attributeId1")
				.category(CategoryId.RESOURCE)
				.issuer("issuer1")
				.expression(expression)
				.build();
	}

	@Test
	public void testCreateAdviceExpression() throws CoreException
	{
		c.replay();
		AdviceExpression exp = AdviceExpression.builder("test",Effect.DENY).attribute(attrExp0).build();
		assertTrue(exp.isApplicable(Decision.DENY));
		assertFalse(exp.isApplicable(Decision.PERMIT));
		assertFalse(exp.isApplicable(Decision.INDETERMINATE));
		assertEquals("test", exp.getId());
		c.verify();
	}

	@Test
	public void testEvaluateAdviceExpression() throws CoreException
	{
		expect(expression.evaluate(context)).andReturn(XacmlTypes.BOOLEAN.of(false));
		c.replay();

		AdviceExpression exp = AdviceExpression.builder("test",Effect.DENY).attribute(attrExp0, attrExp1).build();
		Advice advice = exp.evaluate(context);
		Iterator<AttributeAssignment> it = advice.getAttributes().iterator();
		AttributeAssignment a0 = it.next();
		assertEquals("issuer0", a0.getIssuer());
		assertEquals("attributeId0", a0.getAttributeId());
		assertEquals(Effect.DENY, advice.getFulfillOn());
		assertEquals(CategoryId.SUBJECT_ACCESS, a0.getCategory());
		assertEquals(XacmlTypes.INTEGER.of(1), a0.getAttribute());
		AttributeAssignment a1 = it.next();
		assertEquals("issuer1", a1.getIssuer());
		assertEquals("attributeId1", a1.getAttributeId());
		assertEquals(CategoryId.RESOURCE, a1.getCategory());
		assertEquals(XacmlTypes.BOOLEAN.of(false), a1.getAttribute());

		c.verify();
	}
}
