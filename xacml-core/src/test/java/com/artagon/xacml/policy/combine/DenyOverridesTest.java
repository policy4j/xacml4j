package com.artagon.xacml.policy.combine;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.policy.DecisionCombiningAlgorithm;
import com.artagon.xacml.policy.DecisionResult;
import com.artagon.xacml.policy.MockDecision;
import com.artagon.xacml.policy.XacmlPolicyTestCase;

public class DenyOverridesTest extends XacmlPolicyTestCase
{
	private List<MockDecision> decisions;
	private DecisionCombiningAlgorithm<MockDecision> algorithm;
	
	@Before
	public void init(){
		this.decisions = new LinkedList<MockDecision>();
		this.algorithm = new DenyOverrides<MockDecision>("aaaa");
	}
	
	@Test
	public void testCombineWithNoDecisions()
	{
		assertEquals(DecisionResult.NOT_APPLICABLE, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineWithAllNotApplicable()
	{
		decisions.add(new MockDecision(DecisionResult.NOT_APPLICABLE));
		decisions.add(new MockDecision(DecisionResult.NOT_APPLICABLE));
		decisions.add(new MockDecision(DecisionResult.NOT_APPLICABLE));
		assertEquals(DecisionResult.NOT_APPLICABLE, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineWithPermit()
	{
		decisions.add(new MockDecision(DecisionResult.PERMIT));
		assertEquals(DecisionResult.PERMIT, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineWithDeny()
	{
		decisions.add(new MockDecision(DecisionResult.DENY));
		assertEquals(DecisionResult.DENY, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineWithInderterminateP()
	{
		decisions.add(new MockDecision(DecisionResult.INDETERMINATE_P));
		assertEquals(DecisionResult.INDETERMINATE_P, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineWithInderterminateD()
	{
		decisions.add(new MockDecision(DecisionResult.INDETERMINATE_D));
		assertEquals(DecisionResult.INDETERMINATE_D, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineWithInderterminateDP()
	{
		decisions.add(new MockDecision(DecisionResult.INDETERMINATE_DP));
		assertEquals(DecisionResult.INDETERMINATE_DP, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineOneDeny()
	{
		decisions.add(new MockDecision(DecisionResult.DENY));
		assertEquals(DecisionResult.DENY, algorithm.combine(decisions, context));
		decisions.add(new MockDecision(DecisionResult.PERMIT));	
		assertEquals(DecisionResult.DENY, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineAnyIndeterminateDAndAtLeastOneIndeterminateP()
	{
		decisions.add(new MockDecision(DecisionResult.INDETERMINATE_D));
		decisions.add(new MockDecision(DecisionResult.INDETERMINATE_D));
		decisions.add(new MockDecision(DecisionResult.INDETERMINATE_P));
		assertEquals(DecisionResult.INDETERMINATE_DP, algorithm.combine(decisions, context));
		
	}
	
	@Test
	public void testCombineAnyIndeterminateDAndAtLeastOnePermit()
	{
		decisions.add(new MockDecision(DecisionResult.PERMIT));
		decisions.add(new MockDecision(DecisionResult.INDETERMINATE_D));
		decisions.add(new MockDecision(DecisionResult.INDETERMINATE_P));
		assertEquals(DecisionResult.INDETERMINATE_DP, algorithm.combine(decisions, context));
	}
	
}
