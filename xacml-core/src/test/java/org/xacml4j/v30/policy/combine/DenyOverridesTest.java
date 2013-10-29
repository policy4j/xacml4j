package org.xacml4j.v30.policy.combine;

import static org.easymock.EasyMock.createControl;
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
import org.xacml4j.v30.pdp.DecisionCombiningAlgorithm;


public class DenyOverridesTest
{
	private List<DecisionRule> decisions;
	private DecisionCombiningAlgorithm<DecisionRule> algorithm;
	private EvaluationContext context;
	private IMocksControl c;

	@Before
	public void init(){
		this.c = createControl();
		this.decisions = new LinkedList<DecisionRule>();
		this.algorithm = new DenyOverrides<DecisionRule>("aaaa");
		this.context = c.createMock(EvaluationContext.class);
	}

	@Test
	public void testCombineWithNoDecisions()
	{
		assertEquals(Decision.NOT_APPLICABLE, algorithm.combine(context, decisions));
	}

	@Test
	public void testCombineWithAllNotApplicable() throws EvaluationException
	{
		DecisionRule r1 = c.createMock(DecisionRule.class);
		DecisionRule r2 = c.createMock(DecisionRule.class);
		DecisionRule r3 = c.createMock(DecisionRule.class);
		decisions = new LinkedList<DecisionRule>();
		decisions.add(r1);
		decisions.add(r2);
		decisions.add(r3);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluate(context)).andReturn(Decision.NOT_APPLICABLE);
		expect(r2.createContext(context)).andReturn(context);
		expect(r2.evaluate(context)).andReturn(Decision.NOT_APPLICABLE);
		expect(r3.createContext(context)).andReturn(context);
		expect(r3.evaluate(context)).andReturn(Decision.NOT_APPLICABLE);
		c.replay();
		assertEquals(Decision.NOT_APPLICABLE, algorithm.combine(context, decisions));
		c.verify();
	}

	@Test
	public void testCombineWithPermit() throws EvaluationException
	{
		DecisionRule r1 = c.createMock(DecisionRule.class);
		DecisionRule r2 = c.createMock(DecisionRule.class);
		decisions.add(r1);
		decisions.add(r2);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluate(context)).andReturn(Decision.PERMIT);
		expect(r2.createContext(context)).andReturn(context);
		expect(r2.evaluate(context)).andReturn(Decision.PERMIT);
		c.replay();
		assertEquals(Decision.PERMIT, algorithm.combine(context, decisions));
		c.verify();
	}

	@Test
	public void testCombineWithDeny() throws EvaluationException
	{
		DecisionRule r1 = c.createMock(DecisionRule.class);
		DecisionRule r2 = c.createMock(DecisionRule.class);
		decisions.add(r1);
		decisions.add(r2);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluate(context)).andReturn(Decision.DENY);
		c.replay();
		assertEquals(Decision.DENY, algorithm.combine(context, decisions));
		c.verify();
	}

	@Test
	public void testCombineWithInderterminateP() throws EvaluationException
	{
		DecisionRule r1 = c.createMock(DecisionRule.class);
		decisions.add(r1);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluate(context)).andReturn(Decision.INDETERMINATE_P);
		c.replay();
		assertEquals(Decision.INDETERMINATE_P, algorithm.combine(context, decisions));
		c.verify();
	}

	@Test
	public void testCombineWithInderterminateD() throws EvaluationException
	{
		DecisionRule r1 = c.createMock(DecisionRule.class);
		decisions.add(r1);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluate(context)).andReturn(Decision.INDETERMINATE_D);
		c.replay();
		assertEquals(Decision.INDETERMINATE_D, algorithm.combine(context, decisions));
		c.verify();
	}

	@Test
	public void testCombineWithInderterminate() throws EvaluationException
	{
		DecisionRule r1 = c.createMock(DecisionRule.class);
		decisions.add(r1);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluate(context)).andReturn(Decision.INDETERMINATE);
		c.replay();
		assertEquals(Decision.INDETERMINATE_DP, algorithm.combine(context, decisions));
		c.verify();
	}

	@Test
	public void testCombinePermitWithInderterminate() throws EvaluationException
	{
		DecisionRule r1 = c.createMock(DecisionRule.class);
		DecisionRule r2 = c.createMock(DecisionRule.class);
		decisions.add(r1);
		decisions.add(r2);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluate(context)).andReturn(Decision.PERMIT);
		expect(r2.createContext(context)).andReturn(context);
		expect(r2.evaluate(context)).andReturn(Decision.INDETERMINATE_DP);
		c.replay();
		assertEquals(Decision.INDETERMINATE_DP, algorithm.combine(context, decisions));
		c.verify();
	}

	@Test
	public void testCombineInderterminateDPWithPermit() throws EvaluationException
	{
		DecisionRule r1 = c.createMock(DecisionRule.class);
		DecisionRule r2 = c.createMock(DecisionRule.class);
		decisions.add(r1);
		decisions.add(r2);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluate(context)).andReturn(Decision.INDETERMINATE_DP);
		expect(r2.createContext(context)).andReturn(context);
		expect(r2.evaluate(context)).andReturn(Decision.PERMIT);
		c.replay();
		assertEquals(Decision.INDETERMINATE_DP, algorithm.combine(context, decisions));
		c.verify();
	}

	@Test
	public void testCombinePermitWithInderterminateDP() throws EvaluationException
	{
		DecisionRule r1 = c.createMock(DecisionRule.class);
		DecisionRule r2 = c.createMock(DecisionRule.class);
		decisions.add(r1);
		decisions.add(r2);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluate(context)).andReturn(Decision.PERMIT);
		expect(r2.createContext(context)).andReturn(context);
		expect(r2.evaluate(context)).andReturn(Decision.INDETERMINATE_DP);
		c.replay();
		assertEquals(Decision.INDETERMINATE_DP, algorithm.combine(context, decisions));
		c.verify();
	}

	@Test
	public void testAnyDecisionDenyResultIsDeny() throws EvaluationException
	{
		DecisionRule r1 = c.createMock(DecisionRule.class);
		DecisionRule r2 = c.createMock(DecisionRule.class);
		decisions = new LinkedList<DecisionRule>();
		decisions.add(r1);
		decisions.add(r2);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluate(context)).andReturn(Decision.DENY);
		c.replay();
		assertEquals(Decision.DENY, algorithm.combine(context, decisions));
  		c.verify();
	}
}
