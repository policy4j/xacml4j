package com.artagon.xacml.v3.policy;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import com.artagon.xacml.v3.CategoryId;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.policy.type.DataTypes;

public class DefaultRuleTest extends XacmlPolicyTestCase
{
	
	private Collection<ObligationExpression> obligationExpressions;
	private Collection<AdviceExpression> adviceExpressions;
	
	private Rule rulePermit;
	private Rule ruleDeny;
	
	private Condition condition;
	private Target target;
	
	@Before
	public void init()
	{
		this.condition = createStrictMock(Condition.class);
		this.target = createStrictMock(Target.class);
			
		this.obligationExpressions = new LinkedList<ObligationExpression>();
		this.adviceExpressions = new LinkedList<AdviceExpression>();
		
		AttributeAssignmentExpression attrExpPermit = new AttributeAssignmentExpression(
				"testAttrId", DataTypes.STRING.create("PermitValue"), 
				CategoryId.SUBJECT_RECIPIENT, 
				null);
		AttributeAssignmentExpression attrExpDeny = new AttributeAssignmentExpression(
				"testAttrId", DataTypes.STRING.create("DenyValue"),
				CategoryId.SUBJECT_INTERMEDIARY, 
				null);
		
		adviceExpressions.add(new AdviceExpression("testAdvicePermit", Effect.PERMIT, Collections.singletonList(attrExpPermit)));
		adviceExpressions.add(new AdviceExpression("testAdviceDeny", Effect.DENY, Collections.singletonList(attrExpDeny)));
		obligationExpressions.add(new ObligationExpression("testObligationPermit", Effect.PERMIT, Collections.singletonList(attrExpPermit)));
		obligationExpressions.add(new ObligationExpression("testObligationDeny", Effect.DENY, Collections.singletonList(attrExpDeny)));
		
		this.rulePermit = new DefaultRule("testPermitRule", target, condition, Effect.PERMIT, adviceExpressions, obligationExpressions);
		this.ruleDeny = new DefaultRule("testDenyRule", target, condition, Effect.DENY, adviceExpressions, obligationExpressions);
	}
	
	@Test
	public void testRuleWithNoTargetNoConditionEffectPermit()
	{
		Rule r = new DefaultRule("test", null, null, Effect.PERMIT, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.MATCH, r.isApplicable(ruleContext));
		assertEquals(Decision.PERMIT, r.evaluate(ruleContext));
		assertEquals(1, context.getAdvices().size());
		assertEquals(1, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithNoTargetNoConditionEffectDeny()
	{
		Rule r = new DefaultRule("test", null, null, Effect.DENY, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.MATCH, r.isApplicable(ruleContext));
		assertEquals(Decision.DENY, r.evaluate(ruleContext));
		assertEquals(1, context.getAdvices().size());
		assertEquals(1, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithNoTargetConditionTrueEffectPermit()
	{
		
		Rule r = new DefaultRule("test", null, condition, Effect.PERMIT, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.TRUE);
		replay(condition);
		assertEquals(MatchResult.MATCH, r.isApplicable(ruleContext));
		assertEquals(Decision.PERMIT, r.evaluate(ruleContext));
		assertEquals(1, context.getAdvices().size());
		assertEquals(1, context.getObligations().size());
		verify(condition);
	}
	
	@Test
	public void testRuleWithNoTargetConditionTrueEffectDeny()
	{
		Rule r = new DefaultRule("test", null, condition, Effect.DENY, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.TRUE);
		replay(condition);
		assertEquals(Decision.DENY, r.evaluate(ruleContext));
		assertEquals(MatchResult.MATCH, r.isApplicable(ruleContext));
		assertEquals(1, context.getAdvices().size());
		assertEquals(1, context.getObligations().size());
		verify(condition);
	}
	
	@Test
	public void testRuleWithNoTargetConditionFalseEffectPermit()
	{
		Rule r = new DefaultRule("test", null, condition, Effect.PERMIT, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.FALSE);
		replay(condition);
		assertEquals(MatchResult.MATCH, r.isApplicable(ruleContext));
		assertEquals(Decision.NOT_APPLICABLE, r.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
		verify(condition);
	}
	
	@Test
	public void testRuleWithNoTargetConditionFalseEffectDeny()
	{
		Rule r = new DefaultRule("test", null, condition, Effect.DENY, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.FALSE);
		replay(condition);
		assertEquals(MatchResult.MATCH, r.isApplicable(ruleContext));
		assertEquals(Decision.NOT_APPLICABLE, r.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
		verify(condition);
	}
	
	@Test
	public void testRuleWithNoTargetConditionIndeterminateEffectPermit()
	{
		Rule r = new DefaultRule("test", null, condition, Effect.PERMIT, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.INDETERMINATE);
		replay(condition);
		assertEquals(MatchResult.MATCH, r.isApplicable(ruleContext));
		assertEquals(Decision.INDETERMINATE_P, r.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
		verify(condition);
	}
	
	@Test
	public void testRuleWithNoTargetConditionIndeterminateEffectDeny()
	{
		Rule r = new DefaultRule("test", null, condition, Effect.DENY, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.INDETERMINATE);
		replay(condition);
		assertEquals(MatchResult.MATCH, r.isApplicable(ruleContext));
		assertEquals(Decision.INDETERMINATE_D, r.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
		verify(condition);
	}
	
	@Test
	public void testRuleWithTargetNoMatchConditionTrueEffectPermit()
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.NOMATCH);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.TRUE);
		replay(target, condition);
		assertEquals(MatchResult.NOMATCH, rulePermit.isApplicable(ruleContext));
		assertEquals(Decision.PERMIT, rulePermit.evaluate(ruleContext));
		assertEquals(1, context.getAdvices().size());
		assertEquals(1, context.getObligations().size());
		verify(target, condition);
	}
	
	@Test
	public void testRuleWithTargetNoMatchConditionTrueEffectDeny()
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.NOMATCH);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.TRUE);
		replay(target, condition);
		assertEquals(MatchResult.NOMATCH, ruleDeny.isApplicable(ruleContext));
		assertEquals(Decision.DENY, ruleDeny.evaluate(ruleContext));
		assertEquals(1, context.getAdvices().size());
		assertEquals(1, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithTargetNoMatchConditionFalseEfectPermit()
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.NOMATCH);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.TRUE);
		replay(target, condition);
		assertEquals(MatchResult.NOMATCH, rulePermit.isApplicable(ruleContext));
		assertEquals(Decision.PERMIT, rulePermit.evaluate(ruleContext));
		assertEquals(1, context.getAdvices().size());
		assertEquals(1, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithTargetNoMatchConditionFalseEffectDeny()
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.NOMATCH);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.FALSE);
		replay(target, condition);
		assertEquals(MatchResult.NOMATCH, ruleDeny.isApplicable(ruleContext));
		assertEquals(Decision.NOT_APPLICABLE, ruleDeny.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithTargetNoMatchConditionIndeterminateEffectPermit()
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.NOMATCH);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.INDETERMINATE);
		replay(target, condition);
		assertEquals(MatchResult.NOMATCH, rulePermit.isApplicable(ruleContext));
		assertEquals(Decision.INDETERMINATE_P, rulePermit.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithTargetNoMatchConditionIndeterminateEffectDeny()
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.NOMATCH);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.INDETERMINATE);
		replay(target, condition);
		assertEquals(MatchResult.NOMATCH, ruleDeny.isApplicable(ruleContext));
		assertEquals(Decision.INDETERMINATE_D, ruleDeny.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
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
		assertEquals(1, context.getAdvices().size());
		assertEquals(1, context.getObligations().size());
		verify(target, condition);
	}
	
	@Test
	public void testRuleWithTargetMatchConditionFalseEffectPermit()
	{
		Rule r = new DefaultRule("test", new MockTarget(MatchResult.MATCH), condition, Effect.PERMIT, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.MATCH, r.isApplicable(ruleContext));
		assertEquals(Decision.NOT_APPLICABLE, r.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithTargetMatchConditionIndeterminateEffectPermit()
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.MATCH);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.INDETERMINATE);
		replay(target, condition);
		assertEquals(MatchResult.MATCH, rulePermit.isApplicable(ruleContext));
		assertEquals(Decision.INDETERMINATE_P, rulePermit.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
		verify(target, condition);
	}
	
	@Test
	public void testRuleWithTargetMatchConditionTrueEffectDeny()
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.MATCH);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.TRUE);
		replay(target, condition);
		assertEquals(MatchResult.MATCH, ruleDeny.isApplicable(ruleContext));
		assertEquals(Decision.DENY, ruleDeny.evaluate(ruleContext));
		assertEquals(1, context.getAdvices().size());
		assertEquals(1, context.getObligations().size());
		verify(target, condition);
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
		Rule r = new DefaultRule("test", new MockTarget(MatchResult.INDETERMINATE), condition, Effect.DENY, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.INDETERMINATE, r.isApplicable(ruleContext));
		assertEquals(Decision.INDETERMINATE_D, r.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithTargetIndeterminateConditionFalseEffectPermit()
	{
		Rule r = new DefaultRule("test", new MockTarget(MatchResult.INDETERMINATE), condition, Effect.PERMIT, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.INDETERMINATE, r.isApplicable(ruleContext));
		assertEquals(Decision.NOT_APPLICABLE, r.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithTargetIndeterminateConditionFalseEffectDeny()
	{
		Rule r = new DefaultRule("test", new MockTarget(MatchResult.INDETERMINATE), condition, Effect.DENY, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.INDETERMINATE, r.isApplicable(ruleContext));
		assertEquals(Decision.NOT_APPLICABLE, r.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithTargetIndeterminateConditionIndeterminateEffectPermit()
	{
		Rule r = new DefaultRule("test", new MockTarget(MatchResult.INDETERMINATE), condition, Effect.PERMIT, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.INDETERMINATE, r.isApplicable(ruleContext));
		assertEquals(Decision.INDETERMINATE_P, r.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
	}
	
	@Test
	public void testRuleWithTargetIndeterminateConditionIndeterminateEffectDeny()
	{
		Rule r = new DefaultRule("test", new MockTarget(MatchResult.INDETERMINATE), condition, Effect.DENY, adviceExpressions, obligationExpressions);
		EvaluationContext ruleContext = r.createContext(context);
		assertEquals(MatchResult.INDETERMINATE, r.isApplicable(ruleContext));
		assertEquals(Decision.INDETERMINATE_D, r.evaluate(ruleContext));
		assertEquals(0, context.getAdvices().size());
		assertEquals(0, context.getObligations().size());
	}
	
}
