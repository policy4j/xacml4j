package com.artagon.xacml.v3.policy.combine;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.policy.DecisionCombiningAlgorithm;
import com.artagon.xacml.v3.policy.DecisionRule;
import com.artagon.xacml.v3.policy.XacmlPolicyTestCase;

public class DenyOverridesTest extends XacmlPolicyTestCase
{
	private List<DecisionRule> decisions;
	private DecisionCombiningAlgorithm<DecisionRule> algorithm;
	
	@Before
	public void init(){
		this.decisions = new LinkedList<DecisionRule>();
		this.algorithm = new DenyOverrides<DecisionRule>("aaaa");
	}
	
	@Test
	public void testCombineWithNoDecisions()
	{
		assertEquals(Decision.NOT_APPLICABLE, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineWithAllNotApplicable()
	{
		DecisionRule r1 = createStrictMock(DecisionRule.class);
		DecisionRule r2 = createStrictMock(DecisionRule.class);
		DecisionRule r3 = createStrictMock(DecisionRule.class);
		decisions = new LinkedList<DecisionRule>();
		decisions.add(r1);
		decisions.add(r2);
		decisions.add(r3);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluateIfApplicable(context)).andReturn(Decision.NOT_APPLICABLE);
		expect(r2.createContext(context)).andReturn(context);
		expect(r2.evaluateIfApplicable(context)).andReturn(Decision.NOT_APPLICABLE);
		expect(r3.createContext(context)).andReturn(context);
		expect(r3.evaluateIfApplicable(context)).andReturn(Decision.NOT_APPLICABLE);
		replay(r1, r2, r3);
		assertEquals(Decision.NOT_APPLICABLE, algorithm.combine(decisions, context));
		verify(r1, r2, r3);
	}
	
	@Test
	public void testCombineWithPermit()
	{
		DecisionRule r1 = createStrictMock(DecisionRule.class);
		DecisionRule r2 = createStrictMock(DecisionRule.class);
		decisions.add(r1);
		decisions.add(r2);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluateIfApplicable(context)).andReturn(Decision.PERMIT);
		expect(r2.createContext(context)).andReturn(context);
		expect(r2.evaluateIfApplicable(context)).andReturn(Decision.PERMIT);
		replay(r1, r2);
		assertEquals(Decision.PERMIT, algorithm.combine(decisions, context));
		verify(r1, r2);
	}
	
	@Test
	public void testCombineWithDeny()
	{
		DecisionRule r1 = createStrictMock(DecisionRule.class);
		DecisionRule r2 = createStrictMock(DecisionRule.class);
		decisions.add(r1);
		decisions.add(r2);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluateIfApplicable(context)).andReturn(Decision.DENY);
		replay(r1, r2);
		assertEquals(Decision.DENY, algorithm.combine(decisions, context));
		verify(r1, r2);
	}
		
	@Test
	public void testCombineWithInderterminateP()
	{
		DecisionRule r1 = createStrictMock(DecisionRule.class);
		decisions.add(r1);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluateIfApplicable(context)).andReturn(Decision.INDETERMINATE_P);
		replay(r1);
		assertEquals(Decision.INDETERMINATE_P, algorithm.combine(decisions, context));
		verify(r1);
	}
	
	@Test
	public void testCombineWithInderterminateD()
	{
		DecisionRule r1 = createStrictMock(DecisionRule.class);
		decisions.add(r1);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluateIfApplicable(context)).andReturn(Decision.INDETERMINATE_D);
		replay(r1);
		assertEquals(Decision.INDETERMINATE_D, algorithm.combine(decisions, context));
		verify(r1);
	}
	
	@Test
	public void testCombineWithInderterminateDP()
	{
		DecisionRule r1 = createStrictMock(DecisionRule.class);
		DecisionRule r2 = createStrictMock(DecisionRule.class);
		decisions.add(r1);
		decisions.add(r2);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluateIfApplicable(context)).andReturn(Decision.INDETERMINATE_DP);
		expect(r2.createContext(context)).andReturn(context);
		expect(r2.evaluateIfApplicable(context)).andReturn(Decision.PERMIT);
		replay(r1, r2);
		assertEquals(Decision.INDETERMINATE_DP, algorithm.combine(decisions, context));
		verify(r1, r2);
	}
	
	@Test
	public void testAnyDecisionDenyResultIsDeny()
	{
		DecisionRule r1 = createStrictMock(DecisionRule.class);
		DecisionRule r2 = createStrictMock(DecisionRule.class);
		decisions = new LinkedList<DecisionRule>();
		decisions.add(r1);
		decisions.add(r2);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluateIfApplicable(context)).andReturn(Decision.DENY);
		replay(r1, r2);
		assertEquals(Decision.DENY, algorithm.combine(decisions, context));
  		verify(r1, r2);
	}
}
