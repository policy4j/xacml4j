package org.xacml4j.v30.policy;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.easymock.EasyMock.createStrictControl;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.util.Collection;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.Advice;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.Obligation;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.types.XacmlTypes;


public class RuleTest
{
	private Rule rulePermit;
	private Rule ruleDeny;

	private Policy enclosingPolicy;
	private Condition condition;
	private Target target;

	private EvaluationContext context;
	private EvaluationContextHandler handler;
	private PolicyReferenceResolver resolver;

	private IMocksControl c;

	private Expression denyObligationAttributeExp;
	private Expression permitObligationAttributeExp;

	private Expression denyAdviceAttributeExp;
	private Expression permitAdviceAttributeExp;

	private Rule.Builder builder;
	private DecisionCombiningAlgorithm<Rule> combiner;

	@Before
	public void init()
	{
		this.c = createStrictControl();
		this.combiner = c.createMock(DecisionCombiningAlgorithm.class);
		this.handler = c.createMock(EvaluationContextHandler.class);
		this.resolver = c.createMock(PolicyReferenceResolver.class);


		this.condition = c.createMock(Condition.class);
		this.target = c.createMock(Target.class);
		this.enclosingPolicy = c.createMock(Policy.class);


		this.permitAdviceAttributeExp = c.createMock(Expression.class);
		this.denyAdviceAttributeExp = c.createMock(Expression.class);

		this.denyObligationAttributeExp = c.createMock(Expression.class);
		this.permitObligationAttributeExp = c.createMock(Expression.class);

		Policy.Builder b  = Policy.builder("testPolicyId").version("1.0");

		this.builder = Rule
				.builder("TestRuleId", Effect.DENY)
				.target(target)
				.condition(condition)
				.obligation(ObligationExpression
						.builder("denyObligation", Effect.DENY)
							.attribute("testId", denyObligationAttributeExp))
				.obligation(ObligationExpression
						.builder("permitObligation", Effect.PERMIT)
						.attribute("testId", permitObligationAttributeExp))
				.advice(AdviceExpression
						.builder("denyAdvice", Effect.DENY)
						.attribute("testId", denyAdviceAttributeExp))
				.advice(AdviceExpression
					.builder("permitAdvice", Effect.PERMIT)
					.attribute("testId", permitAdviceAttributeExp));
		this.rulePermit = builder
				.id("testPermitRule")
				.withEffect(Effect.PERMIT)
				.build();
		this.ruleDeny = builder
				.id("testDenyRule")
				.withEffect(Effect.DENY)
				.build();
		this.enclosingPolicy =  b.rule(rulePermit, ruleDeny).combiningAlgorithm(combiner).build();
		this.context =  enclosingPolicy.createContext(new RootEvaluationContext(false, Duration.ZERO, resolver, handler));

	}

	@Test
	public void testDenyRuleIsApplicableWithNoTarget() throws EvaluationException
	{
		DecisionRule ruleDenyNoTarget = builder.withoutTarget().withEffect(Effect.DENY).build();
		EvaluationContext ruleContext = ruleDenyNoTarget.createContext(context);
		c.replay();
		assertEquals(MatchResult.MATCH, ruleDenyNoTarget.isMatch(ruleContext));
		c.verify();
	}

	@Test
	public void testPermitRuleIsApplicableWithNoTarget() throws EvaluationException
	{
		DecisionRule rulePermitNoTarget = builder.withoutTarget().withEffect(Effect.PERMIT).build();
		EvaluationContext ruleContext = rulePermitNoTarget.createContext(context);
		c.replay();
		assertEquals(MatchResult.MATCH, rulePermitNoTarget.isMatch(ruleContext));
		c.verify();
	}

	@Test
	public void testDenyRuleApplicabilityWithTargetMatch() throws EvaluationException
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.MATCH);
		c.replay();
		assertEquals(MatchResult.MATCH, ruleDeny.isMatch(ruleContext));
		c.verify();
	}

	@Test
	public void testPermitRuleIsApplicableWithTargetIndeterminate() throws EvaluationException
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.INDETERMINATE);
		c.replay();
		assertEquals(MatchResult.INDETERMINATE, rulePermit.isMatch(ruleContext));
		c.verify();
	}

	@Test
	public void testDenyRuleIsApplicableWithTargetIndeterminate() throws EvaluationException
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.INDETERMINATE);
		c.replay();
		assertEquals(MatchResult.INDETERMINATE, ruleDeny.isMatch(ruleContext));
		c.verify();
	}

	@Test
	public void testPermitRuleIsApplicableWithTargetMatch() throws EvaluationException
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.MATCH);
		c.replay();
		assertEquals(MatchResult.MATCH, rulePermit.isMatch(ruleContext));
		c.verify();
	}

	@Test
	public void testDenyRuleIsApplicableWithTargetNoMatch() throws EvaluationException
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.NOMATCH);
		c.replay();
		assertEquals(MatchResult.NOMATCH, ruleDeny.isMatch(ruleContext));
		c.verify();
	}

	@Test
	public void testDenyRuleConditionTrue() throws EvaluationException
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.MATCH);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.TRUE);

		expect(denyAdviceAttributeExp.evaluate(ruleContext)).andReturn(
				XacmlTypes.STRING.ofAny("testVal1"));
		expect(denyObligationAttributeExp.evaluate(ruleContext)).andReturn(
				XacmlTypes.STRING.ofAny("testVal1"));
		c.replay();
		assertEquals(Decision.DENY, ruleDeny.evaluate(ruleContext));
		c.verify();
		Collection<Advice> advices = context.getMatchingAdvices(Decision.DENY);
		System.out.println(advices);
		assertTrue(
				advices
				       .contains(Advice.builder("denyAdvice", Effect.DENY)
				                       .attribute("testId", XacmlTypes.STRING.ofAny("testVal1"))
				                       .build()));
		assertTrue(
				context.getMatchingObligations(Decision.DENY)
				       .contains(Obligation.builder("denyObligation", Effect.DENY)
				                           .attribute("testId", XacmlTypes.STRING.ofAny("testVal1"))
				                           .build()));
	}

	@Test
	public void testDenyRuleObligationOrAdviceEvaluationFails() throws EvaluationException
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);

		expect(target.match(ruleContext)).andReturn(MatchResult.MATCH);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.TRUE);

		expect(denyAdviceAttributeExp.evaluate(ruleContext)).andReturn(
				XacmlTypes.STRING.ofAny("testVal1"));

		expect(denyObligationAttributeExp.evaluate(ruleContext)).andThrow(
				new EvaluationException(Status.processingError().build(), new NullPointerException()));

		c.replay();
		assertEquals(Decision.INDETERMINATE_D, ruleDeny.evaluate(ruleContext));
		c.verify();
	}

	@Test
	public void testDenyRuleConditionFalse() throws EvaluationException
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.MATCH);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.FALSE);

		c.replay();

		assertEquals(Decision.NOT_APPLICABLE, ruleDeny.evaluate(ruleContext));
		c.verify();
	}

	@Test
	public void testDenyRuleConditionIndeterminate() throws EvaluationException
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.MATCH);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.INDETERMINATE);
		c.replay();
		assertEquals(Decision.INDETERMINATE_D, ruleDeny.evaluate(ruleContext));
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, ruleContext.getEvaluationStatus().map(s->s.getStatusCode().getValue()).orElse(null));
		c.verify();
	}


	@Test
	public void testPermitRuleConditionTrue() throws EvaluationException
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.MATCH);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.TRUE);

		expect(permitAdviceAttributeExp.evaluate(ruleContext)).andReturn(XacmlTypes.STRING.ofAny("testVal1"));
		expect(permitObligationAttributeExp.evaluate(ruleContext)).andReturn(XacmlTypes.STRING.ofAny("testVal1"));


		c.replay();
		assertEquals(Decision.PERMIT, rulePermit.evaluate(ruleContext));
		c.verify();

		assertTrue(
				context.getMatchingAdvices(Decision.PERMIT).contains(Advice
						.builder("permitAdvice", Effect.PERMIT)
						.attribute(
								"testId",
								XacmlTypes.STRING.ofAny("testVal1"))
								.build()));

		assertTrue(
				context.getMatchingObligations(Decision.PERMIT).contains(Obligation
						.builder("permitObligation", Effect.PERMIT)
						.attribute(
								"testId",
								XacmlTypes.STRING.ofAny("testVal1"))
								.build()));
	}

	@Test
	public void testPermitRuleConditionFalse() throws EvaluationException
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.MATCH);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.FALSE);
		c.replay();
		assertEquals(Decision.NOT_APPLICABLE, rulePermit.evaluate(ruleContext));
		c.verify();
	}

	@Test
	public void testPermitRuleConditionIndeterminate() throws EvaluationException
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.MATCH);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.INDETERMINATE);
		c.replay();
		assertEquals(Decision.INDETERMINATE_P, rulePermit.evaluate(ruleContext));
		c.verify();
	}

	@Test
	public void testDenyRuleEvaluateIfApplicableWithTargetIndeterminate()
		throws EvaluationException
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.INDETERMINATE);
		c.replay();
		assertEquals(Decision.INDETERMINATE_D, ruleDeny.evaluate(ruleContext));
		c.verify();
	}

	@Test
	public void testDenyRuleEvaluateIfApplicableWithTargetNoMatch()
		throws EvaluationException
	{
		EvaluationContext ruleContext = ruleDeny.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.NOMATCH);
		c.replay();
		assertEquals(Decision.NOT_APPLICABLE, ruleDeny.evaluate(ruleContext));
		c.verify();
	}

	@Test
	public void testDenyRuleEvaluateIfApplicableWithTargetMatchConditionTrue() throws EvaluationException
	{

		EvaluationContext ruleContext = ruleDeny.createContext(context);

		expect(target.match(ruleContext)).andReturn(MatchResult.MATCH);
		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.TRUE);

		expect(denyAdviceAttributeExp.evaluate(ruleContext)).andReturn(XacmlTypes.STRING.ofAny("testVal1"));
		expect(denyObligationAttributeExp.evaluate(ruleContext)).andReturn(XacmlTypes.STRING.ofAny("testVal1"));

		c.replay();

		assertEquals(Decision.DENY, ruleDeny.evaluate(ruleContext));
		c.verify();

		assertTrue(
				context.getMatchingAdvices(Decision.DENY).contains(Advice
						.builder("denyAdvice", Effect.DENY)
						.attribute(
								"testId",
								XacmlTypes.STRING.ofAny("testVal1"))
								.build()));

		assertTrue(
				context.getMatchingObligations(Decision.DENY).contains(Obligation
						.builder("denyObligation", Effect.DENY)
						.attribute(
								"testId",
								XacmlTypes.STRING.ofAny("testVal1"))
								.build()));
	}


	@Test
	public void testPermitRuleEvaluateIfApplicableWithTargetIndeterminate()
		throws EvaluationException
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.INDETERMINATE);
		c.replay();
		assertEquals(Decision.INDETERMINATE_P, rulePermit.evaluate(ruleContext));
		c.verify();
	}

	@Test
	public void testPermitRuleEvaluateIfApplicableWithTargetEvaluationException()
			throws EvaluationException
	{
		Status.Builder b =  Status.missingAttribute(AttributeDesignatorKey.builder("id")
		                                              .category(CategoryId.ACTION)
		                                              .dataType(XacmlTypes.INTEGER)
		                                              .build());
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(target.match(ruleContext)).andThrow(new EvaluationException(b.build(), "Bad"));
		c.replay();
		assertEquals(Decision.INDETERMINATE_P, rulePermit.evaluate(ruleContext));
		c.verify();
		assertEquals(ruleContext.getEvaluationStatus().get(), b.message("Bad").build());
		assertEquals(context.getEvaluationStatus().get(), b.message("Bad").build());
	}

	@Test
	public void testPermitRuleEvaluateIfApplicableWithTargetNoMatch()
		throws EvaluationException
	{
		EvaluationContext ruleContext = rulePermit.createContext(context);
		expect(target.match(ruleContext)).andReturn(MatchResult.NOMATCH);
		c.replay();
		assertEquals(Decision.NOT_APPLICABLE, rulePermit.evaluate(ruleContext));
		c.verify();
	}

	@Test
	public void testPermitRuleEvaluateIfApplicableWithTargetMatchConditionTrue() throws EvaluationException
	{

		EvaluationContext ruleContext = rulePermit.createContext(context);

		expect(target.match(ruleContext)).andReturn(MatchResult.MATCH);

		expect(condition.evaluate(ruleContext)).andReturn(ConditionResult.TRUE);

		expect(permitAdviceAttributeExp.evaluate(ruleContext)).andReturn(XacmlTypes.STRING.ofAny("testVal1"));
		expect(permitObligationAttributeExp.evaluate(ruleContext)).andReturn(XacmlTypes.STRING.ofAny("testVal1"));

		c.replay();

		assertEquals(Decision.PERMIT, rulePermit.evaluate(ruleContext));

		assertTrue(
				context.getMatchingAdvices(Decision.PERMIT).contains(Advice
						.builder("permitAdvice", Effect.PERMIT)
						.attribute(
								"testId",
								XacmlTypes.STRING.ofAny("testVal1"))
								.build()));

		assertTrue(
				context.getMatchingObligations(Decision.PERMIT).contains(Obligation
						.builder("permitObligation", Effect.PERMIT)
						.attribute(
								"testId",
								XacmlTypes.STRING.ofAny("testVal1"))
								.build()));
		c.verify();
	}

	@Test
	public void testEquals(){
		Rule r1 = builder.build();
		Rule r2 = builder.build();
		Policy p1 = Policy
				.builder("policy-id")
				.combiningAlgorithm(combiner)
				.build();
		assertEquals(r1, r2);
		assertFalse(r1.equals(null));
		assertFalse(r1.equals(p1));
	}

	@Test
	public void testHashCode(){
		Rule r1 = builder.build();
		Rule r2 = builder.build();

		assertEquals(r1.hashCode(), r2.hashCode());
	}
}
