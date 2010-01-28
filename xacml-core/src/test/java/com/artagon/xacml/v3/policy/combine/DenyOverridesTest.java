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

public class DenyOverridesTest extends XacmlPolicyTestCase
{
	private List<MockDecisionRule> decisions;
	private DecisionCombiningAlgorithm<MockDecisionRule> algorithm;
	
	@Before
	public void init(){
		this.decisions = new LinkedList<MockDecisionRule>();
		this.algorithm = new DenyOverrides<MockDecisionRule>("aaaa");
	}
	
	@Test
	public void testCombineWithNoDecisions()
	{
		assertEquals(Decision.NOT_APPLICABLE, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineWithAllNotApplicable()
	{
		decisions.add(new MockDecisionRule(Decision.NOT_APPLICABLE));
		decisions.add(new MockDecisionRule(Decision.NOT_APPLICABLE));
		decisions.add(new MockDecisionRule(Decision.NOT_APPLICABLE));
		assertEquals(Decision.NOT_APPLICABLE, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineWithPermit()
	{
		decisions.add(new MockDecisionRule(Decision.PERMIT));
		assertEquals(Decision.PERMIT, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineWithDeny()
	{
		decisions.add(new MockDecisionRule(Decision.DENY));
		assertEquals(Decision.DENY, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineWithInderterminateP()
	{
		decisions.add(new MockDecisionRule(Decision.INDETERMINATE_P));
		assertEquals(Decision.INDETERMINATE_P, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineWithInderterminateD()
	{
		decisions.add(new MockDecisionRule(Decision.INDETERMINATE_D));
		assertEquals(Decision.INDETERMINATE_D, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineWithInderterminateDP()
	{
		decisions.add(new MockDecisionRule(Decision.INDETERMINATE_DP));
		assertEquals(Decision.INDETERMINATE_DP, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineOneDeny()
	{
		decisions.add(new MockDecisionRule(Decision.DENY));
		assertEquals(Decision.DENY, algorithm.combine(decisions, context));
		decisions.add(new MockDecisionRule(Decision.PERMIT));	
		assertEquals(Decision.DENY, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineAnyIndeterminateDAndAtLeastOneIndeterminateP()
	{
		decisions.add(new MockDecisionRule(Decision.INDETERMINATE_D));
		decisions.add(new MockDecisionRule(Decision.INDETERMINATE_D));
		decisions.add(new MockDecisionRule(Decision.INDETERMINATE_P));
		assertEquals(Decision.INDETERMINATE_DP, algorithm.combine(decisions, context));
		
	}
	
	@Test
	public void testCombineAnyIndeterminateDAndAtLeastOnePermit()
	{
		decisions.add(new MockDecisionRule(Decision.PERMIT));
		decisions.add(new MockDecisionRule(Decision.INDETERMINATE_D));
		decisions.add(new MockDecisionRule(Decision.INDETERMINATE_P));
		assertEquals(Decision.INDETERMINATE_DP, algorithm.combine(decisions, context));
	}
	
}
