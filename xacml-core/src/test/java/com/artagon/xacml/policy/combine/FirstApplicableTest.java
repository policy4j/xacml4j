package com.artagon.xacml.policy.combine;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.policy.DecisionCombiningAlgorithm;
import com.artagon.xacml.policy.DecisionResult;
import com.artagon.xacml.policy.MatchResult;
import com.artagon.xacml.policy.MockDecision;
import com.artagon.xacml.policy.XacmlPolicyTestCase;

public class FirstApplicableTest extends XacmlPolicyTestCase
{
	
	private List<MockDecision> decisions;
	private DecisionCombiningAlgorithm<MockDecision> algorithm;
	
	@Before
	public void init(){
		this.decisions = new LinkedList<MockDecision>();
		this.algorithm = new FirstApplicable<MockDecision>("test");
	}
	
	@Test
	public void testNoApplicableDecisions()
	{
		decisions.add(new MockDecision(DecisionResult.PERMIT, MatchResult.NOMATCH));
		assertEquals(DecisionResult.NOT_APPLICABLE, algorithm.combine(decisions, context));
		decisions.add(new MockDecision(DecisionResult.DENY, MatchResult.NOMATCH));
		assertEquals(DecisionResult.NOT_APPLICABLE, algorithm.combine(decisions, context));
		decisions.add(new MockDecision(DecisionResult.INDETERMINATE, MatchResult.NOMATCH));
		assertEquals(DecisionResult.NOT_APPLICABLE, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testPermitApplicable()
	{
		decisions.add(new MockDecision(DecisionResult.PERMIT, MatchResult.MATCH));
		assertEquals(DecisionResult.PERMIT, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testFirstPermitNotApplicableSecondPermitIsApplicable()
	{
		decisions.add(new MockDecision(DecisionResult.PERMIT, MatchResult.NOMATCH));
		assertEquals(DecisionResult.NOT_APPLICABLE, algorithm.combine(decisions, context));
		decisions.add(new MockDecision(DecisionResult.PERMIT, MatchResult.MATCH));
		assertEquals(DecisionResult.PERMIT, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testDenyApplicable()
	{
		decisions.add(new MockDecision(DecisionResult.DENY, MatchResult.MATCH));
		assertEquals(DecisionResult.DENY, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testIndeterminateApplicable()
	{
		decisions.add(new MockDecision(DecisionResult.INDETERMINATE, MatchResult.MATCH));
		assertEquals(DecisionResult.INDETERMINATE, algorithm.combine(decisions, context));
	}
	
	
}
