package com.artagon.xacml.v3.policy.impl.combine;

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
import com.artagon.xacml.v3.DecisionCombiningAlgorithm;
import com.artagon.xacml.v3.DecisionRule;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;

public class FirstApplicableTest
{
	
	private List<DecisionRule> decisions;
	private DecisionCombiningAlgorithm<DecisionRule> algorithm;
	private EvaluationContext context;
	
	@Before
	public void init(){
		this.decisions = new LinkedList<DecisionRule>();
		this.algorithm = new FirstApplicable<DecisionRule>("test");
		this.context = createStrictMock(EvaluationContext.class);
	}
	
	@Test
	public void testNoDecisions()
	{
		assertEquals(Decision.NOT_APPLICABLE, algorithm.combine(decisions, context));
	}

	
	@Test
	public void testNoApplicableDecisions() throws EvaluationException
	{
		DecisionRule r1 = createStrictMock(DecisionRule.class);
		DecisionRule r2 = createStrictMock(DecisionRule.class);
		decisions.add(r1);
		decisions.add(r2);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluateIfApplicable(context)).andReturn(Decision.NOT_APPLICABLE);
		expect(r2.createContext(context)).andReturn(context);
		expect(r2.evaluateIfApplicable(context)).andReturn(Decision.NOT_APPLICABLE);
		replay(r1, r2);
		assertEquals(Decision.NOT_APPLICABLE, algorithm.combine(decisions, context));
		verify(r1, r2);
	}
	
	@Test
	public void testPermit() throws EvaluationException
	{
		DecisionRule r1 = createStrictMock(DecisionRule.class);
		DecisionRule r2 = createStrictMock(DecisionRule.class);
		decisions.add(r1);
		decisions.add(r2);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluateIfApplicable(context)).andReturn(Decision.PERMIT);
		replay(r1, r2);
		assertEquals(Decision.PERMIT, algorithm.combine(decisions, context));
		verify(r1, r2);
	}
	
	@Test
	public void testDeny() throws EvaluationException
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
	public void testIndeterminate() throws EvaluationException
	{
		DecisionRule r1 = createStrictMock(DecisionRule.class);
		DecisionRule r2 = createStrictMock(DecisionRule.class);
		decisions.add(r1);
		decisions.add(r2);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluateIfApplicable(context)).andReturn(Decision.INDETERMINATE);
		replay(r1, r2);
		assertEquals(Decision.INDETERMINATE, algorithm.combine(decisions, context));
		verify(r1, r2);
	}
	
	@Test
	public void testIndeterminateD() throws EvaluationException
	{
		DecisionRule r1 = createStrictMock(DecisionRule.class);
		DecisionRule r2 = createStrictMock(DecisionRule.class);
		decisions.add(r1);
		decisions.add(r2);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluateIfApplicable(context)).andReturn(Decision.INDETERMINATE_D);
		replay(r1, r2);
		assertEquals(Decision.INDETERMINATE_D, algorithm.combine(decisions, context));
		verify(r1, r2);
	}
	
	@Test
	public void testIndeterminateP() throws EvaluationException
	{
		DecisionRule r1 = createStrictMock(DecisionRule.class);
		DecisionRule r2 = createStrictMock(DecisionRule.class);
		decisions.add(r1);
		decisions.add(r2);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluateIfApplicable(context)).andReturn(Decision.INDETERMINATE_P);
		replay(r1, r2);
		assertEquals(Decision.INDETERMINATE_P, algorithm.combine(decisions, context));
		verify(r1, r2);
	}
	
	@Test
	public void testIndeterminateDP() throws EvaluationException
	{
		DecisionRule r1 = createStrictMock(DecisionRule.class);
		DecisionRule r2 = createStrictMock(DecisionRule.class);
		decisions.add(r1);
		decisions.add(r2);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluateIfApplicable(context)).andReturn(Decision.INDETERMINATE_DP);
		replay(r1, r2);
		assertEquals(Decision.INDETERMINATE_DP, algorithm.combine(decisions, context));
		verify(r1, r2);
	}
}
