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

import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.Iterables;

@ExtendWith(MockitoExtension.class)
public class PolicySetTest
{
	private EvaluationContext context;
	private PolicySet policy;
	private PolicySet.Builder builder;
	private Target target;
	private Expression targetExpression;
	private Condition condition;
	private Expression conditionExpression;


	private DecisionCombiningAlgorithm<CompositeDecisionRule> combingingAlg;

	private Expression denyObligationAttributeExp;
	private Expression permitObligationAttributeExp;

	private Expression denyAdviceAttributeExp;
	private Expression permitAdviceAttributeExp;


	private PolicyReferenceResolver referenceResolver;
	private EvaluationContextHandler handler;


	@SuppressWarnings("unchecked")
	@BeforeEach
	public void init() throws SyntaxException
	{

		this.targetExpression = ModelFixtures.reference();
		this.target = ModelFixtures.target(targetExpression);
		this.conditionExpression = mock(Expression.class);
		this.condition = ModelFixtures.condition(conditionExpression);
		this.combingingAlg = mock(DecisionCombiningAlgorithm.class);

		this.permitAdviceAttributeExp = mock(Expression.class);
		this.denyAdviceAttributeExp = mock(Expression.class);

		this.denyObligationAttributeExp = mock(Expression.class);
		this.permitObligationAttributeExp = mock(Expression.class);

		this.builder = PolicySet.builder("TestPolicy")
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
					.attribute("testId", permitAdviceAttributeExp));
		this.policy = builder.build();

		this.referenceResolver = mock(PolicyReferenceResolver.class);
		this.handler = mock(EvaluationContextHandler.class);
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
		when(targetExpression.evaluate(policyContext)).thenReturn(ModelFixtures.matchValues(MatchResult.MATCH));
		InOrder order = inOrder(targetExpression, conditionExpression, combingingAlg, permitAdviceAttributeExp, denyAdviceAttributeExp, denyObligationAttributeExp, permitObligationAttributeExp, referenceResolver, handler);
		assertEquals(MatchResult.MATCH, policy.isMatch(policyContext));
		order.verify(targetExpression).evaluate(policyContext);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(targetExpression, times(1)).evaluate(policyContext);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(targetExpression, conditionExpression, combingingAlg, permitAdviceAttributeExp, denyAdviceAttributeExp, denyObligationAttributeExp, permitObligationAttributeExp, referenceResolver, handler);
	}

	@Test
	public void testIsApplicableTargetNoMatch() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		when(targetExpression.evaluate(policyContext)).thenReturn(ModelFixtures.matchValues(MatchResult.NOMATCH));
		InOrder order = inOrder(targetExpression, conditionExpression, combingingAlg, permitAdviceAttributeExp, denyAdviceAttributeExp, denyObligationAttributeExp, permitObligationAttributeExp, referenceResolver, handler);
		assertEquals(MatchResult.NOMATCH, policy.isMatch(policyContext));
		order.verify(targetExpression).evaluate(policyContext);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(targetExpression, times(1)).evaluate(policyContext);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(targetExpression, conditionExpression, combingingAlg, permitAdviceAttributeExp, denyAdviceAttributeExp, denyObligationAttributeExp, permitObligationAttributeExp, referenceResolver, handler);
	}

	@Test
	public void testIsApplicableTargetIndeterminate() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		when(targetExpression.evaluate(policyContext)).thenThrow(ModelFixtures.evaluationError());
		InOrder order = inOrder(targetExpression, conditionExpression, combingingAlg, permitAdviceAttributeExp, denyAdviceAttributeExp, denyObligationAttributeExp, permitObligationAttributeExp, referenceResolver, handler);
		assertEquals(MatchResult.INDETERMINATE, policy.isMatch(policyContext));
		order.verify(targetExpression).evaluate(policyContext);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(targetExpression, times(1)).evaluate(policyContext);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(targetExpression, conditionExpression, combingingAlg, permitAdviceAttributeExp, denyAdviceAttributeExp, denyObligationAttributeExp, permitObligationAttributeExp, referenceResolver, handler);
	}

	@Test
	public void testIsApplicableTargetException() throws EvaluationException
	{
		// rv019: inject at Target.match so BaseDecisionRule.isMatch must catch the exception.
		Target throwingTarget = spy(target);
		PolicySet policyWithThrowingTarget = builder.target(throwingTarget).build();
		EvaluationContext policyContext = policyWithThrowingTarget.createContext(context);
		doThrow(new NullPointerException()).when(throwingTarget).match(policyContext);
		InOrder order = inOrder(throwingTarget, targetExpression, conditionExpression, combingingAlg, permitAdviceAttributeExp, denyAdviceAttributeExp, denyObligationAttributeExp, permitObligationAttributeExp, referenceResolver, handler);
		assertEquals(MatchResult.INDETERMINATE, policyWithThrowingTarget.isMatch(policyContext));
		order.verify(throwingTarget).match(policyContext);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(throwingTarget, times(1)).match(policyContext);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(throwingTarget, targetExpression, conditionExpression, combingingAlg, permitAdviceAttributeExp, denyAdviceAttributeExp, denyObligationAttributeExp, permitObligationAttributeExp, referenceResolver, handler);
		assertEquals(Status.processingError(new NullPointerException())
		                   .build(), policyContext.getEvaluationStatus().get());
	}

	@Test
	public void testEvaluateTargetMatchConditionFalse() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		when(targetExpression.evaluate(policyContext)).thenReturn(ModelFixtures.matchValues(MatchResult.MATCH));
		when(conditionExpression.evaluate(policyContext)).thenReturn(XacmlTypes.BOOLEAN.ofAny(false));
		InOrder order = inOrder(targetExpression, conditionExpression, combingingAlg, permitAdviceAttributeExp, denyAdviceAttributeExp, denyObligationAttributeExp, permitObligationAttributeExp, referenceResolver, handler);
		assertEquals(Decision.NOT_APPLICABLE, policy.evaluate(policyContext));
		order.verify(targetExpression).evaluate(policyContext);
		order.verify(conditionExpression).evaluate(policyContext);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(targetExpression, times(1)).evaluate(policyContext);
		verify(conditionExpression, times(1)).evaluate(policyContext);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(targetExpression, conditionExpression, combingingAlg, permitAdviceAttributeExp, denyAdviceAttributeExp, denyObligationAttributeExp, permitObligationAttributeExp, referenceResolver, handler);
	}

	@Test
	public void testEvaluateTargetMatchConditionTrue() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		when(targetExpression.evaluate(policyContext)).thenReturn(ModelFixtures.matchValues(MatchResult.MATCH));
		when(conditionExpression.evaluate(policyContext)).thenReturn(XacmlTypes.BOOLEAN.ofAny(false));
		InOrder order = inOrder(targetExpression, conditionExpression, combingingAlg, permitAdviceAttributeExp, denyAdviceAttributeExp, denyObligationAttributeExp, permitObligationAttributeExp, referenceResolver, handler);
		assertEquals(Decision.NOT_APPLICABLE, policy.evaluate(policyContext));
		order.verify(targetExpression).evaluate(policyContext);
		order.verify(conditionExpression).evaluate(policyContext);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(targetExpression, times(1)).evaluate(policyContext);
		verify(conditionExpression, times(1)).evaluate(policyContext);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(targetExpression, conditionExpression, combingingAlg, permitAdviceAttributeExp, denyAdviceAttributeExp, denyObligationAttributeExp, permitObligationAttributeExp, referenceResolver, handler);
	}

	@Test
	public void testEvaluateTargetIndeterminateCombiningAlgoReturnsNotApplicable() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		ArgumentCaptor<List<CompositeDecisionRule>> ruleCapture = ArgumentCaptor.forClass(List.class);
		ArgumentCaptor<EvaluationContext> contextCapture = ArgumentCaptor.forClass(EvaluationContext.class);
		when(targetExpression.evaluate(policyContext)).thenThrow(ModelFixtures.evaluationError());
		when(combingingAlg.combine(contextCapture.capture(), ruleCapture.capture())).thenReturn(Decision.NOT_APPLICABLE);
		InOrder order = inOrder(targetExpression, conditionExpression, combingingAlg, permitAdviceAttributeExp, denyAdviceAttributeExp, denyObligationAttributeExp, permitObligationAttributeExp, referenceResolver, handler);
		assertEquals(Decision.NOT_APPLICABLE, policy.evaluate(policyContext));
		order.verify(targetExpression).evaluate(policyContext);
		order.verify(combingingAlg).combine(contextCapture.capture(), ruleCapture.capture());
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(targetExpression, times(1)).evaluate(policyContext);
		verify(combingingAlg, times(1)).combine(contextCapture.capture(), ruleCapture.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(targetExpression, conditionExpression, combingingAlg, permitAdviceAttributeExp, denyAdviceAttributeExp, denyObligationAttributeExp, permitObligationAttributeExp, referenceResolver, handler);
		assertEquals(java.util.Collections.emptyList(), ruleCapture.getValue());
		assertTrue(contextCapture.getValue().isExtendedIndeterminateEval());
		assertSame(policy, contextCapture.getValue().getCurrentPolicySet());
	}

	@Test
	public void testEvaluateTargetIndeterminateCombiningAlgoReturnsDeny() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		ArgumentCaptor<List<CompositeDecisionRule>> ruleCapture = ArgumentCaptor.forClass(List.class);
		ArgumentCaptor<EvaluationContext> contextCapture = ArgumentCaptor.forClass(EvaluationContext.class);
		when(targetExpression.evaluate(policyContext)).thenThrow(ModelFixtures.evaluationError());
		when(combingingAlg.combine(contextCapture.capture(), ruleCapture.capture())).thenReturn(Decision.DENY);
		InOrder order = inOrder(targetExpression, conditionExpression, combingingAlg, permitAdviceAttributeExp, denyAdviceAttributeExp, denyObligationAttributeExp, permitObligationAttributeExp, referenceResolver, handler);
		assertEquals(Decision.INDETERMINATE_D, policy.evaluate(policyContext));
		assertTrue(contextCapture.getValue().isExtendedIndeterminateEval());
		order.verify(targetExpression).evaluate(policyContext);
		order.verify(combingingAlg).combine(contextCapture.capture(), ruleCapture.capture());
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(targetExpression, times(1)).evaluate(policyContext);
		verify(combingingAlg, times(1)).combine(contextCapture.capture(), ruleCapture.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(targetExpression, conditionExpression, combingingAlg, permitAdviceAttributeExp, denyAdviceAttributeExp, denyObligationAttributeExp, permitObligationAttributeExp, referenceResolver, handler);
		assertEquals(java.util.Collections.emptyList(), ruleCapture.getValue());
	}

	@Test
	public void testEvaluateTargetIndeterminateCombiningAlgoReturnsPermit() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		ArgumentCaptor<List<CompositeDecisionRule>> ruleCapture = ArgumentCaptor.forClass(List.class);
		ArgumentCaptor<EvaluationContext> contextCapture = ArgumentCaptor.forClass(EvaluationContext.class);
		when(targetExpression.evaluate(policyContext)).thenThrow(ModelFixtures.evaluationError());
		when(combingingAlg.combine(contextCapture.capture(), ruleCapture.capture())).thenReturn(Decision.PERMIT);
		InOrder order = inOrder(targetExpression, conditionExpression, combingingAlg, permitAdviceAttributeExp, denyAdviceAttributeExp, denyObligationAttributeExp, permitObligationAttributeExp, referenceResolver, handler);
		assertEquals(Decision.INDETERMINATE_P, policy.evaluate(policyContext));
		assertTrue(contextCapture.getValue().isExtendedIndeterminateEval());
		order.verify(targetExpression).evaluate(policyContext);
		order.verify(combingingAlg).combine(contextCapture.capture(), ruleCapture.capture());
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(targetExpression, times(1)).evaluate(policyContext);
		verify(combingingAlg, times(1)).combine(contextCapture.capture(), ruleCapture.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(targetExpression, conditionExpression, combingingAlg, permitAdviceAttributeExp, denyAdviceAttributeExp, denyObligationAttributeExp, permitObligationAttributeExp, referenceResolver, handler);
		assertEquals(java.util.Collections.emptyList(), ruleCapture.getValue());
	}

	@Test
	public void testEvaluateTargetIndeterminateCombiningAlgoReturnsindeterminate() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);
		ArgumentCaptor<List<CompositeDecisionRule>> ruleCapture = ArgumentCaptor.forClass(List.class);
		ArgumentCaptor<EvaluationContext> contextCapture = ArgumentCaptor.forClass(EvaluationContext.class);
		when(targetExpression.evaluate(policyContext)).thenThrow(ModelFixtures.evaluationError());
		when(combingingAlg.combine(contextCapture.capture(), ruleCapture.capture())).thenReturn(Decision.INDETERMINATE);
		InOrder order = inOrder(targetExpression, conditionExpression, combingingAlg, permitAdviceAttributeExp, denyAdviceAttributeExp, denyObligationAttributeExp, permitObligationAttributeExp, referenceResolver, handler);
		assertEquals(Decision.INDETERMINATE_DP, policy.evaluate(policyContext));
		assertTrue(contextCapture.getValue().isExtendedIndeterminateEval());
		order.verify(targetExpression).evaluate(policyContext);
		order.verify(combingingAlg).combine(contextCapture.capture(), ruleCapture.capture());
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(targetExpression, times(1)).evaluate(policyContext);
		verify(combingingAlg, times(1)).combine(contextCapture.capture(), ruleCapture.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(targetExpression, conditionExpression, combingingAlg, permitAdviceAttributeExp, denyAdviceAttributeExp, denyObligationAttributeExp, permitObligationAttributeExp, referenceResolver, handler);
		assertEquals(java.util.Collections.emptyList(), ruleCapture.getValue());
	}

	@Test
	public void testEvaluateTargetMatchConditionTrueCombiningAlgorithmResultIsDeny() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);

		ArgumentCaptor<EvaluationContext> contextCapture = ArgumentCaptor.forClass(EvaluationContext.class);
		ArgumentCaptor<List<CompositeDecisionRule>> ruleCapture = ArgumentCaptor.forClass(List.class);

		when(targetExpression.evaluate(policyContext)).thenReturn(ModelFixtures.matchValues(MatchResult.MATCH));
		when(conditionExpression.evaluate(policyContext)).thenReturn(XacmlTypes.BOOLEAN.ofAny(true));

		when(combingingAlg.combine(contextCapture.capture(), ruleCapture.capture())).thenReturn(Decision.DENY);

		when(denyAdviceAttributeExp.evaluate(policyContext)).thenReturn(XacmlTypes.STRING.ofAny("testValue1"));
		when(denyObligationAttributeExp.evaluate(policyContext)).thenReturn(XacmlTypes.STRING.ofAny("testValue1"));

		InOrder order = inOrder(targetExpression, conditionExpression, combingingAlg, permitAdviceAttributeExp, denyAdviceAttributeExp, denyObligationAttributeExp, permitObligationAttributeExp, referenceResolver, handler);
		assertEquals(Decision.DENY, policy.evaluate(policyContext));
		order.verify(targetExpression).evaluate(policyContext);
		order.verify(conditionExpression).evaluate(policyContext);
		order.verify(combingingAlg).combine(contextCapture.capture(), ruleCapture.capture());
		order.verify(denyAdviceAttributeExp).evaluate(policyContext);
		order.verify(denyObligationAttributeExp).evaluate(policyContext);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(targetExpression, times(1)).evaluate(policyContext);
		verify(conditionExpression, times(1)).evaluate(policyContext);
		verify(combingingAlg, times(1)).combine(contextCapture.capture(), ruleCapture.capture());
		verify(denyAdviceAttributeExp, times(1)).evaluate(policyContext);
		verify(denyObligationAttributeExp, times(1)).evaluate(policyContext);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(targetExpression, conditionExpression, combingingAlg, permitAdviceAttributeExp, denyAdviceAttributeExp, denyObligationAttributeExp, permitObligationAttributeExp, referenceResolver, handler);
		assertSame(policy, policyContext.getCurrentPolicySet());
		assertSame(policyContext, contextCapture.getValue());

		assertEquals(1, Iterables.size(context.getMatchingAdvices(Decision.DENY)));
		assertEquals(1, Iterables.size(context.getMatchingObligations(Decision.DENY)));
		assertEquals(java.util.Collections.emptyList(), ruleCapture.getValue());
	}

	@Test
	public void testEvaluateTargetMatchConditionTrueCombiningAlgorithResultIsPermit() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);

		ArgumentCaptor<EvaluationContext> contextCapture = ArgumentCaptor.forClass(EvaluationContext.class);
		ArgumentCaptor<List<CompositeDecisionRule>> ruleCapture = ArgumentCaptor.forClass(List.class);

		when(targetExpression.evaluate(policyContext)).thenReturn(ModelFixtures.matchValues(MatchResult.MATCH));
		when(conditionExpression.evaluate(policyContext)).thenReturn(XacmlTypes.BOOLEAN.ofAny(true));

		when(combingingAlg.combine(contextCapture.capture(), ruleCapture.capture())).thenReturn(Decision.PERMIT);
		when(permitAdviceAttributeExp.evaluate(policyContext)).thenReturn(XacmlTypes.STRING.ofAny("testValue1"));
		when(permitObligationAttributeExp.evaluate(policyContext)).thenReturn(XacmlTypes.STRING.ofAny("testValue1"));

		InOrder order = inOrder(targetExpression, conditionExpression, combingingAlg, permitAdviceAttributeExp, denyAdviceAttributeExp, denyObligationAttributeExp, permitObligationAttributeExp, referenceResolver, handler);
		assertEquals(Decision.PERMIT, policy.evaluate(policyContext));
		order.verify(targetExpression).evaluate(policyContext);
		order.verify(conditionExpression).evaluate(policyContext);
		order.verify(combingingAlg).combine(contextCapture.capture(), ruleCapture.capture());
		order.verify(permitAdviceAttributeExp).evaluate(policyContext);
		order.verify(permitObligationAttributeExp).evaluate(policyContext);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(targetExpression, times(1)).evaluate(policyContext);
		verify(conditionExpression, times(1)).evaluate(policyContext);
		verify(combingingAlg, times(1)).combine(contextCapture.capture(), ruleCapture.capture());
		verify(permitAdviceAttributeExp, times(1)).evaluate(policyContext);
		verify(permitObligationAttributeExp, times(1)).evaluate(policyContext);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(targetExpression, conditionExpression, combingingAlg, permitAdviceAttributeExp, denyAdviceAttributeExp, denyObligationAttributeExp, permitObligationAttributeExp, referenceResolver, handler);
		assertSame(policy, policyContext.getCurrentPolicySet());
		assertSame(policyContext, contextCapture.getValue());
		assertEquals(1, Iterables.size(context.getMatchingAdvices(Decision.PERMIT)));
		assertEquals(1, Iterables.size(context.getMatchingObligations(Decision.PERMIT)));
		assertEquals(java.util.Collections.emptyList(), ruleCapture.getValue());
	}

	@Test
	public void testEvaluateTargetMatchConditionTrueCombiningAlgorithResultIsIndeterminate() throws EvaluationException
	{
		EvaluationContext policyContext = policy.createContext(context);

		ArgumentCaptor<EvaluationContext> contextCapture = ArgumentCaptor.forClass(EvaluationContext.class);
		ArgumentCaptor<List<CompositeDecisionRule>> ruleCapture = ArgumentCaptor.forClass(List.class);

		when(targetExpression.evaluate(policyContext)).thenReturn(ModelFixtures.matchValues(MatchResult.MATCH));
		when(conditionExpression.evaluate(policyContext)).thenReturn(XacmlTypes.BOOLEAN.ofAny(true));

		when(combingingAlg.combine(contextCapture.capture(), ruleCapture.capture())).thenReturn(Decision.INDETERMINATE);

		InOrder order = inOrder(targetExpression, conditionExpression, combingingAlg, permitAdviceAttributeExp, denyAdviceAttributeExp, denyObligationAttributeExp, permitObligationAttributeExp, referenceResolver, handler);
		assertEquals(Decision.INDETERMINATE, policy.evaluate(policyContext));
		order.verify(targetExpression).evaluate(policyContext);
		order.verify(conditionExpression).evaluate(policyContext);
		order.verify(combingingAlg).combine(contextCapture.capture(), ruleCapture.capture());
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(targetExpression, times(1)).evaluate(policyContext);
		verify(conditionExpression, times(1)).evaluate(policyContext);
		verify(combingingAlg, times(1)).combine(contextCapture.capture(), ruleCapture.capture());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(targetExpression, conditionExpression, combingingAlg, permitAdviceAttributeExp, denyAdviceAttributeExp, denyObligationAttributeExp, permitObligationAttributeExp, referenceResolver, handler);
		assertSame(policy, policyContext.getCurrentPolicySet());
		assertSame(policyContext, contextCapture.getValue());
		assertEquals(0, Iterables.size(context.getMatchingAdvices(Decision.DENY)));
		assertEquals(0, Iterables.size(context.getMatchingObligations(Decision.DENY)));
		assertEquals(0, Iterables.size(context.getMatchingAdvices(Decision.PERMIT)));
		assertEquals(0, Iterables.size(context.getMatchingObligations(Decision.PERMIT)));
		assertEquals(java.util.Collections.emptyList(), ruleCapture.getValue());
	}
}
