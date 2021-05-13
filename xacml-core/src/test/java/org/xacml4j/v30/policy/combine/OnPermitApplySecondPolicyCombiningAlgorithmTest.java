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
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.LinkedList;
import java.util.List;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.pdp.DecisionCombiningAlgorithm;


public class OnPermitApplySecondPolicyCombiningAlgorithmTest {

	private List<CompositeDecisionRule> decisions;
	private DecisionCombiningAlgorithm<CompositeDecisionRule> algorithm;
	private EvaluationContext context;
	private IMocksControl c;

	@Before
	public void init(){
		this.c = createStrictControl();
		this.decisions = new LinkedList<CompositeDecisionRule>();
		this.algorithm = new OnPermitApplySecondPolicyCombiningAlgorithm();
		this.context = c.createMock(EvaluationContext.class);
	}

	/* If there are not exactly two or three children, then the result is "Indeterminate{DP}". */
	@Test
	public void testCombineWithNoDecisions() {
		context.setEvaluationStatus(Status.processingError().build());
		c.replay();
		Decision decision = algorithm.combine(context, decisions);
		c.verify();
		assertThat(decision, is(Decision.INDETERMINATE_DP));
	}

	@Test
	public void testCombineWithTooManyDecisions() {
		decisions = new LinkedList<CompositeDecisionRule>();
		decisions.add(c.createMock(CompositeDecisionRule.class));
		decisions.add(c.createMock(CompositeDecisionRule.class));
		decisions.add(c.createMock(CompositeDecisionRule.class));
		decisions.add(c.createMock(CompositeDecisionRule.class));

		context.setEvaluationStatus(Status.processingError().build());

		c.replay();
		Decision decision = algorithm.combine(context, decisions);
		c.verify();
		assertThat(decision, is(Decision.INDETERMINATE_DP));
	}

	@Test
	public void testCombineFirstPermitSecondNotApplicable() {
		CompositeDecisionRule r1 = c.createMock(CompositeDecisionRule.class);
		CompositeDecisionRule r2 = c.createMock(CompositeDecisionRule.class);

		decisions = new LinkedList<CompositeDecisionRule>();
		decisions.add(r1);
		decisions.add(r2);

		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluate(context)).andReturn(Decision.PERMIT);
		expect(r2.createContext(context)).andReturn(context);
		expect(r2.evaluate(context)).andReturn(Decision.NOT_APPLICABLE);

		c.replay();
		Decision decision = algorithm.combine(context, decisions);
		c.verify();
		assertThat(decision, is(Decision.NOT_APPLICABLE));
	}

	@Test
	public void testCombineFirstPermitSecondPermit() {
		CompositeDecisionRule r1 = c.createMock(CompositeDecisionRule.class);
		CompositeDecisionRule r2 = c.createMock(CompositeDecisionRule.class);

		decisions = new LinkedList<CompositeDecisionRule>();
		decisions.add(r1);
		decisions.add(r2);

		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluate(context)).andReturn(Decision.PERMIT);
		expect(r2.createContext(context)).andReturn(context);
		expect(r2.evaluate(context)).andReturn(Decision.PERMIT);

		c.replay();
		Decision decision = algorithm.combine(context, decisions);
		c.verify();
		assertThat(decision, is(Decision.PERMIT));
	}

	@Test
	public void testCombineFirstPermitSecondDeny() {
		CompositeDecisionRule r1 = c.createMock(CompositeDecisionRule.class);
		CompositeDecisionRule r2 = c.createMock(CompositeDecisionRule.class);

		decisions = new LinkedList<CompositeDecisionRule>();
		decisions.add(r1);
		decisions.add(r2);

		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluate(context)).andReturn(Decision.PERMIT);
		expect(r2.createContext(context)).andReturn(context);
		expect(r2.evaluate(context)).andReturn(Decision.DENY);

		c.replay();
		Decision decision = algorithm.combine(context, decisions);
		c.verify();
		assertThat(decision, is(Decision.DENY));
	}

	/* if the decision from the first child is "NotApplicable", "Deny", or "Indeterminate{D}", then the result is "NotApplicable" if there is no third child, or the decision of the third child if there is a third child. */
	@Test
	public void testCombineTwoPoliciesFirstDeny() {
		CompositeDecisionRule r1 = c.createMock(CompositeDecisionRule.class);
		CompositeDecisionRule r2 = c.createMock(CompositeDecisionRule.class);

		decisions = new LinkedList<CompositeDecisionRule>();
		decisions.add(r1);
		decisions.add(r2);

		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluate(context)).andReturn(Decision.DENY);

		c.replay();
		Decision decision = algorithm.combine(context, decisions);
		c.verify();
		assertThat(decision, is(Decision.NOT_APPLICABLE));
	}

	@Test
	public void testCombineTwoPoliciesFirstNotApplicable() {
		CompositeDecisionRule r1 = c.createMock(CompositeDecisionRule.class);
		CompositeDecisionRule r2 = c.createMock(CompositeDecisionRule.class);

		decisions = new LinkedList<CompositeDecisionRule>();
		decisions.add(r1);
		decisions.add(r2);

		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluate(context)).andReturn(Decision.NOT_APPLICABLE);

		c.replay();
		Decision decision = algorithm.combine(context, decisions);
		c.verify();
		assertThat(decision, is(Decision.NOT_APPLICABLE));
	}

	@Test
	public void testCombineTwoPoliciesFirstIndeterminateD() {
		CompositeDecisionRule r1 = c.createMock(CompositeDecisionRule.class);
		CompositeDecisionRule r2 = c.createMock(CompositeDecisionRule.class);

		decisions = new LinkedList<CompositeDecisionRule>();
		decisions.add(r1);
		decisions.add(r2);

		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluate(context)).andReturn(Decision.INDETERMINATE_D);

		c.replay();
		Decision decision = algorithm.combine(context, decisions);
		c.verify();
		assertThat(decision, is(Decision.NOT_APPLICABLE));
	}

	@Test
	public void testCombineThreePoliciesFirstDeny() {
		CompositeDecisionRule r1 = c.createMock(CompositeDecisionRule.class);
		CompositeDecisionRule r2 = c.createMock(CompositeDecisionRule.class);
		CompositeDecisionRule r3 = c.createMock(CompositeDecisionRule.class);

		decisions = new LinkedList<CompositeDecisionRule>();
		decisions.add(r1);
		decisions.add(r2);
		decisions.add(r3);

		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluate(context)).andReturn(Decision.DENY);
		expect(r3.createContext(context)).andReturn(context);
		expect(r3.evaluate(context)).andReturn(Decision.PERMIT);

		c.replay();
		Decision decision = algorithm.combine(context, decisions);
		c.verify();
		assertThat(decision, is(Decision.PERMIT));
	}
}
