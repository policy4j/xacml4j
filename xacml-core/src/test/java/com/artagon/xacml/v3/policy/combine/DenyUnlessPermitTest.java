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
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.policy.DecisionCombiningAlgorithm;
import com.artagon.xacml.v3.policy.DecisionRule;

public class DenyUnlessPermitTest
{
	private List<DecisionRule> decisions;
	private DecisionCombiningAlgorithm<DecisionRule> algorithm;
	private EvaluationContext context;
	
	@Before
	public void init(){
		this.decisions = new LinkedList<DecisionRule>();
		this.algorithm = new DenyUnlessPermit<DecisionRule>("aaaa");
		this.context = createStrictMock(EvaluationContext.class);
	}
	
	@Test
	public void testCombineWithNoDecisions()
	{
		assertEquals(Decision.DENY, algorithm.combine(decisions, context));
	}
	
	@Test
	public void testCombineWithPermit() throws EvaluationException
	{
		DecisionRule r1 = createStrictMock(DecisionRule.class);
		decisions.add(r1);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluateIfApplicable(context)).andReturn(Decision.PERMIT);
		replay(r1);
		assertEquals(Decision.PERMIT, algorithm.combine(decisions, context));
		verify(r1);
	}
	
	@Test
	public void testCombineWithNotApplicable() throws EvaluationException
	{
		DecisionRule r1 = createStrictMock(DecisionRule.class);
		decisions.add(r1);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluateIfApplicable(context)).andReturn(Decision.NOT_APPLICABLE);
		replay(r1);
		assertEquals(Decision.DENY, algorithm.combine(decisions, context));
		verify(r1);
	}
	
	@Test
	public void testCombineWithIndeterminate() throws EvaluationException
	{
		DecisionRule r1 = createStrictMock(DecisionRule.class);
		decisions.add(r1);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluateIfApplicable(context)).andReturn(Decision.INDETERMINATE);
		replay(r1);
		assertEquals(Decision.DENY, algorithm.combine(decisions, context));
		verify(r1);
	}
	
	
	@Test
	public void testCombineWithIndeterminateD() throws EvaluationException
	{
		DecisionRule r1 = createStrictMock(DecisionRule.class);
		decisions.add(r1);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluateIfApplicable(context)).andReturn(Decision.INDETERMINATE_D);
		replay(r1);
		assertEquals(Decision.DENY, algorithm.combine(decisions, context));
		verify(r1);
	}
	
	@Test
	public void testCombineWithIndeterminateP() throws EvaluationException
	{
		DecisionRule r1 = createStrictMock(DecisionRule.class);
		decisions.add(r1);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluateIfApplicable(context)).andReturn(Decision.INDETERMINATE_P);
		replay(r1);
		assertEquals(Decision.DENY, algorithm.combine(decisions, context));
		verify(r1);
	}
	
	@Test
	public void testCombineWithIndeterminateDP() throws EvaluationException
	{
		DecisionRule r1 = createStrictMock(DecisionRule.class);
		decisions.add(r1);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluateIfApplicable(context)).andReturn(Decision.INDETERMINATE_DP);
		replay(r1);
		assertEquals(Decision.DENY, algorithm.combine(decisions, context));
		verify(r1);
	}
	
	@Test
	public void testCombineWithDenyAndPermit() throws EvaluationException
	{
		DecisionRule r1 = createStrictMock(DecisionRule.class);
		DecisionRule r2 = createStrictMock(DecisionRule.class);
		decisions.add(r1);
		decisions.add(r2);
		expect(r1.createContext(context)).andReturn(context);
		expect(r1.evaluateIfApplicable(context)).andReturn(Decision.DENY);
		expect(r2.createContext(context)).andReturn(context);
		expect(r2.evaluateIfApplicable(context)).andReturn(Decision.PERMIT);
		replay(r1, r2);
		assertEquals(Decision.PERMIT, algorithm.combine(decisions, context));
		verify(r1, r2);
	}
}
