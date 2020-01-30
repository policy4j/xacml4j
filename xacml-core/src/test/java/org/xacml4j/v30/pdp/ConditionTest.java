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

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.easymock.Capture;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.*;
import org.xacml4j.v30.types.XacmlTypes;


/**
 * @author Giedrius Trumpickas
 */
public class ConditionTest
{
	private Expression exp;
	private EvaluationContext context;
	private IMocksControl ctl;

	@Before
	public void init(){
		ctl = createStrictControl();
		this.exp = ctl.createMock(Expression.class);
		this.context = ctl.createMock(EvaluationContext.class);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCreateWithExpWhichReturnsNonBooleanValue() {
		expect(exp.getEvaluatesTo()).andReturn(XacmlTypes.INTEGER);

		ctl.replay();
		new Condition(exp);
		ctl.verify();
	}

	@Test
	public void testExpressionThrowsEvaluationException() {

		expect(exp.getEvaluatesTo()).andReturn(XacmlTypes.BOOLEAN);
		expect(exp.evaluate(context)).andThrow(new FunctionInvocationException(
				ctl.createMock(FunctionSpec.class), new NullPointerException()));
		Capture<Status> statusCapture = new Capture<>();
		context.setEvaluationStatus(capture(statusCapture));

		ctl.replay();
		Condition c = new Condition(exp);
		assertEquals(ConditionResult.INDETERMINATE, c.evaluate(context));
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, statusCapture.getValue().getStatusCode().getValue());
		ctl.verify();
	}

	@Test
	public void testExpressionThrowsRuntimeException() {
		expect(exp.getEvaluatesTo()).andReturn(XacmlTypes.BOOLEAN);
		expect(exp.evaluate(context)).andThrow(new IllegalArgumentException());
		Capture<Status> statusCapture = new Capture<>();
		context.setEvaluationStatus(capture(statusCapture));

		ctl.replay();
		Condition c = new Condition(exp);
		assertEquals(ConditionResult.INDETERMINATE, c.evaluate(context));
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, statusCapture.getValue().getStatusCode().getValue());
		ctl.verify();
	}

	@Test
	public void testExpressionEvaluatesToFalse() {
		expect(exp.getEvaluatesTo()).andReturn(XacmlTypes.BOOLEAN);
		expect(exp.evaluate(context)).andReturn(XacmlTypes.BOOLEAN.of(false));

		ctl.replay();
		Condition c = new Condition(exp);
		assertEquals(ConditionResult.FALSE, c.evaluate(context));
		ctl.verify();
	}

	@Test
	public void testExpressionEvaluatesToTrue() {
		expect(exp.getEvaluatesTo()).andReturn(XacmlTypes.BOOLEAN);
		expect(exp.evaluate(context)).andReturn(XacmlTypes.BOOLEAN.of(true));

		ctl.replay();
		Condition c = new Condition(exp);
		assertEquals(ConditionResult.TRUE, c.evaluate(context));
		ctl.verify();
	}

	@Test
	public void testObjectMethods() {
		AttributeValue exp1 = ctl.createMock(AttributeValue.class);
		AttributeValue exp2 = ctl.createMock(AttributeValue.class);

		expect(exp1.getEvaluatesTo()).andReturn(XacmlTypes.BOOLEAN).times(2);
		expect(exp2.getEvaluatesTo()).andReturn(XacmlTypes.BOOLEAN).times(1);

		ctl.replay();

		Condition c1 = new Condition(exp1);
		Condition c2 = new Condition(exp1);
		Condition c3 = new Condition(exp2);

		assertTrue(c1.equals(c2));
		assertFalse(c1.equals(c3));
		assertFalse(c1.equals(exp1));
		assertFalse(c1.equals(null));

		assertEquals(c1.hashCode(), c2.hashCode());
		assertEquals(c1.toString(), c2.toString());
		ctl.verify();
	}
}
