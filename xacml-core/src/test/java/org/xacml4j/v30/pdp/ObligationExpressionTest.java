package org.xacml4j.v30.pdp;

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

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.AttributeAssignment;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.types.BooleanExp;
import org.xacml4j.v30.types.IntegerExp;


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
	public void testCreateObligationExpression() throws XacmlException
	{
		AttributeAssignmentExpression attrExp = c.createMock(AttributeAssignmentExpression.class);

		expect(attrExp.getAttributeId()).andReturn("attributeId");
		c.replay();
		ObligationExpression exp = ObligationExpression.builder("test",Effect.DENY).attribute(attrExp).build();

		assertTrue(exp.isApplicable(Decision.DENY));
		assertFalse(exp.isApplicable(Decision.PERMIT));
		assertFalse(exp.isApplicable(Decision.INDETERMINATE));
		assertEquals("test", exp.getId());
		c.verify();
	}

	@Test
	public void testEvaluateObligationExpression() throws XacmlException
	{
		AttributeAssignmentExpression attrExp0 = c.createMock(AttributeAssignmentExpression.class);
		AttributeAssignmentExpression attrExp1 = c.createMock(AttributeAssignmentExpression.class);
		expect(attrExp0.getAttributeId()).andReturn("attributeId0").times(2);
		expect(attrExp0.getCategory()).andReturn(Categories.SUBJECT_ACCESS);
		expect(attrExp0.getIssuer()).andReturn("issuer0");
		expect(attrExp0.evaluate(context)).andReturn(IntegerExp.valueOf(1));
		expect(attrExp1.getAttributeId()).andReturn("attributeId1").times(2);
		expect(attrExp1.getCategory()).andReturn(Categories.RESOURCE);
		expect(attrExp1.getIssuer()).andReturn("issuer1");
		expect(attrExp1.evaluate(context)).andReturn(BooleanExp.valueOf(false));
		c.replay();
		ObligationExpression exp = ObligationExpression.builder("test",Effect.DENY).attribute(attrExp0, attrExp1).build();

		Obligation obligation = exp.evaluate(context);
		Iterator<AttributeAssignment> it = obligation.getAttributes().iterator();
		AttributeAssignment a0 = it.next();
		assertEquals(Effect.DENY, obligation.getFulfillOn());
		assertEquals("issuer0", a0.getIssuer());
		assertEquals("attributeId0", a0.getAttributeId());
		assertEquals(Categories.SUBJECT_ACCESS, a0.getCategory());
		assertEquals(IntegerExp.valueOf(1), a0.getAttribute());
		AttributeAssignment a1 = it.next();
		assertEquals("issuer1", a1.getIssuer());
		assertEquals("attributeId1", a1.getAttributeId());
		assertEquals(Categories.RESOURCE, a1.getCategory());
		assertEquals(BooleanExp.valueOf(false), a1.getAttribute());
		c.verify();
	}

	@Test(expected=EvaluationException.class)
	public void testAttributeAssignmentThrowsEvaluationException() throws XacmlException
	{
		AttributeAssignmentExpression attrExp0 = c.createMock(AttributeAssignmentExpression.class);
		AttributeAssignmentExpression attrExp1 = c.createMock(AttributeAssignmentExpression.class);
		expect(attrExp0.getAttributeId()).andReturn("attributeId0").times(2);
		expect(attrExp0.getCategory()).andReturn(Categories.SUBJECT_ACCESS);
		expect(attrExp0.getIssuer()).andReturn("issuer0");
		expect(attrExp0.evaluate(context)).andReturn(IntegerExp.valueOf(1));
		expect(attrExp1.getAttributeId()).andReturn("attributeId1").times(2);
		expect(attrExp1.getCategory()).andReturn(Categories.RESOURCE);
		expect(attrExp1.getIssuer()).andReturn("issuer1");
		expect(attrExp1.evaluate(context)).andThrow(new EvaluationException(Status.processingError().build(), new NullPointerException()));
		c.replay();

		ObligationExpression exp = ObligationExpression.builder("test",Effect.DENY).attribute(attrExp0, attrExp1).build();

		exp.evaluate(context);
		c.verify();
	}
}
