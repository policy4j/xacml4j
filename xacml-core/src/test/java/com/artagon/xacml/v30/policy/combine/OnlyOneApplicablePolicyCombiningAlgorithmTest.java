package com.artagon.xacml.v30.policy.combine;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.CompositeDecisionRule;
import com.artagon.xacml.v30.Decision;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.EvaluationException;
import com.artagon.xacml.v30.MatchResult;

public class OnlyOneApplicablePolicyCombiningAlgorithmTest
{
	private OnlyOneApplicablePolicyCombingingAlgorithm c;
	private List<CompositeDecisionRule> d;
	private EvaluationContext context;
	
	@Before
	public void init(){
		this.c = new OnlyOneApplicablePolicyCombingingAlgorithm();
		this.d = new LinkedList<CompositeDecisionRule>();
		this.context = createStrictMock(EvaluationContext.class);
	}
	
	@Test
	public void testDecisionIsNoMatchContinueEvaluation() throws EvaluationException
	{
		CompositeDecisionRule d1 = createStrictMock(CompositeDecisionRule.class);
		CompositeDecisionRule d2 = createStrictMock(CompositeDecisionRule.class);
		EvaluationContext c1 = createStrictMock(EvaluationContext.class);
		EvaluationContext c2 = createStrictMock(EvaluationContext.class);
		d.add(d1);
		d.add(d2);
		expect(d1.createContext(context)).andReturn(c1);
		expect(d1.isApplicable(c1)).andReturn(MatchResult.NOMATCH);
		expect(d2.createContext(context)).andReturn(c2);
		expect(d2.isApplicable(c2)).andReturn(MatchResult.NOMATCH);
		replay(d1, d2, context, c1, c2);
		assertEquals(Decision.NOT_APPLICABLE, c.combine(d, context));
		verify(d1, d2, context, c1, c2);
	}
	
	@Test
	public void testDecisionIndeterminateStopsEvaluation() throws EvaluationException
	{
		CompositeDecisionRule d1 = createStrictMock(CompositeDecisionRule.class);
		CompositeDecisionRule d2 = createStrictMock(CompositeDecisionRule.class);
		EvaluationContext c1 = createStrictMock(EvaluationContext.class);
		EvaluationContext c2 = createStrictMock(EvaluationContext.class);
		d.add(d1);
		d.add(d2);
		expect(d1.createContext(context)).andReturn(c1);
		expect(d1.isApplicable(c1)).andReturn(MatchResult.INDETERMINATE);
		replay(d1, d2, context, c1, c2);
		assertEquals(Decision.INDETERMINATE, c.combine(d, context));
		verify(d1, d2, context, c1, c2);
	}
	
	@Test
	public void testMoreThanOneIsApplicable() throws EvaluationException
	{
		CompositeDecisionRule d1 = createStrictMock(CompositeDecisionRule.class);
		CompositeDecisionRule d2 = createStrictMock(CompositeDecisionRule.class);
		EvaluationContext c1 = createStrictMock(EvaluationContext.class);
		EvaluationContext c2 = createStrictMock(EvaluationContext.class);
		d.add(d1);
		d.add(d2);
		expect(d1.createContext(context)).andReturn(c1);
		expect(d1.isApplicable(c1)).andReturn(MatchResult.MATCH);
		expect(d2.createContext(context)).andReturn(c2);
		expect(d2.isApplicable(c2)).andReturn(MatchResult.MATCH);
		replay(d1, d2, context, c1, c2);
		assertEquals(Decision.INDETERMINATE, c.combine(d, context));
		verify(d1, d2, context, c1, c2);
	}
	
	@Test
	public void testOnlyOneIsApplicableAndDecisionIsPermit() throws EvaluationException
	{
		CompositeDecisionRule d1 = createStrictMock(CompositeDecisionRule.class);
		CompositeDecisionRule d2 = createStrictMock(CompositeDecisionRule.class);
		EvaluationContext c1 = createStrictMock(EvaluationContext.class);
		EvaluationContext c2 = createStrictMock(EvaluationContext.class);
		d.add(d1);
		d.add(d2);
		expect(d1.createContext(context)).andReturn(c1);
		expect(d1.isApplicable(c1)).andReturn(MatchResult.MATCH);
		expect(d2.createContext(context)).andReturn(c2);
		expect(d2.isApplicable(c2)).andReturn(MatchResult.NOMATCH);
		expect(d1.evaluate(c1)).andReturn(Decision.PERMIT);
		replay(d1, d2, context, c1, c2);
		assertEquals(Decision.PERMIT, c.combine(d, context));
		verify(d1, d2, context, c2, c2);
	}
	
	@Test
	public void testOnlyOneIsApplicableAndDecisionIsDeny() throws EvaluationException
	{
		CompositeDecisionRule d1 = createStrictMock(CompositeDecisionRule.class);
		CompositeDecisionRule d2 = createStrictMock(CompositeDecisionRule.class);
		EvaluationContext c1 = createStrictMock(EvaluationContext.class);
		EvaluationContext c2 = createStrictMock(EvaluationContext.class);
		d.add(d1);
		d.add(d2);
		expect(d1.createContext(context)).andReturn(c1);
		expect(d1.isApplicable(c1)).andReturn(MatchResult.MATCH);
		expect(d2.createContext(context)).andReturn(c2);
		expect(d2.isApplicable(c2)).andReturn(MatchResult.NOMATCH);
		expect(d1.evaluate(c1)).andReturn(Decision.DENY);
		replay(d1, d2, context, c1, c2);
		assertEquals(Decision.DENY, c.combine(d, context));
		verify(d1, d2, context, c1, c2);
	}
	
	@Test
	public void testOnlyOneIsApplicableAndDecisionIsIndeterminate() throws EvaluationException
	{
		CompositeDecisionRule d1 = createStrictMock(CompositeDecisionRule.class);
		CompositeDecisionRule d2 = createStrictMock(CompositeDecisionRule.class);
		EvaluationContext c1 = createStrictMock(EvaluationContext.class);
		EvaluationContext c2 = createStrictMock(EvaluationContext.class);
		d.add(d1);
		d.add(d2);
		expect(d1.createContext(context)).andReturn(c1);
		expect(d1.isApplicable(c1)).andReturn(MatchResult.MATCH);
		expect(d2.createContext(context)).andReturn(c2);
		expect(d2.isApplicable(c2)).andReturn(MatchResult.NOMATCH);
		expect(d1.evaluate(c1)).andReturn(Decision.INDETERMINATE);
		replay(d1, d2, context, c1, c2);
		assertEquals(Decision.INDETERMINATE, c.combine(d, context));
		verify(d1, d2, context, c1, c2);
	}
}
