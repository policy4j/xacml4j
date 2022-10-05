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

import com.google.common.collect.Iterables;
import org.easymock.Capture;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.*;
import org.xacml4j.v30.types.XacmlTypes;

import java.time.Duration;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class PolicySetTest
{
	private EvaluationContext context;
	private PolicySet policy;
	private Target target;
	private Condition condition;


	private DecisionCombiningAlgorithm<CompositeDecisionRule> combingingAlg;

	private Expression denyObligationAttributeExp;
	private Expression permitObligationAttributeExp;

	private Expression denyAdviceAttributeExp;
	private Expression permitAdviceAttributeExp;



	private PolicyReferenceResolver referenceResolver;
	private EvaluationContextHandler handler;

	private IMocksControl c;

	@SuppressWarnings("unchecked")
	@Before
	public void init() throws SyntaxException
	{
		this.c = createStrictControl();

		this.target = c.createMock(Target.class);
		this.condition = c.createMock(Condition.class);
		this.combingingAlg = c.createMock(DecisionCombiningAlgorithm.class);

		this.permitAdviceAttributeExp = c.createMock(Expression.class);
		this.denyAdviceAttributeExp = c.createMock(Expression.class);

		this.denyObligationAttributeExp = c.createMock(Expression.class);
		this.permitObligationAttributeExp = c.createMock(Expression.class);

		this.policy = PolicySet.builder("TestPolicy")
				.version("1.0")
				.target(target)
				.condition(condition)
				.withCombiningAlgorithm(combingingAlg)
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
					.attribute("testId", permitAdviceAttributeExp))
				.build();

		this.referenceResolver = c.createMock(PolicyReferenceResolver.class);
		this.handler = c.createMock(EvaluationContextHandler.class);
		this.context = new RootEvaluationContext(true, Duration.ZERO, referenceResolver, handler);
	}

	@Test
	public void testCreateContext() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		assertSame(policy, policyContext.getCurrentPolicySet());
		assertSame(context, policyContext.getParentContext());
		EvaluationContext policyContext1 = policy.createContext(policyContext);
		assertSame(policyContext, policyContext1);
	}

	@Test
	public void testIsApplicableTargetMatch() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		expect(target.match(policyContext)).andReturn(MatchResult.MATCH);
		c.replay();
		assertEquals(MatchResult.MATCH, policy.isMatch(policyContext));
		c.verify();
	}

	@Test
	public void testIsApplicableTargetNoMatch() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		expect(target.match(policyContext)).andReturn(MatchResult.NOMATCH);
		c.replay();
		assertEquals(MatchResult.NOMATCH, policy.isMatch(policyContext));
		c.verify();
	}

	@Test
	public void testIsApplicableTargetIndeterminate() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		expect(target.match(policyContext)).andReturn(MatchResult.INDETERMINATE);
		c.replay();
		assertEquals(MatchResult.INDETERMINATE, policy.isMatch(policyContext));
		c.verify();
	}

	@Test
	public void testIsApplicableTargetException() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		expect(target.match(policyContext)).andThrow(new NullPointerException());
		c.replay();
		assertEquals(MatchResult.INDETERMINATE, policy.isMatch(policyContext));
		c.verify();
	}

	@Test
	public void testEvaluateTargetMatchConditionFalse() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		expect(target.match(policyContext)).andReturn(MatchResult.MATCH);
		expect(condition.evaluate(policyContext)).andReturn(ConditionResult.FALSE);
		c.replay();
		assertEquals(Decision.NOT_APPLICABLE, policy.evaluate(policyContext));
		c.verify();
	}

	@Test
	public void testEvaluateTargetMatchConditionTrue() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		expect(target.match(policyContext)).andReturn(MatchResult.MATCH);
		expect(condition.evaluate(policyContext)).andReturn(ConditionResult.FALSE);
		c.replay();
		assertEquals(Decision.NOT_APPLICABLE, policy.evaluate(policyContext));
		c.verify();
	}

	@Test
	public void testEvaluateTargetIndeterminateCombiningAlgoReturnsNotApplicable() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		Capture<List<CompositeDecisionRule>> ruleCapture = new Capture<List<CompositeDecisionRule>>();
		Capture<EvaluationContext> contextCapture = new Capture<EvaluationContext>();
		expect(target.match(policyContext)).andReturn(MatchResult.INDETERMINATE);
		expect(combingingAlg.combine(capture(contextCapture), capture(ruleCapture))).andReturn(Decision.NOT_APPLICABLE);
		c.replay();
		assertEquals(Decision.NOT_APPLICABLE, policy.evaluate(policyContext));
		c.verify();
	}

	@Test
	public void testEvaluateTargetIndeterminateCombiningAlgoReturnsDeny() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		Capture<List<CompositeDecisionRule>> ruleCapture = new Capture<List<CompositeDecisionRule>>();
		Capture<EvaluationContext> contextCapture = new Capture<EvaluationContext>();
		expect(target.match(policyContext)).andReturn(MatchResult.INDETERMINATE);
		expect(combingingAlg.combine(capture(contextCapture), capture(ruleCapture))).andReturn(Decision.DENY);
		c.replay();
		assertEquals(Decision.INDETERMINATE_D, policy.evaluate(policyContext));
		assertTrue(contextCapture.getValue().isExtendedIndeterminateEval());
		c.verify();
	}

	@Test
	public void testEvaluateTargetIndeterminateCombiningAlgoReturnsPermit() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		Capture<List<CompositeDecisionRule>> ruleCapture = new Capture<List<CompositeDecisionRule>>();
		Capture<EvaluationContext> contextCapture = new Capture<EvaluationContext>();
		expect(target.match(policyContext)).andReturn(MatchResult.INDETERMINATE);
		expect(combingingAlg.combine(capture(contextCapture), capture(ruleCapture))).andReturn(Decision.PERMIT);
		c.replay();
		assertEquals(Decision.INDETERMINATE_P, policy.evaluate(policyContext));
		assertTrue(contextCapture.getValue().isExtendedIndeterminateEval());
		c.verify();
	}

	@Test
	public void testEvaluateTargetIndeterminateCombiningAlgoReturnsindeterminate() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		Capture<List<CompositeDecisionRule>> ruleCapture = new Capture<List<CompositeDecisionRule>>();
		Capture<EvaluationContext> contextCapture = new Capture<EvaluationContext>();
		expect(target.match(policyContext)).andReturn(MatchResult.INDETERMINATE);
		expect(combingingAlg.combine(capture(contextCapture), capture(ruleCapture))).andReturn(Decision.INDETERMINATE);
		c.replay();
		assertEquals(Decision.INDETERMINATE_DP, policy.evaluate(policyContext));
		assertTrue(contextCapture.getValue().isExtendedIndeterminateEval());
		c.verify();
	}

	@Test
	public void testEvaluateTargetMatchConditionTrueCombiningAlgorithmResultIsDeny() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);

		Capture<EvaluationContext> contextCapture = new Capture<EvaluationContext>();
		Capture<List<CompositeDecisionRule>> ruleCapture = new Capture<List<CompositeDecisionRule>>();

		expect(target.match(policyContext)).andReturn(MatchResult.MATCH);
		expect(condition.evaluate(policyContext)).andReturn(ConditionResult.TRUE);

		expect(combingingAlg.combine(capture(contextCapture), capture(ruleCapture))).andReturn(Decision.DENY);

		expect(denyAdviceAttributeExp.evaluate(policyContext)).andReturn(XacmlTypes.STRING.of("testValue1"));
		expect(denyObligationAttributeExp.evaluate(policyContext)).andReturn(XacmlTypes.STRING.of("testValue1"));

		c.replay();
		assertEquals(Decision.DENY, policy.evaluate(policyContext));
		c.verify();
		assertSame(policy, policyContext.getCurrentPolicySet());
		assertSame(policyContext, contextCapture.getValue());

		assertEquals(1, Iterables.size(context.getMatchingAdvices(Decision.DENY)));
		assertEquals(1, Iterables.size(context.getMatchingObligations(Decision.DENY)));
	}

	@Test
	public void testEvaluateTargetMatchConditionTrueCombiningAlgorithResultIsPermit() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);

		Capture<EvaluationContext> contextCapture = new Capture<EvaluationContext>();
		Capture<List<CompositeDecisionRule>> ruleCapture = new Capture<List<CompositeDecisionRule>>();

		expect(target.match(policyContext)).andReturn(MatchResult.MATCH);
		expect(condition.evaluate(policyContext)).andReturn(ConditionResult.TRUE);

		expect(combingingAlg.combine(capture(contextCapture), capture(ruleCapture))).andReturn(Decision.PERMIT);
		expect(permitAdviceAttributeExp.evaluate(policyContext)).andReturn(XacmlTypes.STRING.of("testValue1"));
		expect(permitObligationAttributeExp.evaluate(policyContext)).andReturn(XacmlTypes.STRING.of("testValue1"));

		c.replay();
		assertEquals(Decision.PERMIT, policy.evaluate(policyContext));
		c.verify();
		assertSame(policy, policyContext.getCurrentPolicySet());
		assertSame(policyContext, contextCapture.getValue());
		assertEquals(1, Iterables.size(context.getMatchingAdvices(Decision.PERMIT)));
		assertEquals(1, Iterables.size(context.getMatchingObligations(Decision.PERMIT)));
	}

	@Test
	public void testEvaluateTargetMatchConditionTrueCombiningAlgorithResultIsIndeterminate() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);

		Capture<EvaluationContext> contextCapture = new Capture<EvaluationContext>();
		Capture<List<CompositeDecisionRule>> ruleCapture = new Capture<List<CompositeDecisionRule>>();

		expect(target.match(policyContext)).andReturn(MatchResult.MATCH);
		expect(condition.evaluate(policyContext)).andReturn(ConditionResult.TRUE);

		expect(combingingAlg.combine(capture(contextCapture), capture(ruleCapture))).andReturn(Decision.INDETERMINATE);

		c.replay();
		assertEquals(Decision.INDETERMINATE, policy.evaluate(policyContext));
		c.verify();
		assertSame(policy, policyContext.getCurrentPolicySet());
		assertSame(policyContext, contextCapture.getValue());
		assertEquals(0, Iterables.size(context.getMatchingAdvices(Decision.DENY)));
		assertEquals(0, Iterables.size(context.getMatchingObligations(Decision.DENY)));
		assertEquals(0, Iterables.size(context.getMatchingAdvices(Decision.PERMIT)));
		assertEquals(0, Iterables.size(context.getMatchingObligations(Decision.PERMIT)));
	}
}
