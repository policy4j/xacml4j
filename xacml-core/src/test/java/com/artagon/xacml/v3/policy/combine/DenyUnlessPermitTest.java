package com.artagon.xacml.v3.policy.combine;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.policy.DecisionCombiningAlgorithm;
import com.artagon.xacml.v3.policy.MockDecisionRule;
import com.artagon.xacml.v3.policy.XacmlPolicyTestCase;

public class DenyUnlessPermitTest extends XacmlPolicyTestCase
{
	private List<MockDecisionRule> decisions;
	private DecisionCombiningAlgorithm<MockDecisionRule> algorithm;
	
	@Before
	public void init(){
		this.decisions = new LinkedList<MockDecisionRule>();
		this.algorithm = new DenyUnlessPermit<MockDecisionRule>("aaaa");
	}
	
	@Test
	public void testCombineWithNoDecisions()
	{
		assertEquals(Decision.DENY, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineWithPermit()
	{
		decisions.add(new MockDecisionRule(Decision.PERMIT));
		assertEquals(Decision.PERMIT, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineWithNotApplicable()
	{
		decisions.add(new MockDecisionRule(Decision.NOT_APPLICABLE));
		assertEquals(Decision.DENY, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineWithIndeterminate()
	{
		decisions.add(new MockDecisionRule(Decision.INDETERMINATE));
		assertEquals(Decision.DENY, algorithm.combine(decisions, context));
		decisions.add(new MockDecisionRule(Decision.NOT_APPLICABLE));
		assertEquals(Decision.DENY, algorithm.combine(decisions, context));
		decisions.add(new MockDecisionRule(Decision.DENY));
		assertEquals(Decision.DENY, algorithm.combine(decisions, context));
		decisions.add(new MockDecisionRule(Decision.PERMIT));
		assertEquals(Decision.PERMIT, algorithm.combine(decisions, context));
	}
	
	
	@Test
	public void testCombineWithIndeterminateD()
	{
		decisions.add(new MockDecisionRule(Decision.INDETERMINATE_D));
		assertEquals(Decision.DENY, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineWithIndeterminateP()
	{
		decisions.add(new MockDecisionRule(Decision.INDETERMINATE_P));
		assertEquals(Decision.DENY, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineWithIndeterminateDP()
	{
		decisions.add(new MockDecisionRule(Decision.INDETERMINATE_DP));
		assertEquals(Decision.DENY, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineWithDenyAndPermit()
	{
		decisions.add(new MockDecisionRule(Decision.PERMIT));
		assertEquals(Decision.PERMIT, algorithm.combine(decisions, context));
		decisions.add(new MockDecisionRule(Decision.DENY));
		assertEquals(Decision.PERMIT, algorithm.combine(decisions, context));
	}
}
