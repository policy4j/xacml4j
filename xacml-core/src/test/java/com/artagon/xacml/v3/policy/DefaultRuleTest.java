package com.artagon.xacml.v3.policy;

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
	private Rule ruleDenyNoTarget;
	private Rule rulePermitNoTarget;
	
	private Condition condition;
	private Target target;
	
	private EvaluationContext context;
	
	@Before
	public void init()
	{
		this.context = createStrictMock(EvaluationContext.class);
		this.condition = createStrictMock(Condition.class);
		this.target = createStrictMock(Target.class);
			
		this.obligationExpressions = new LinkedList<ObligationExpression>();
		this.adviceExpressions = new LinkedList<AdviceExpression>();
		
		this.denyObligationExp = createMock(ObligationExpression.class);
		this.permitObligationExp = createMock(ObligationExpression.class);
		obligationExpressions.add(denyObligationExp);
		obligationExpressions.add(permitObligationExp);
		
		this.denyAdviceExp = createMock(AdviceExpression.class);
		this.permitAdviceExp = createMock(AdviceExpression.class);
		adviceExpressions.add(denyAdviceExp);
		adviceExpressions.add(permitAdviceExp);
		
		this.rulePermit = new DefaultRule("testPermitRule", target, condition, Effect.PERMIT, adviceExpressions, obligationExpressions);
		this.ruleDeny = new DefaultRule("testDenyRule", target, condition, Effect.DENY, adviceExpressions, obligationExpressions);
		
		this.rulePermitNoTarget = new DefaultRule("testPermitRuleNoTarget", null, condition, Effect.PERMIT, adviceExpressions, obligationExpressions);
		this.ruleDenyNoTarget = new DefaultRule("testDenyRuleNoTarget", null, condition, Effect.DENY, adviceExpressions, obligationExpressions);
	}
	
	@Test
	public void testRuleApplicabilityWithNoTarget() throws EvaluationException
	{
		Rule r = new DefaultRule("test", null, null, Effect.PERMIT, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		
		replay(context, permitAdviceExp, permitObligationExp);
		
		assertEquals(MatchResult.MATCH, r.isApplicable(ruleContext));
		
		verify(context, permitAdviceExp, permitObligationExp);
	}
	
	@Test
	public void testRuleWithNoTargetNoConditionEffectDeny() throws EvaluationException
	{
		Rule r = new DefaultRule("test", null, null, Effect.DENY, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		
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
		
		replay(context, denyAdviceExp, denyObligationExp);
		
		assertEquals(MatchResult.MATCH, r.isApplicable(ruleContext));
		assertEquals(Decision.DENY, r.evaluate(ruleContext));
		
		verify(context, denyAdviceExp, denyObligationExp);
	}
	
	@Test
	public void testRuleWithNoTargetConditionTrueEffectPermit() throws EvaluationException
	{
		EvaluationContext ruleContext = rulePermitNoTarget.createContext(context);
		
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
		
		replay(condition, context, permitAdviceExp, permitObligationExp);
		
		assertEquals(MatchResult.MATCH, rulePermitNoTarget.isApplicable(ruleContext));
		assertEquals(Decision.PERMIT, rulePermitNoTarget.evaluate(ruleContext));

		verify(condition, context, permitAdviceExp, permitObligationExp);
	}
	
	@Test
	public void testRuleWithNoTargetConditionTrueEffectDeny() throws EvaluationException
	{
		EvaluationContext ruleContext = ruleDenyNoTarget.createContext(context);
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
		
		replay(condition, context, denyAdviceExp, denyObligationExp);
	
		assertEquals(MatchResult.MATCH, ruleDenyNoTarget.isApplicable(ruleContext));
		assertEquals(Decision.DENY, ruleDenyNoTarget.evaluate(ruleContext));
		
		verify(condition, context, denyAdviceExp, denyObligationExp);
	}
	
	@Test
	public void testRuleWithNoTargetConditionFalseEffectPermit() throws EvaluationException
	{
		EvaluationContext ruleContext = rulePermitNoTarget.createContext(context);
		
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.FALSE);	
		replay(condition, context, permitAdviceExp, permitObligationExp);
		
		assertEquals(MatchResult.MATCH, rulePermitNoTarget.isApplicable(ruleContext));
		assertEquals(Decision.NOT_APPLICABLE, rulePermitNoTarget.evaluate(ruleContext));
		
		verify(condition, context, permitAdviceExp, permitObligationExp);
		
	}
	
	@Test
	public void testRuleWithNoTargetConditionFalseEffectDeny()
	{
		EvaluationContext ruleContext = ruleDenyNoTarget.createContext(context);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.FALSE);
		replay(condition, context, denyAdviceExp, denyObligationExp);
		assertEquals(MatchResult.MATCH, ruleDenyNoTarget.isApplicable(ruleContext));
		assertEquals(Decision.NOT_APPLICABLE, ruleDenyNoTarget.evaluate(ruleContext));
		verify(condition, context, denyAdviceExp, denyObligationExp);
	}
	
	@Test
	public void testRuleWithNoTargetConditionIndeterminateEffectPermit()
	{
		EvaluationContext ruleContext = rulePermitNoTarget.createContext(context);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.INDETERMINATE);
		replay(condition, context, permitAdviceExp, permitObligationExp);
		assertEquals(MatchResult.MATCH, rulePermitNoTarget.isApplicable(ruleContext));
		assertEquals(Decision.INDETERMINATE_P, rulePermitNoTarget.evaluate(ruleContext));
		verify(condition, context, permitAdviceExp, permitObligationExp);
	}
	
	@Test
	public void testRuleWithNoTargetConditionIndeterminateEffectDeny()
	{
		EvaluationContext ruleContext = ruleDenyNoTarget.createContext(context);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.INDETERMINATE);
		replay(condition, context, denyAdviceExp, denyObligationExp);
		assertEquals(MatchResult.MATCH, ruleDenyNoTarget.isApplicable(ruleContext));
		assertEquals(Decision.INDETERMINATE_D, ruleDenyNoTarget.evaluate(ruleContext));
		verify(condition, context, denyAdviceExp, denyObligationExp);
	}
	
	@Test
	public void testRuleWithTargetNoMatchConditionTrueEffectPermit() throws EvaluationException
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.NOMATCH);
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
		
		assertEquals(MatchResult.NOMATCH, rulePermit.isApplicable(ruleContext));
		assertEquals(Decision.PERMIT, rulePermit.evaluate(ruleContext));
		
		verify(target, condition, context, permitAdviceExp, permitObligationExp);
	}
	
	@Test
	public void testRuleWithTargetNoMatchConditionTrueEffectDeny() throws EvaluationException
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.NOMATCH);
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
	
		assertEquals(MatchResult.NOMATCH, ruleDeny.isApplicable(ruleContext));
		assertEquals(Decision.DENY, ruleDeny.evaluate(ruleContext));
		
		verify(target, condition, context, denyAdviceExp, denyObligationExp);
	}
	
	@Test
	public void testRuleWithTargetNoMatchConditionFalseEfectPermit()
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.NOMATCH);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.FALSE);
			
		replay(target, condition, context, permitAdviceExp, permitObligationExp);
		
		assertEquals(MatchResult.NOMATCH, rulePermit.isApplicable(ruleContext));
		assertEquals(Decision.NOT_APPLICABLE, rulePermit.evaluate(ruleContext));

		verify(target, condition, context, permitAdviceExp, permitObligationExp);
	}
	
	@Test
	public void testRuleWithTargetNoMatchConditionFalseEffectDeny()
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.NOMATCH);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.FALSE);
		
		replay(target, condition, context, denyAdviceExp, denyObligationExp);
		
		assertEquals(MatchResult.NOMATCH, ruleDeny.isApplicable(ruleContext));
		assertEquals(Decision.NOT_APPLICABLE, ruleDeny.evaluate(ruleContext));
		
		verify(target, condition, context, denyAdviceExp, denyObligationExp);
	}
	
	@Test
	public void testRuleWithTargetNoMatchConditionIndeterminateEffectPermit()
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		
		expect(target.match(ruleContext)).andReturn(MatchResult.NOMATCH);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.INDETERMINATE);
		
		replay(target, condition, context, permitAdviceExp, permitObligationExp);
		
		assertEquals(MatchResult.NOMATCH, rulePermit.isApplicable(ruleContext));
		assertEquals(Decision.INDETERMINATE_P, rulePermit.evaluate(ruleContext));
		
		verify(target, condition, context, permitAdviceExp, permitObligationExp);

	}
	
	@Test
	public void testRuleWithTargetNoMatchConditionIndeterminateEffectDeny()
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		
		expect(target.match(ruleContext)).andReturn(MatchResult.NOMATCH);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.INDETERMINATE);
		
		replay(target, condition, context, denyAdviceExp, denyObligationExp);
		
		assertEquals(MatchResult.NOMATCH, ruleDeny.isApplicable(ruleContext));
		assertEquals(Decision.INDETERMINATE_D, ruleDeny.evaluate(ruleContext));
		
		verify(target, condition, context, denyAdviceExp, denyObligationExp);
		
	}
	
	@Test
	public void testRuleWithTargetMatchConditionTrueEffectPermit()
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.MATCH);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.TRUE);
		replay(target, condition);
		
		assertEquals(MatchResult.MATCH, rulePermit.isApplicable(ruleContext));
		assertEquals(Decision.PERMIT, rulePermit.evaluate(ruleContext));
		
		verify(target, condition);
	}
	
	@Test
	public void testRuleWithTargetMatchConditionFalseEffectPermit() throws EvaluationException
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.MATCH);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.FALSE);
		
		replay(target, condition, context, permitAdviceExp, permitObligationExp);
		
		assertEquals(MatchResult.MATCH, rulePermit.isApplicable(ruleContext));
		assertEquals(Decision.NOT_APPLICABLE, rulePermit.evaluate(ruleContext));
		
		verify(target, condition, context, permitAdviceExp, permitObligationExp);
	}
	
	@Test
	public void testRuleWithTargetMatchConditionIndeterminateEffectPermit()
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.MATCH);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.INDETERMINATE);
		
		replay(target, condition, context, permitAdviceExp, permitObligationExp);
		
		assertEquals(MatchResult.MATCH, rulePermit.isApplicable(ruleContext));
		assertEquals(Decision.INDETERMINATE_P, rulePermit.evaluate(ruleContext));
		
		verify(target, condition, context, permitAdviceExp, permitObligationExp);
	}
	
	@Test
	public void testRuleWithTargetMatchConditionTrueEffectDeny() throws EvaluationException
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.MATCH);
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
		
		assertEquals(MatchResult.MATCH, ruleDeny.isApplicable(ruleContext));
		assertEquals(Decision.DENY, ruleDeny.evaluate(ruleContext));
		
		verify(target, condition, context, denyAdviceExp, denyObligationExp);
	}
	
	@Test
	public void testRuleWithTargetIndeterminateConditionTrueEffectPermit()
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.INDETERMINATE);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.TRUE);
		replay(target, condition);
		assertEquals(MatchResult.INDETERMINATE, rulePermit.isApplicable(ruleContext));
		assertEquals(Decision.PERMIT, rulePermit.evaluate(ruleContext));
		assertEquals(1, context.getAdvices().size());
		assertEquals(1, context.getObligations().size());
		verify(target, condition);
	}
	
	@Test
	public void testRuleWithTargetIndeterminateConditionTrueEffectDeny()
	{	
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.INDETERMINATE);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.TRUE);
		replay(target, condition);
		
		assertEquals(MatchResult.INDETERMINATE, ruleDeny.isApplicable(ruleContext));
		assertEquals(Decision.DENY, ruleDeny.evaluate(ruleContext));
		assertEquals(1, ruleContext.getAdvices().size());
		assertEquals(1, ruleContext.getObligations().size());
		verify(condition, target);
	}
	
	@Test
	public void testRuleEvaluateIfApplicableWithTargetIndeterminateConditionTrueEffectDeny()
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.INDETERMINATE);
		replay(target, condition);
		assertEquals(Decision.INDETERMINATE, ruleDeny.evaluateIfApplicable(ruleContext));
		assertEquals(0, ruleContext.getAdvices().size());
		assertEquals(0, ruleContext.getObligations().size());
		verify(condition, target);
	}
	
	@Test
	public void testRuleWithTargetIndeterminateConditionFalseEffectPermit()
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.INDETERMINATE);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.FALSE);
		replay(target, condition);
		assertEquals(MatchResult.INDETERMINATE, rulePermit.isApplicable(ruleContext));
		assertEquals(Decision.NOT_APPLICABLE, rulePermit.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithTargetIndeterminateConditionFalseEffectDeny()
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.INDETERMINATE);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.FALSE);
		replay(target, condition);
		assertEquals(MatchResult.INDETERMINATE, ruleDeny.isApplicable(ruleContext));
		assertEquals(Decision.NOT_APPLICABLE, ruleDeny.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithTargetIndeterminateConditionIndeterminateEffectPermit()
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.INDETERMINATE);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.INDETERMINATE);
		replay(target, condition);
		assertEquals(MatchResult.INDETERMINATE, rulePermit.isApplicable(ruleContext));
		assertEquals(Decision.INDETERMINATE_P, rulePermit.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithTargetIndeterminateConditionIndeterminateEffectDeny()
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.INDETERMINATE);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.INDETERMINATE);
		replay(target, condition);
		assertEquals(MatchResult.INDETERMINATE, ruleDeny.isApplicable(ruleContext));
		assertEquals(Decision.INDETERMINATE_D, ruleDeny.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
	}
	
	@Test
	public void testEvaluateIfApplicableTargetIsNoMatch()
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.NOMATCH);
		replay(target, condition);
		assertEquals(Decision.NOT_APPLICABLE, rulePermit.evaluateIfApplicable(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
	}
	
	@Test
	public void testEvaluateIfApplicableTargetIsMatchConditionTrue()
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.MATCH);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.TRUE);
		replay(target, condition);
		assertEquals(Decision.PERMIT, rulePermit.evaluateIfApplicable(ruleContext));
		assertEquals(1, context.getAdvices().size());
		assertEquals(1, context.getObligations().size());
	}
	
}
