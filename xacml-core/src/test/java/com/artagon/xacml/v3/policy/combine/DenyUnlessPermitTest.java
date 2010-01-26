package com.artagon.xacml.v3.policy.combine;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.DecisionResult;
import com.artagon.xacml.v3.policy.DecisionCombiningAlgorithm;
import com.artagon.xacml.v3.policy.MockDecision;
import com.artagon.xacml.v3.policy.XacmlPolicyTestCase;
import com.artagon.xacml.v3.policy.combine.DenyUnlessPermit;

public class DenyUnlessPermitTest extends XacmlPolicyTestCase
{
	private List<MockDecision> decisions;
	private DecisionCombiningAlgorithm<MockDecision> algorithm;
	
	@Before
	public void init(){
		this.decisions = new LinkedList<MockDecision>();
		this.algorithm = new DenyUnlessPermit<MockDecision>("aaaa");
	}
	
	@Test
	public void testCombineWithNoDecisions()
	{
		assertEquals(DecisionResult.DENY, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineWithPermit()
	{
		decisions.add(new MockDecision(DecisionResult.PERMIT));
		assertEquals(DecisionResult.PERMIT, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineWithNotApplicable()
	{
		decisions.add(new MockDecision(DecisionResult.NOT_APPLICABLE));
		assertEquals(DecisionResult.DENY, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineWithIndeterminate()
	{
		decisions.add(new MockDecision(DecisionResult.INDETERMINATE));
		assertEquals(DecisionResult.DENY, algorithm.combine(decisions, context));
		decisions.add(new MockDecision(DecisionResult.NOT_APPLICABLE));
		assertEquals(DecisionResult.DENY, algorithm.combine(decisions, context));
		decisions.add(new MockDecision(DecisionResult.DENY));
		assertEquals(DecisionResult.DENY, algorithm.combine(decisions, context));
		decisions.add(new MockDecision(DecisionResult.PERMIT));
		assertEquals(DecisionResult.PERMIT, algorithm.combine(decisions, context));
	}
	
	
	@Test
	public void testCombineWithIndeterminateD()
	{
		decisions.add(new MockDecision(DecisionResult.INDETERMINATE_D));
		assertEquals(DecisionResult.DENY, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineWithIndeterminateP()
	{
		decisions.add(new MockDecision(DecisionResult.INDETERMINATE_P));
		assertEquals(DecisionResult.DENY, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineWithIndeterminateDP()
	{
		decisions.add(new MockDecision(DecisionResult.INDETERMINATE_DP));
		assertEquals(DecisionResult.DENY, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineWithDenyAndPermit()
	{
		decisions.add(new MockDecision(DecisionResult.PERMIT));
		assertEquals(DecisionResult.PERMIT, algorithm.combine(decisions, context));
		decisions.add(new MockDecision(DecisionResult.DENY));
		assertEquals(DecisionResult.PERMIT, algorithm.combine(decisions, context));
	}
}
