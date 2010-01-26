package com.artagon.xacml.v30.policy.combine;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.DecisionResult;
import com.artagon.xacml.v30.policy.CompositeDecision;
import com.artagon.xacml.v30.policy.MatchResult;
import com.artagon.xacml.v30.policy.MockCompositeDecision;
import com.artagon.xacml.v30.policy.XacmlPolicyTestCase;
import com.artagon.xacml.v30.policy.combine.PolicyOnlyOneApplicable;

public class PolicyOnlyOneApplicableTest extends XacmlPolicyTestCase
{
	private PolicyOnlyOneApplicable c;
	private List<CompositeDecision> d;
	
	@Before
	public void init(){
		this.c = new PolicyOnlyOneApplicable();
		this.d = new LinkedList<CompositeDecision>();
	}
	
	@Test
	public void testWithOnlyOneDecisionPermitAndNoMatch()
	{
		d.add(new MockCompositeDecision(DecisionResult.PERMIT, MatchResult.NOMATCH));
		assertEquals(DecisionResult.NOT_APPLICABLE, c.combine(d, context));
	}
	
	@Test
	public void testWithOnlyOneDecisionDenyAndNoMatch()
	{
		d.add(new MockCompositeDecision(DecisionResult.DENY, MatchResult.NOMATCH));
		assertEquals(DecisionResult.NOT_APPLICABLE, c.combine(d, context));
	}
	
	@Test
	public void testWithOnlyOneDecisionIndeterminateAndNoMatch()
	{
		d.add(new MockCompositeDecision(DecisionResult.INDETERMINATE, MatchResult.NOMATCH));
		assertEquals(DecisionResult.NOT_APPLICABLE, c.combine(d, context));
	}
	
	@Test
	public void testWithOnlyOneDecisionPermitAndMatchIndeterminate()
	{
		d.add(new MockCompositeDecision(DecisionResult.PERMIT, MatchResult.INDETERMINATE));
		assertEquals(DecisionResult.INDETERMINATE, c.combine(d, context));
	}
	
	@Test
	public void testWithOnlyOneDecisionDenyAndMatchIndeterminate()
	{
		d.add(new MockCompositeDecision(DecisionResult.PERMIT, MatchResult.INDETERMINATE));
		assertEquals(DecisionResult.INDETERMINATE, c.combine(d, context));
	}
	
	@Test
	public void testWithOnlyOneDecisionIndeterminateAndMatchIndeterminate()
	{
		d.add(new MockCompositeDecision(DecisionResult.PERMIT, MatchResult.INDETERMINATE));
		assertEquals(DecisionResult.INDETERMINATE, c.combine(d, context));
	}
	
	@Test
	public void testMoreThanOneIsApplicable()
	{
		List<CompositeDecision> d = new LinkedList<CompositeDecision>();
		d.add(new MockCompositeDecision(DecisionResult.PERMIT, MatchResult.MATCH));
		d.add(new MockCompositeDecision(DecisionResult.DENY, MatchResult.MATCH));
		assertEquals(DecisionResult.INDETERMINATE, c.combine(d, context));
	}
	
	@Test
	public void testOnlyOneIsApplicable()
	{
		List<CompositeDecision> d = new LinkedList<CompositeDecision>();
		d.add(new MockCompositeDecision(DecisionResult.PERMIT, MatchResult.MATCH));
		d.add(new MockCompositeDecision(DecisionResult.DENY, MatchResult.INDETERMINATE));
		d.add(new MockCompositeDecision(DecisionResult.INDETERMINATE, MatchResult.INDETERMINATE));
		assertEquals(DecisionResult.INDETERMINATE, c.combine(d, context));
	}
}
