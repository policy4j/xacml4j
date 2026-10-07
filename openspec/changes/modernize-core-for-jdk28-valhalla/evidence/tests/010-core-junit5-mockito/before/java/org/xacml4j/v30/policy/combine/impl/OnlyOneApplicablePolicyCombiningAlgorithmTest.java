package org.xacml4j.v30.policy.combine.impl;

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

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.MatchResult;


public class OnlyOneApplicablePolicyCombiningAlgorithmTest
{
	private OnlyOneApplicablePolicyCombiningAlgorithm c;
	private List<CompositeDecisionRule> d;
	private EvaluationContext context;

	@Before
	public void init(){
		this.c = new OnlyOneApplicablePolicyCombiningAlgorithm();
		this.d = new LinkedList<CompositeDecisionRule>();
		this.context = createStrictMock(EvaluationContext.class);
	}

	@Test
	public void testDecisionIsNoMatchContinueEvaluation() throws EvaluationException
	{
		CompositeDecisionRule d1 = createStrictMock(CompositeDecisionRule.class);
		CompositeDecisionRule d2 = createStrictMock(CompositeDecisionRule.class);
		EvaluationContext c1 = createStrictMock(EvaluationContext.class);
		EvaluationContext c2 = createStrictMock(EvaluationContext.class);
		d.add(d1);
		d.add(d2);
		expect(d1.createContext(context)).andReturn(c1);
		expect(d1.isMatch(c1)).andReturn(MatchResult.NOMATCH);
		expect(d2.createContext(context)).andReturn(c2);
		expect(d2.isMatch(c2)).andReturn(MatchResult.NOMATCH);
		replay(d1, d2, context, c1, c2);
		assertEquals(Decision.NOT_APPLICABLE, c.combine(context, d));
		verify(d1, d2, context, c1, c2);
	}

	@Test
	public void testDecisionIndeterminateStopsEvaluation() throws EvaluationException
	{
		CompositeDecisionRule d1 = createStrictMock(CompositeDecisionRule.class);
		CompositeDecisionRule d2 = createStrictMock(CompositeDecisionRule.class);
		EvaluationContext c1 = createStrictMock(EvaluationContext.class);
		EvaluationContext c2 = createStrictMock(EvaluationContext.class);
		d.add(d1);
		d.add(d2);
		expect(d1.createContext(context)).andReturn(c1);
		expect(d1.isMatch(c1)).andReturn(MatchResult.INDETERMINATE);
		replay(d1, d2, context, c1, c2);
		assertEquals(Decision.INDETERMINATE, c.combine(context, d));
		verify(d1, d2, context, c1, c2);
	}

	@Test
	public void testMoreThanOneIsApplicable() throws EvaluationException
	{
		CompositeDecisionRule d1 = createStrictMock(CompositeDecisionRule.class);
		CompositeDecisionRule d2 = createStrictMock(CompositeDecisionRule.class);
		EvaluationContext c1 = createStrictMock(EvaluationContext.class);
		EvaluationContext c2 = createStrictMock(EvaluationContext.class);
		d.add(d1);
		d.add(d2);
		expect(d1.createContext(context)).andReturn(c1);
		expect(d1.isMatch(c1)).andReturn(MatchResult.MATCH);
		expect(d2.createContext(context)).andReturn(c2);
		expect(d2.isMatch(c2)).andReturn(MatchResult.MATCH);
		replay(d1, d2, context, c1, c2);
		assertEquals(Decision.INDETERMINATE, c.combine(context, d));
		verify(d1, d2, context, c1, c2);
	}

	@Test
	public void testOnlyOneIsApplicableAndDecisionIsPermit() throws EvaluationException
	{
		CompositeDecisionRule d1 = createStrictMock(CompositeDecisionRule.class);
		CompositeDecisionRule d2 = createStrictMock(CompositeDecisionRule.class);
		EvaluationContext c1 = createStrictMock(EvaluationContext.class);
		EvaluationContext c2 = createStrictMock(EvaluationContext.class);
		d.add(d1);
		d.add(d2);
		expect(d1.createContext(context)).andReturn(c1);
		expect(d1.isMatch(c1)).andReturn(MatchResult.MATCH);
		expect(d2.createContext(context)).andReturn(c2);
		expect(d2.isMatch(c2)).andReturn(MatchResult.NOMATCH);
		expect(d1.evaluate(c1)).andReturn(Decision.PERMIT);
		replay(d1, d2, context, c1, c2);
		assertEquals(Decision.PERMIT, c.combine(context, d));
		verify(d1, d2, context, c2, c2);
	}

	@Test
	public void testOnlyOneIsApplicableAndDecisionIsDeny() throws EvaluationException
	{
		CompositeDecisionRule d1 = createStrictMock(CompositeDecisionRule.class);
		CompositeDecisionRule d2 = createStrictMock(CompositeDecisionRule.class);
		EvaluationContext c1 = createStrictMock(EvaluationContext.class);
		EvaluationContext c2 = createStrictMock(EvaluationContext.class);
		d.add(d1);
		d.add(d2);
		expect(d1.createContext(context)).andReturn(c1);
		expect(d1.isMatch(c1)).andReturn(MatchResult.MATCH);
		expect(d2.createContext(context)).andReturn(c2);
		expect(d2.isMatch(c2)).andReturn(MatchResult.NOMATCH);
		expect(d1.evaluate(c1)).andReturn(Decision.DENY);
		replay(d1, d2, context, c1, c2);
		assertEquals(Decision.DENY, c.combine(context, d));
		verify(d1, d2, context, c1, c2);
	}

	@Test
	public void testOnlyOneIsApplicableAndDecisionIsIndeterminate() throws EvaluationException
	{
		CompositeDecisionRule d1 = createStrictMock(CompositeDecisionRule.class);
		CompositeDecisionRule d2 = createStrictMock(CompositeDecisionRule.class);
		EvaluationContext c1 = createStrictMock(EvaluationContext.class);
		EvaluationContext c2 = createStrictMock(EvaluationContext.class);
		d.add(d1);
		d.add(d2);
		expect(d1.createContext(context)).andReturn(c1);
		expect(d1.isMatch(c1)).andReturn(MatchResult.MATCH);
		expect(d2.createContext(context)).andReturn(c2);
		expect(d2.isMatch(c2)).andReturn(MatchResult.NOMATCH);
		expect(d1.evaluate(c1)).andReturn(Decision.INDETERMINATE);
		replay(d1, d2, context, c1, c2);
		assertEquals(Decision.INDETERMINATE, c.combine(context, d));
		verify(d1, d2, context, c1, c2);
	}
}
