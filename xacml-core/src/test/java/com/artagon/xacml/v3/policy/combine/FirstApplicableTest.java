package com.artagon.xacml.v3.policy.combine;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.policy.DecisionCombiningAlgorithm;
import com.artagon.xacml.v3.policy.MatchResult;
import com.artagon.xacml.v3.policy.MockDecisionRule;
import com.artagon.xacml.v3.policy.XacmlPolicyTestCase;

public class FirstApplicableTest extends XacmlPolicyTestCase
{
	
	private List<MockDecisionRule> decisions;
	private DecisionCombiningAlgorithm<MockDecisionRule> algorithm;
	
	@Before
	public void init(){
		this.decisions = new LinkedList<MockDecisionRule>();
		this.algorithm = new FirstApplicable<MockDecisionRule>("test");
	}
	
	@Test
	public void testNoApplicableDecisions()
	{
		decisions.add(new MockDecisionRule(Decision.PERMIT, MatchResult.NOMATCH));
		assertEquals(Decision.NOT_APPLICABLE, algorithm.combine(decisions, context));
		decisions.add(new MockDecisionRule(Decision.DENY, MatchResult.NOMATCH));
		assertEquals(Decision.NOT_APPLICABLE, algorithm.combine(decisions, context));
		decisions.add(new MockDecisionRule(Decision.INDETERMINATE, MatchResult.NOMATCH));
		assertEquals(Decision.NOT_APPLICABLE, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testPermitApplicable()
	{
		decisions.add(new MockDecisionRule(Decision.PERMIT, MatchResult.MATCH));
		assertEquals(Decision.PERMIT, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testFirstPermitNotApplicableSecondPermitIsApplicable()
	{
		decisions.add(new MockDecisionRule(Decision.PERMIT, MatchResult.NOMATCH));
		assertEquals(Decision.NOT_APPLICABLE, algorithm.combine(decisions, context));
		decisions.add(new MockDecisionRule(Decision.PERMIT, MatchResult.MATCH));
		assertEquals(Decision.PERMIT, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testDenyApplicable()
	{
		decisions.add(new MockDecisionRule(Decision.DENY, MatchResult.MATCH));
		assertEquals(Decision.DENY, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testIndeterminateApplicable()
	{
		decisions.add(new MockDecisionRule(Decision.INDETERMINATE, MatchResult.MATCH));
		assertEquals(Decision.INDETERMINATE, algorithm.combine(decisions, context));
	}
	
	
}
