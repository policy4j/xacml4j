package org.xacml4j.v30.policy.combine;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.pdp.DecisionCombiningAlgorithm;
import org.xacml4j.v30.policy.combine.OnPermitApplySecondPolicyCombiningAlgorithm;


public class OnPermitApplySecondPolicyTest 
{
	private List<CompositeDecisionRule> decisions;
	private DecisionCombiningAlgorithm<CompositeDecisionRule> algorithm;
	private EvaluationContext context;
	private IMocksControl c;
	
	@Before
	public void init(){
		this.c = createControl();
		this.decisions = new LinkedList<CompositeDecisionRule>();
		this.algorithm = new OnPermitApplySecondPolicyCombiningAlgorithm();
		this.context = c.createMock(EvaluationContext.class);
	}
	
	@Test
	public void testCombineWithNoDecisions()
	{
		assertEquals(Decision.INDETERMINATE_DP, algorithm.combine(context, decisions));
	}
	
	@Test
	public void testCombineFirstPermitSecondNotApplicable() throws EvaluationException
	{
		CompositeDecisionRule r1 = c.createMock(CompositeDecisionRule.class);
		CompositeDecisionRule r2 = c.createMock(CompositeDecisionRule.class);
	
		decisions = new LinkedList<CompositeDecisionRule>();
		decisions.add(r1);
		decisions.add(r2);
	
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluateIfMatch(context)).andReturn(Decision.PERMIT);
		expect(r2.createContext(context)).andReturn(context);
		expect(r2.evaluateIfMatch(context)).andReturn(Decision.NOT_APPLICABLE);
		c.replay();
		assertEquals(Decision.NOT_APPLICABLE, algorithm.combine(context, decisions));
		c.verify();
	}
	
	@Test
	public void testCombineFirstPermitSecondPermit() throws EvaluationException
	{
		CompositeDecisionRule r1 = c.createMock(CompositeDecisionRule.class);
		CompositeDecisionRule r2 = c.createMock(CompositeDecisionRule.class);
	
		decisions = new LinkedList<CompositeDecisionRule>();
		decisions.add(r1);
		decisions.add(r2);
	
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluateIfMatch(context)).andReturn(Decision.PERMIT);
		expect(r2.createContext(context)).andReturn(context);
		expect(r2.evaluateIfMatch(context)).andReturn(Decision.PERMIT);
		c.replay();
		assertEquals(Decision.PERMIT, algorithm.combine(context, decisions));
		c.verify();
	}
	
	@Test
	public void testCombineFirstPermitSecondDeny() throws EvaluationException
	{
		CompositeDecisionRule r1 = c.createMock(CompositeDecisionRule.class);
		CompositeDecisionRule r2 = c.createMock(CompositeDecisionRule.class);
	
		decisions = new LinkedList<CompositeDecisionRule>();
		decisions.add(r1);
		decisions.add(r2);
	
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluateIfMatch(context)).andReturn(Decision.PERMIT);
		expect(r2.createContext(context)).andReturn(context);
		expect(r2.evaluateIfMatch(context)).andReturn(Decision.DENY);
		c.replay();
		assertEquals(Decision.DENY, algorithm.combine(context, decisions));
		c.verify();
	}
}
