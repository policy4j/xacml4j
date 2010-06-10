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

import com.artagon.xacml.v3.CompositeDecisionRule;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.MatchResult;

public class PolicyOnlyOneApplicableTest
{
	private PolicyOnlyOneApplicableCombingingAlgorithm c;
	private List<CompositeDecisionRule> d;
	private EvaluationContext context;
	
	@Before
	public void init(){
		this.c = new PolicyOnlyOneApplicableCombingingAlgorithm();
		this.d = new LinkedList<CompositeDecisionRule>();
		this.context = createStrictMock(EvaluationContext.class);
	}
	
	@Test
	public void testDecisionIsNoMatchContinueEvaluation() throws EvaluationException
	{
		CompositeDecisionRule d1 = createStrictMock(CompositeDecisionRule.class);
		CompositeDecisionRule d2 = createStrictMock(CompositeDecisionRule.class);
		d.add(d1);
		d.add(d2);
		expect(d1.createContext(context)).andReturn(context);
		expect(d1.isApplicable(context)).andReturn(MatchResult.NOMATCH);
		expect(d2.createContext(context)).andReturn(context);
		expect(d2.isApplicable(context)).andReturn(MatchResult.NOMATCH);
		replay(d1, d2);
		assertEquals(Decision.NOT_APPLICABLE, c.combine(d, context));
		verify(d1, d2);
	}
	
	@Test
	public void testDecisionIndeterminateStopsEvaluation() throws EvaluationException
	{
		CompositeDecisionRule d1 = createStrictMock(CompositeDecisionRule.class);
		CompositeDecisionRule d2 = createStrictMock(CompositeDecisionRule.class);
		d.add(d1);
		d.add(d2);
		expect(d1.createContext(context)).andReturn(context);
		expect(d1.isApplicable(context)).andReturn(MatchResult.INDETERMINATE);
		replay(d1, d2);
		assertEquals(Decision.INDETERMINATE, c.combine(d, context));
		verify(d1, d2);
	}
	
	@Test
	public void testMoreThanOneIsApplicable() throws EvaluationException
	{
		CompositeDecisionRule d1 = createStrictMock(CompositeDecisionRule.class);
		CompositeDecisionRule d2 = createStrictMock(CompositeDecisionRule.class);
		d.add(d1);
		d.add(d2);
		expect(d1.createContext(context)).andReturn(context);
		expect(d1.isApplicable(context)).andReturn(MatchResult.MATCH);
		expect(d2.createContext(context)).andReturn(context);
		expect(d2.isApplicable(context)).andReturn(MatchResult.MATCH);
		replay(d1, d2);
		assertEquals(Decision.INDETERMINATE, c.combine(d, context));
		verify(d1, d2);
	}
	
	@Test
	public void testOnlyOneIsApplicableAndDecisionIsPermit() throws EvaluationException
	{
		CompositeDecisionRule d1 = createStrictMock(CompositeDecisionRule.class);
		CompositeDecisionRule d2 = createStrictMock(CompositeDecisionRule.class);
		d.add(d1);
		d.add(d2);
		expect(d1.createContext(context)).andReturn(context);
		expect(d1.isApplicable(context)).andReturn(MatchResult.MATCH);
		expect(d2.createContext(context)).andReturn(context);
		expect(d2.isApplicable(context)).andReturn(MatchResult.NOMATCH);
		expect(d1.evaluate(context)).andReturn(Decision.PERMIT);
		replay(d1, d2);
		assertEquals(Decision.PERMIT, c.combine(d, context));
		verify(d1, d2);
	}
	
	@Test
	public void testOnlyOneIsApplicableAndDecisionIsDeny() throws EvaluationException
	{
		CompositeDecisionRule d1 = createStrictMock(CompositeDecisionRule.class);
		CompositeDecisionRule d2 = createStrictMock(CompositeDecisionRule.class);
		d.add(d1);
		d.add(d2);
		expect(d1.createContext(context)).andReturn(context);
		expect(d1.isApplicable(context)).andReturn(MatchResult.MATCH);
		expect(d2.createContext(context)).andReturn(context);
		expect(d2.isApplicable(context)).andReturn(MatchResult.NOMATCH);
		expect(d1.evaluate(context)).andReturn(Decision.DENY);
		replay(d1, d2);
		assertEquals(Decision.DENY, c.combine(d, context));
		verify(d1, d2);
	}
	
	@Test
	public void testOnlyOneIsApplicableAndDecisionIsIndeterminate() throws EvaluationException
	{
		CompositeDecisionRule d1 = createStrictMock(CompositeDecisionRule.class);
		CompositeDecisionRule d2 = createStrictMock(CompositeDecisionRule.class);
		d.add(d1);
		d.add(d2);
		expect(d1.createContext(context)).andReturn(context);
		expect(d1.isApplicable(context)).andReturn(MatchResult.MATCH);
		expect(d2.createContext(context)).andReturn(context);
		expect(d2.isApplicable(context)).andReturn(MatchResult.NOMATCH);
		expect(d1.evaluate(context)).andReturn(Decision.INDETERMINATE);
		replay(d1, d2);
		assertEquals(Decision.INDETERMINATE, c.combine(d, context));
		verify(d1, d2);
	}
}
