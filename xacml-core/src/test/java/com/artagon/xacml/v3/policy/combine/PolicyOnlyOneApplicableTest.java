package com.artagon.xacml.v3.policy.combine;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.policy.CompositeDecisionRule;
import com.artagon.xacml.v3.policy.MatchResult;
import com.artagon.xacml.v3.policy.MockCompositeDecisionRule;
import com.artagon.xacml.v3.policy.XacmlPolicyTestCase;

public class PolicyOnlyOneApplicableTest extends XacmlPolicyTestCase
{
	private PolicyOnlyOneApplicable c;
	private List<CompositeDecisionRule> d;
	
	@Before
	public void init(){
		this.c = new PolicyOnlyOneApplicable();
		this.d = new LinkedList<CompositeDecisionRule>();
	}
	
	@Test
	public void testWithOnlyOneDecisionPermitAndNoMatch()
	{
		d.add(new MockCompositeDecisionRule(Decision.PERMIT, MatchResult.NOMATCH));
		assertEquals(Decision.NOT_APPLICABLE, c.combine(d, context));
	}
	
	@Test
	public void testWithOnlyOneDecisionDenyAndNoMatch()
	{
		d.add(new MockCompositeDecisionRule(Decision.DENY, MatchResult.NOMATCH));
		assertEquals(Decision.NOT_APPLICABLE, c.combine(d, context));
	}
	
	@Test
	public void testWithOnlyOneDecisionIndeterminateAndNoMatch()
	{
		d.add(new MockCompositeDecisionRule(Decision.INDETERMINATE, MatchResult.NOMATCH));
		assertEquals(Decision.NOT_APPLICABLE, c.combine(d, context));
	}
	
	@Test
	public void testWithOnlyOneDecisionPermitAndMatchIndeterminate()
	{
		d.add(new MockCompositeDecisionRule(Decision.PERMIT, MatchResult.INDETERMINATE));
		assertEquals(Decision.INDETERMINATE, c.combine(d, context));
	}
	
	@Test
	public void testWithOnlyOneDecisionDenyAndMatchIndeterminate()
	{
		d.add(new MockCompositeDecisionRule(Decision.PERMIT, MatchResult.INDETERMINATE));
		assertEquals(Decision.INDETERMINATE, c.combine(d, context));
	}
	
	@Test
	public void testWithOnlyOneDecisionIndeterminateAndMatchIndeterminate()
	{
		d.add(new MockCompositeDecisionRule(Decision.PERMIT, MatchResult.INDETERMINATE));
		assertEquals(Decision.INDETERMINATE, c.combine(d, context));
	}
	
	@Test
	public void testMoreThanOneIsApplicable()
	{
		List<CompositeDecisionRule> d = new LinkedList<CompositeDecisionRule>();
		d.add(new MockCompositeDecisionRule(Decision.PERMIT, MatchResult.MATCH));
		d.add(new MockCompositeDecisionRule(Decision.DENY, MatchResult.MATCH));
		assertEquals(Decision.INDETERMINATE, c.combine(d, context));
	}
	
	@Test
	public void testOnlyOneIsApplicable()
	{
		List<CompositeDecisionRule> d = new LinkedList<CompositeDecisionRule>();
		d.add(new MockCompositeDecisionRule(Decision.PERMIT, MatchResult.MATCH));
		d.add(new MockCompositeDecisionRule(Decision.DENY, MatchResult.INDETERMINATE));
		d.add(new MockCompositeDecisionRule(Decision.INDETERMINATE, MatchResult.INDETERMINATE));
		assertEquals(Decision.INDETERMINATE, c.combine(d, context));
	}
}
