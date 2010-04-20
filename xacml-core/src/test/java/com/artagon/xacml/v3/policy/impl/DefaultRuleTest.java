package com.artagon.xacml.v3.policy.impl;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.Advice;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.Obligation;
import com.artagon.xacml.v3.policy.AdviceExpression;
import com.artagon.xacml.v3.policy.Condition;
import com.artagon.xacml.v3.policy.ConditionResult;
import com.artagon.xacml.v3.policy.DecisionRule;
import com.artagon.xacml.v3.policy.Effect;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.MatchResult;
import com.artagon.xacml.v3.policy.ObligationExpression;
import com.artagon.xacml.v3.policy.Policy;
import com.artagon.xacml.v3.policy.Rule;
import com.artagon.xacml.v3.policy.Target;

public class DefaultRuleTest
{
	
	private Collection<ObligationExpression> obligationExpressions;
	private Collection<AdviceExpression> adviceExpressions;
	
	private ObligationExpression denyObligationExp;
	
	private ObligationExpression permitObligationExp;
	
	private AdviceExpression denyAdviceExp;
	private  AdviceExpression permitAdviceExp;
	
	private Rule rulePermit;
	private Rule ruleDeny;
	
	private Policy currentPolicy;
	private Condition condition;
	private Target target;
	
	private EvaluationContext context;
	
	@Before
	public void init()
	{
		this.context = createStrictMock(EvaluationContext.class);
		this.condition = createStrictMock(Condition.class);
		this.target = createStrictMock(Target.class);
		this.currentPolicy = createStrictMock(Policy.class);
			
		this.obligationExpressions = new LinkedList<ObligationExpression>();
		this.adviceExpressions = new LinkedList<AdviceExpression>();
		
		this.denyObligationExp = createStrictMock(ObligationExpression.class);
		this.permitObligationExp = createStrictMock(ObligationExpression.class);
		obligationExpressions.add(denyObligationExp);
		obligationExpressions.add(permitObligationExp);
		
		this.denyAdviceExp = createStrictMock(AdviceExpression.class);
		this.permitAdviceExp = createStrictMock(AdviceExpression.class);
		
		adviceExpressions.add(denyAdviceExp);
		adviceExpressions.add(permitAdviceExp);
		
		this.rulePermit = new DefaultRule("testPermitRule", target, condition, Effect.PERMIT, adviceExpressions, obligationExpressions);
		this.ruleDeny = new DefaultRule("testDenyRule", target, condition, Effect.DENY, adviceExpressions, obligationExpressions);
	}
	
	@Test
	public void testDenyRuleIsApplicableWithNoTarget() throws EvaluationException
	{
		DecisionRule ruleDenyNoTarget = new DefaultRule("testDenyRuleNoTarget", null, condition, Effect.DENY, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = ruleDenyNoTarget.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		replay(context, condition, denyAdviceExp, denyObligationExp);
		assertEquals(MatchResult.MATCH, ruleDenyNoTarget.isApplicable(ruleContext));
		verify(context, denyAdviceExp, denyAdviceExp);
	}
	
	@Test
	public void testPermitRuleIsApplicableWithNoTarget() throws EvaluationException
	{
		DecisionRule rulePermitNoTarget = new DefaultRule("testPermitRuleNoTarget", null, condition, Effect.PERMIT, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = rulePermitNoTarget.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		replay(context, condition, denyAdviceExp, denyObligationExp);
		assertEquals(MatchResult.MATCH, rulePermitNoTarget.isApplicable(ruleContext));
		verify(context, denyAdviceExp, denyAdviceExp);
	}
	
	@Test
	public void testDenyRuleApplicabilityWithTargetMatch() throws EvaluationException
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.MATCH);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		replay(target, condition, context, denyAdviceExp, denyObligationExp);
		assertEquals(MatchResult.MATCH, ruleDeny.isApplicable(ruleContext));
		verify(target, condition, context, denyAdviceExp, denyObligationExp);
	}
	
	@Test
	public void testPermitRuleIsApplicableWithTargetIndeterminate() throws EvaluationException
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(target.match(ruleContext)).andReturn(MatchResult.INDETERMINATE);
		replay(target, condition, context, permitAdviceExp, permitObligationExp);
		assertEquals(MatchResult.INDETERMINATE, rulePermit.isApplicable(ruleContext));
		verify(target, condition, context, permitAdviceExp, permitObligationExp);
	}
	
	@Test
	public void testDenyRuleApplicabilityWithTargetIndeterminate() throws EvaluationException
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(target.match(ruleContext)).andReturn(MatchResult.INDETERMINATE);
		replay(target, condition, context, denyAdviceExp, denyObligationExp);
		assertEquals(MatchResult.INDETERMINATE, ruleDeny.isApplicable(ruleContext));
		verify(target, condition, context, denyAdviceExp, denyObligationExp);
	}
	
	@Test
	public void testPermitRuleIsApplicableWithTargetMatch() throws EvaluationException
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(target.match(ruleContext)).andReturn(MatchResult.MATCH);
		replay(target, condition, context, permitAdviceExp, permitObligationExp);
		assertEquals(MatchResult.MATCH, rulePermit.isApplicable(ruleContext));
		verify(target, condition, context, permitAdviceExp, permitObligationExp);
	}
	
	@Test
	public void testDenyRuleIsApplicableWithTargetNoMatch() throws EvaluationException
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(target.match(ruleContext)).andReturn(MatchResult.NOMATCH);
		replay(target, condition, context, denyAdviceExp, denyObligationExp);
		assertEquals(MatchResult.NOMATCH, ruleDeny.isApplicable(ruleContext));
		verify(target, condition, context, denyAdviceExp, denyObligationExp);		
	}
	
	@Test
	public void testDenyRuleConditionTrue() throws EvaluationException
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.TRUE);
		
		Advice advice = createMock(Advice.class);
		Obligation obligation = createMock(Obligation.class);
		
		expect(denyAdviceExp.isApplicable(Decision.DENY)).andReturn(true);
		expect(denyAdviceExp.getId()).andReturn("denyAdviceExp").times(0, 1);
		expect(denyAdviceExp.evaluate(ruleContext)).andReturn(advice);
		
		expect(denyObligationExp.isApplicable(Decision.DENY)).andReturn(true);
		expect(denyObligationExp.getId()).andReturn("denyObligationExp").times(0, 1);
		expect(denyObligationExp.evaluate(ruleContext)).andReturn(obligation);
		context.addAdvices(Collections.singletonList(advice));
		context.addObligations(Collections.singletonList(obligation));
		
		replay(target, condition, context, denyAdviceExp, denyObligationExp);
		
		assertEquals(Decision.DENY, ruleDeny.evaluate(ruleContext));
		verify(target, condition, context, denyAdviceExp, denyObligationExp);
	}
	
	@Test
	public void testDenyRuleConditionFalse() throws EvaluationException
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.FALSE);	
		replay(context, condition, target, denyAdviceExp, denyObligationExp);
		
		assertEquals(Decision.NOT_APPLICABLE, ruleDeny.evaluate(ruleContext));
		verify(context, condition, target, denyAdviceExp, denyObligationExp);	
	}
	
	@Test
	public void testDenyRuleConditionIndeterminate() throws EvaluationException
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.INDETERMINATE);	
		replay(target, condition, context, denyAdviceExp, denyObligationExp);
		assertEquals(Decision.INDETERMINATE_D, ruleDeny.evaluate(ruleContext));
		verify(target, condition, context, denyAdviceExp, denyObligationExp);	
	}
	
		
	@Test
	public void testPermitRuleConditionTrue() throws EvaluationException
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.TRUE);
		
		Advice advice = createMock(Advice.class);
		Obligation obligation = createMock(Obligation.class);
		
		expect(permitAdviceExp.isApplicable(Decision.PERMIT)).andReturn(true);
		expect(permitAdviceExp.getId()).andReturn("permitAdviceExp").times(0, 1);
		expect(permitAdviceExp.evaluate(ruleContext)).andReturn(advice);
		expect(permitObligationExp.isApplicable(Decision.PERMIT)).andReturn(true);
		expect(permitObligationExp.getId()).andReturn("permitObligationExp").times(0, 1);
		expect(permitObligationExp.evaluate(ruleContext)).andReturn(obligation);
		
		context.addAdvices(Collections.singletonList(advice));
		context.addObligations(Collections.singletonList(obligation));
		replay(target, condition, context, permitAdviceExp, permitObligationExp);
		assertEquals(Decision.PERMIT, rulePermit.evaluate(ruleContext));
		verify(target, condition, context, permitAdviceExp, permitObligationExp);
	}
	
	@Test
	public void testPermitRuleConditionFalse() throws EvaluationException
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.FALSE);	
		replay(context, condition, target, denyAdviceExp, denyObligationExp);
		
		assertEquals(Decision.NOT_APPLICABLE, ruleDeny.evaluate(ruleContext));
		verify(context, condition, target, denyAdviceExp, denyObligationExp);	
	}
	
	@Test
	public void testPermitRuleConditionIndeterminate() throws EvaluationException
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.INDETERMINATE);	
		replay(target, condition, context, permitAdviceExp, permitObligationExp);
		assertEquals(Decision.INDETERMINATE_D, ruleDeny.evaluate(ruleContext));
		verify(target, condition, context, permitAdviceExp, permitObligationExp);	
	}

	@Test
	public void testDenyRuleEvaluateIfApplicableWithTargetIndeterminate() 
		throws EvaluationException
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(target.match(ruleContext)).andReturn(MatchResult.INDETERMINATE);
		replay(target, condition, currentPolicy);
		assertEquals(Decision.INDETERMINATE, ruleDeny.evaluateIfApplicable(ruleContext));
		verify(condition, target, currentPolicy);
	}
	
	@Test
	public void testDenyRuleEvaluateIfApplicableWithTargetNoMatch()
		throws EvaluationException
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(target.match(ruleContext)).andReturn(MatchResult.NOMATCH);
		replay(target, condition, currentPolicy);
		assertEquals(Decision.NOT_APPLICABLE, ruleDeny.evaluateIfApplicable(ruleContext));
		verify(condition, target, currentPolicy);
	}
	
	@Test
	public void testDenyRuleEvaluateIfApplicableWithTargetMatchConditionTrue() throws EvaluationException
	{
		
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.TRUE);
		
		Advice advice = createMock(Advice.class);
		Obligation obligation = createMock(Obligation.class);
		
		expect(denyAdviceExp.isApplicable(Decision.DENY)).andReturn(true);
		expect(denyAdviceExp.getId()).andReturn("denyAdviceExp").times(0, 1);
		expect(denyAdviceExp.evaluate(ruleContext)).andReturn(advice);
		
		expect(denyObligationExp.isApplicable(Decision.DENY)).andReturn(true);
		expect(denyObligationExp.getId()).andReturn("denyObligationExp").times(0, 1);
		expect(denyObligationExp.evaluate(ruleContext)).andReturn(obligation);
		context.addAdvices(Collections.singletonList(advice));
		context.addObligations(Collections.singletonList(obligation));
		
		replay(target, condition, context, denyAdviceExp, denyObligationExp);
		
		assertEquals(Decision.DENY, ruleDeny.evaluate(ruleContext));
		verify(target, condition, context, denyAdviceExp, denyObligationExp);
	}
	
	
	@Test
	public void testPermitRuleEvaluateIfApplicableWithTargetIndeterminate() 
		throws EvaluationException
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(target.match(ruleContext)).andReturn(MatchResult.INDETERMINATE);
		replay(target, condition);
		assertEquals(Decision.INDETERMINATE, rulePermit.evaluateIfApplicable(ruleContext));
		verify(condition, target);
	}
	
	@Test
	public void testPermitRuleEvaluateIfApplicableWithTargetNoMatch() 
		throws EvaluationException
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(target.match(ruleContext)).andReturn(MatchResult.NOMATCH);
		replay(target, condition);
		assertEquals(Decision.NOT_APPLICABLE, rulePermit.evaluateIfApplicable(ruleContext));
		verify(condition, target);
	}	
	
	@Test
	public void testPermitRuleEvaluateIfApplicableWithTargetMatchConditionTrue() throws EvaluationException
	{
		
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.TRUE);
		
		Advice advice = createMock(Advice.class);
		Obligation obligation = createMock(Obligation.class);
		
		expect(permitAdviceExp.isApplicable(Decision.PERMIT)).andReturn(true);
		expect(permitAdviceExp.getId()).andReturn("permitAdviceExp").times(0, 1);
		expect(permitAdviceExp.evaluate(ruleContext)).andReturn(advice);
		
		expect(permitObligationExp.isApplicable(Decision.PERMIT)).andReturn(true);
		expect(permitObligationExp.getId()).andReturn("permitObligationExp").times(0, 1);
		expect(permitObligationExp.evaluate(ruleContext)).andReturn(obligation);
		context.addAdvices(Collections.singletonList(advice));
		context.addObligations(Collections.singletonList(obligation));
		
		replay(target, condition, context, permitAdviceExp, permitObligationExp);
		
		assertEquals(Decision.PERMIT, rulePermit.evaluate(ruleContext));
		verify(target, condition, context,  permitAdviceExp, permitObligationExp);
	}
}
