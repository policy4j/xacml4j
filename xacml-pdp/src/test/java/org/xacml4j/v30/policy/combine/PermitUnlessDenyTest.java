package org.xacml4j.v30.policy.combine;

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

import static org.easymock.EasyMock.createStrictControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.policy.DecisionCombiningAlgorithm;


public class PermitUnlessDenyTest {

	private IMocksControl mockCtl;

	private List<DecisionRule> decisions;
	private DecisionCombiningAlgorithm<DecisionRule> algorithm;
	private EvaluationContext context;

	@Before
	public void init(){
		this.decisions = new LinkedList<DecisionRule>();
		this.algorithm = new PermitUnlessDeny<DecisionRule>("aaaa");

		mockCtl = createStrictControl();
		this.context = mockCtl.createMock(EvaluationContext.class);
	}

	@Test
	public void testCombineWithNoDecisions()
	{
		mockCtl.replay();
		assertEquals(Decision.PERMIT, algorithm.combine(context, decisions));
		mockCtl.verify();
	}

	@Test
	public void testCombineWithDeny() throws EvaluationException
	{
		DecisionRule r1 = mockCtl.createMock(DecisionRule.class);
		decisions.add(r1);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluate(context)).andReturn(Decision.DENY);
		mockCtl.replay();
		assertEquals(Decision.DENY, algorithm.combine(context, decisions));
		mockCtl.verify();
	}

	@Test
	public void testCombineWithNotApplicable() throws EvaluationException
	{
		DecisionRule r1 = mockCtl.createMock(DecisionRule.class);
		decisions.add(r1);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluate(context)).andReturn(Decision.NOT_APPLICABLE);
		mockCtl.replay();
		assertEquals(Decision.PERMIT, algorithm.combine(context, decisions));
		mockCtl.verify();
	}

	@Test
	public void testCombineWithIndeterminate() throws EvaluationException
	{
		DecisionRule r1 = mockCtl.createMock(DecisionRule.class);
		decisions.add(r1);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluate(context)).andReturn(Decision.INDETERMINATE);
		mockCtl.replay();
		assertEquals(Decision.PERMIT, algorithm.combine(context, decisions));
		mockCtl.verify();
	}


	@Test
	public void testCombineWithDenyAndPermit() throws EvaluationException
	{
		DecisionRule r1 = mockCtl.createMock(DecisionRule.class);
		DecisionRule r2 = mockCtl.createMock(DecisionRule.class);
		decisions.add(r1);
		decisions.add(r2);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluate(context)).andReturn(Decision.PERMIT);
		expect(r2.createContext(context)).andReturn(context);
		expect(r2.evaluate(context)).andReturn(Decision.DENY);
		mockCtl.replay();
		assertEquals(Decision.DENY, algorithm.combine(context, decisions));
		mockCtl.verify();
	}
}
