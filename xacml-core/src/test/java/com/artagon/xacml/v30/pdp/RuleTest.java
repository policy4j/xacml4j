package com.artagon.xacml.v30.pdp;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;

import org.easymock.Capture;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.pdp.Advice;
import com.artagon.xacml.v30.pdp.AdviceExpression;
import com.artagon.xacml.v30.pdp.Condition;
import com.artagon.xacml.v30.pdp.ConditionResult;
import com.artagon.xacml.v30.pdp.Decision;
import com.artagon.xacml.v30.pdp.DecisionRule;
import com.artagon.xacml.v30.pdp.Effect;
import com.artagon.xacml.v30.pdp.EvaluationContext;
import com.artagon.xacml.v30.pdp.EvaluationException;
import com.artagon.xacml.v30.pdp.Expression;
import com.artagon.xacml.v30.pdp.MatchResult;
import com.artagon.xacml.v30.pdp.Obligation;
import com.artagon.xacml.v30.pdp.ObligationExpression;
import com.artagon.xacml.v30.pdp.Policy;
import com.artagon.xacml.v30.pdp.Rule;
import com.artagon.xacml.v30.pdp.StatusCode;
import com.artagon.xacml.v30.pdp.Target;
import com.artagon.xacml.v30.types.StringType;

public class RuleTest
{
	private Rule rulePermit;
	private Rule ruleDeny;
	
	private Policy currentPolicy;
	private Condition condition;
	private Target target;
	
	private EvaluationContext context;
	
	private IMocksControl c;
	
	private Expression denyObligationAttributeExp;
	private Expression permitObligationAttributeExp;
	
	private Expression denyAdviceAttributeExp;
	private Expression permitAdviceAttributeExp;
	
	private Rule.Builder builder;
	
	@Before
	public void init()
	{
		this.c = createStrictControl();
		this.context = c.createMock(EvaluationContext.class);
		this.condition = c.createMock(Condition.class);
		this.target = c.createMock(Target.class);
		this.currentPolicy = c.createMock(Policy.class);
			
	
		this.permitAdviceAttributeExp = c.createMock(Expression.class);
		this.denyAdviceAttributeExp = c.createMock(Expression.class);
		
		this.denyObligationAttributeExp = c.createMock(Expression.class);
		this.permitObligationAttributeExp = c.createMock(Expression.class);
		
		this.builder = Rule
				.builder()
				.withTarget(target)
				.withCondition(condition)
				.withObligation(ObligationExpression
						.builder("denyObligation", Effect.DENY)
							.attributeAssigment("testId", denyObligationAttributeExp))
				.withObligation(ObligationExpression
						.builder("permitObligation", Effect.PERMIT)
						.attributeAssigment("testId", permitObligationAttributeExp))
				.withAdvice(AdviceExpression
						.builder("denyAdvice", Effect.DENY)
						.withAttributeAssigment("testId", denyAdviceAttributeExp))
				.withAdvice(AdviceExpression
					.builder("permitAdvice", Effect.PERMIT)
					.withAttributeAssigment("testId", permitAdviceAttributeExp));
		
		this.rulePermit = builder
				.withId("testPermitRule")
				.withEffect(Effect.PERMIT)
				.build();
		
		this.ruleDeny = builder
				.withId("testDenyRule")
				.withEffect(Effect.DENY)
				.build();
		
	}
	
	@Test
	public void testDenyRuleIsApplicableWithNoTarget() throws EvaluationException
	{
		DecisionRule ruleDenyNoTarget = builder.withoutTarget().withEffect(Effect.DENY).build();
		EvaluationContext ruleContext = ruleDenyNoTarget.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		c.replay();
		assertEquals(MatchResult.MATCH, ruleDenyNoTarget.isApplicable(ruleContext));
		c.verify();
	}
	
	@Test
	public void testPermitRuleIsApplicableWithNoTarget() throws EvaluationException
	{
		DecisionRule rulePermitNoTarget = builder.withoutTarget().withEffect(Effect.PERMIT).build();
		EvaluationContext ruleContext = rulePermitNoTarget.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		c.replay();
		assertEquals(MatchResult.MATCH, rulePermitNoTarget.isApplicable(ruleContext));
		c.verify();
	}
	
	@Test
	public void testDenyRuleApplicabilityWithTargetMatch() throws EvaluationException
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(target.match(ruleContext)).andReturn(MatchResult.MATCH);
		c.replay();
		assertEquals(MatchResult.MATCH, ruleDeny.isApplicable(ruleContext));
		c.verify();
	}
	
	@Test
	public void testPermitRuleIsApplicableWithTargetIndeterminate() throws EvaluationException
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(target.match(ruleContext)).andReturn(MatchResult.INDETERMINATE);
		c.replay();
		assertEquals(MatchResult.INDETERMINATE, rulePermit.isApplicable(ruleContext));
		c.verify();
	}
	
	@Test
	public void testDenyRuleIsApplicableWithTargetIndeterminate() throws EvaluationException
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(target.match(ruleContext)).andReturn(MatchResult.INDETERMINATE);
		c.replay();
		assertEquals(MatchResult.INDETERMINATE, ruleDeny.isApplicable(ruleContext));
		c.verify();
	}
	
	@Test
	public void testPermitRuleIsApplicableWithTargetMatch() throws EvaluationException
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(target.match(ruleContext)).andReturn(MatchResult.MATCH);
		c.replay();
		assertEquals(MatchResult.MATCH, rulePermit.isApplicable(ruleContext));
		c.verify();
	}
	
	@Test
	public void testDenyRuleIsApplicableWithTargetNoMatch() throws EvaluationException
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(target.match(ruleContext)).andReturn(MatchResult.NOMATCH);
		c.replay();
		assertEquals(MatchResult.NOMATCH, ruleDeny.isApplicable(ruleContext));
		c.verify();		
	}
	
	@Test
	public void testDenyRuleConditionTrue() throws EvaluationException
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.TRUE);
		
		Capture<Collection<Advice>> advices = new Capture<Collection<Advice>>();
		Capture<Collection<Obligation>> obligations = new Capture<Collection<Obligation>>();
		
		expect(denyAdviceAttributeExp.evaluate(ruleContext)).andReturn(
				StringType.STRING.create("testVal1"));
		
		
		expect(denyObligationAttributeExp.evaluate(ruleContext)).andReturn(
				StringType.STRING.create("testVal1"));
		
		context.addAdvices(capture(advices));
		context.addObligations(capture(obligations));
				
		c.replay();
		
		assertEquals(Decision.DENY, ruleDeny.evaluate(ruleContext));
		
		c.verify();
		
		assertTrue(
				advices.getValue().contains(Advice
						.builder("denyAdvice", Effect.DENY)
						.attribute(
								"testId", 
								StringType.STRING.create("testVal1"))
								.create()));
		
		assertTrue(
				obligations.getValue().contains(Obligation
						.builder("denyObligation", Effect.DENY)
						.withAttribute(
								"testId", 
								StringType.STRING.create("testVal1"))
								.create()));
		
	}
	
	@Test
	public void testDenyRuleObligationOrAdviceEvaluationFails() throws EvaluationException
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.TRUE);
			
		expect(denyAdviceAttributeExp.evaluate(ruleContext)).andReturn(
				StringType.STRING.create("testVal1"));
		
		expect(denyObligationAttributeExp.evaluate(ruleContext)).andThrow(
				new EvaluationException(StatusCode.createProcessingError(), ruleContext, new NullPointerException()));
		
		c.replay();
		assertEquals(Decision.INDETERMINATE, ruleDeny.evaluate(ruleContext));
		c.verify();
	}
	
	@Test
	public void testDenyRuleConditionFalse() throws EvaluationException
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.FALSE);	
		c.replay();
		
		assertEquals(Decision.NOT_APPLICABLE, ruleDeny.evaluate(ruleContext));
		c.verify();	
	}
	
	@Test
	public void testDenyRuleConditionIndeterminate() throws EvaluationException
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.INDETERMINATE);	
		c.replay();
		assertEquals(Decision.INDETERMINATE_D, ruleDeny.evaluate(ruleContext));
		c.verify();
	}
	
		
	@Test
	public void testPermitRuleConditionTrue() throws EvaluationException
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.TRUE);
		
		Capture<Collection<Advice>> advices = new Capture<Collection<Advice>>();
		Capture<Collection<Obligation>> obligations = new Capture<Collection<Obligation>>();
		
		expect(permitAdviceAttributeExp.evaluate(context)).andReturn(StringType.STRING.create("testVal1"));
		expect(permitObligationAttributeExp.evaluate(context)).andReturn(StringType.STRING.create("testVal1"));
		
		context.addAdvices(capture(advices));
		context.addObligations(capture(obligations));
	
		c.replay();
		assertEquals(Decision.PERMIT, rulePermit.evaluate(ruleContext));
		c.verify();
		
		assertTrue(
				advices.getValue().contains(Advice
						.builder("permitAdvice", Effect.PERMIT)
						.attribute(
								"testId", 
								StringType.STRING.create("testVal1"))
								.create()));
		
		assertTrue(
				obligations.getValue().contains(Obligation
						.builder("permitObligation", Effect.PERMIT)
						.withAttribute(
								"testId", 
								StringType.STRING.create("testVal1"))
								.create()));
	}
	
	@Test
	public void testPermitRuleConditionFalse() throws EvaluationException
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.FALSE);	
		c.replay();
		
		assertEquals(Decision.NOT_APPLICABLE, ruleDeny.evaluate(ruleContext));
		c.verify();
	}
	
	@Test
	public void testPermitRuleConditionIndeterminate() throws EvaluationException
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.INDETERMINATE);	
		c.replay();
		assertEquals(Decision.INDETERMINATE_D, ruleDeny.evaluate(ruleContext));
		c.verify();	
	}

	@Test
	public void testDenyRuleEvaluateIfApplicableWithTargetIndeterminate() 
		throws EvaluationException
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(target.match(ruleContext)).andReturn(MatchResult.INDETERMINATE);
		c.replay();
		assertEquals(Decision.INDETERMINATE_D, ruleDeny.evaluateIfApplicable(ruleContext));
		c.verify();
	}
	
	@Test
	public void testDenyRuleEvaluateIfApplicableWithTargetNoMatch()
		throws EvaluationException
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(target.match(ruleContext)).andReturn(MatchResult.NOMATCH);
		c.replay();
		assertEquals(Decision.NOT_APPLICABLE, ruleDeny.evaluateIfApplicable(ruleContext));
		c.verify();
	}
	
	@Test
	public void testDenyRuleEvaluateIfApplicableWithTargetMatchConditionTrue() throws EvaluationException
	{
		
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		
		expect(target.match(ruleContext)).andReturn(MatchResult.MATCH);
	
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.TRUE);
			
		expect(denyAdviceAttributeExp.evaluate(context)).andReturn(StringType.STRING.create("testVal1"));
		expect(denyObligationAttributeExp.evaluate(context)).andReturn(StringType.STRING.create("testVal1"));
		
		Capture<Collection<Advice>> advices = new Capture<Collection<Advice>>();
		Capture<Collection<Obligation>> obligations = new Capture<Collection<Obligation>>();
		
		context.addAdvices(capture(advices));
		context.addObligations(capture(obligations));
		
		c.replay();
		
		assertEquals(Decision.DENY, ruleDeny.evaluateIfApplicable(ruleContext));
		c.verify();
		
		assertTrue(
				advices.getValue().contains(Advice
						.builder("denyAdvice", Effect.DENY)
						.attribute(
								"testId", 
								StringType.STRING.create("testVal1"))
								.create()));
		
		assertTrue(
				obligations.getValue().contains(Obligation
						.builder("denyObligation", Effect.DENY)
						.withAttribute(
								"testId", 
								StringType.STRING.create("testVal1"))
								.create()));
	}
	
	
	@Test
	public void testPermitRuleEvaluateIfApplicableWithTargetIndeterminate() 
		throws EvaluationException
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(target.match(ruleContext)).andReturn(MatchResult.INDETERMINATE);
		c.replay();
		assertEquals(Decision.INDETERMINATE_P, rulePermit.evaluateIfApplicable(ruleContext));
		c.verify();
	}
	
	@Test
	public void testPermitRuleEvaluateIfApplicableWithTargetNoMatch() 
		throws EvaluationException
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(target.match(ruleContext)).andReturn(MatchResult.NOMATCH);
		c.replay();
		assertEquals(Decision.NOT_APPLICABLE, rulePermit.evaluateIfApplicable(ruleContext));
		c.verify();
	}	
	
	@Test
	public void testPermitRuleEvaluateIfApplicableWithTargetMatchConditionTrue() throws EvaluationException
	{
		
		EvaluationContext ruleContext = rulePermit.createContext(context);
		
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		expect(target.match(ruleContext)).andReturn(MatchResult.MATCH);
		expect(context.getCurrentPolicy()).andReturn(currentPolicy);
		
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.TRUE);
			
		expect(permitAdviceAttributeExp.evaluate(context)).andReturn(StringType.STRING.create("testVal1"));
		expect(permitObligationAttributeExp.evaluate(context)).andReturn(StringType.STRING.create("testVal1"));
		
		Capture<Collection<Advice>> advices = new Capture<Collection<Advice>>();
		Capture<Collection<Obligation>> obligations = new Capture<Collection<Obligation>>();
		
		context.addAdvices(capture(advices));
		context.addObligations(capture(obligations));
		
		
		c.replay();
		
		assertEquals(Decision.PERMIT, rulePermit.evaluateIfApplicable(ruleContext));
		c.verify();
	}
}
