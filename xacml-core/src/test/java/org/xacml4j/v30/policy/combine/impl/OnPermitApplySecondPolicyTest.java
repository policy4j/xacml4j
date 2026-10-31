package org.xacml4j.v30.policy.combine.impl;

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

import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRuleResponse;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.policy.AdviceExpression;
import org.xacml4j.v30.policy.Condition;
import org.xacml4j.v30.policy.DecisionCombiningAlgorithm;
import org.xacml4j.v30.policy.ModelFixtures;
import org.xacml4j.v30.policy.ObligationExpression;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.policy.Rule;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProviderBuilder;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.StringVal;


@ExtendWith(MockitoExtension.class)
public class OnPermitApplySecondPolicyTest
{
	private static final String FIRST_APPLICABLE_RULES =
			"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable";
	private static final String FIRST_APPLICABLE_POLICIES =
			"urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:first-applicable";
	private static final String PERMIT_OVERRIDES_POLICIES =
			"urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-overrides";

	private static final DecisionCombiningAlgorithmProvider ALGORITHMS =
			DecisionCombiningAlgorithmProviderBuilder.builder().withDefaultAlgorithms().build();

	private List<CompositeDecisionRule> decisions;
	private DecisionCombiningAlgorithm<CompositeDecisionRule> algorithm;
	private EvaluationContext context;

	@BeforeEach
	public void init(){
		this.decisions = new LinkedList<>();
		this.algorithm = new OnPermitApplySecondPolicyCombiningAlgorithm();
		this.context = mock(EvaluationContext.class);
	}

	@Test
	public void testCombineWithNoDecisions()
	{
		doNothing().when(context).setEvaluationStatus(Status.processingError().build());
		InOrder order = inOrder(context);
		assertEquals(Decision.INDETERMINATE_DP, algorithm.combine(context, decisions));
		order.verify(context).setEvaluationStatus(Status.processingError().build());
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(context, times(1)).setEvaluationStatus(Status.processingError().build());
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context);
	}

	@Test
	public void testCombineFirstPermitSecondNotApplicable() throws EvaluationException
	{
		CompositeDecisionRule r1 = mock(CompositeDecisionRule.class);
		CompositeDecisionRule r2 = mock(CompositeDecisionRule.class);

		decisions = new LinkedList<CompositeDecisionRule>();
		decisions.add(r1);
		decisions.add(r2);

		when(r1.createContext(context)).thenReturn(context);
		when(r1.evaluate(context)).thenReturn(Decision.PERMIT);
		when(r2.createContext(context)).thenReturn(context);
		when(r2.evaluate(context)).thenReturn(Decision.NOT_APPLICABLE);
		InOrder order = inOrder(context, r1, r2);
		assertEquals(Decision.NOT_APPLICABLE, algorithm.combine(context, decisions));
		order.verify(r1).createContext(context);
		order.verify(r1).evaluate(context);
		order.verify(r2).createContext(context);
		order.verify(r2).evaluate(context);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(r1, times(1)).createContext(context);
		verify(r1, times(1)).evaluate(context);
		verify(r2, times(1)).createContext(context);
		verify(r2, times(1)).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, r1, r2);
	}

	@Test
	public void testCombineFirstPermitSecondPermit() throws EvaluationException
	{
		CompositeDecisionRule r1 = mock(CompositeDecisionRule.class);
		CompositeDecisionRule r2 = mock(CompositeDecisionRule.class);

		decisions = new LinkedList<CompositeDecisionRule>();
		decisions.add(r1);
		decisions.add(r2);

		when(r1.createContext(context)).thenReturn(context);
		when(r1.evaluate(context)).thenReturn(Decision.PERMIT);
		when(r2.createContext(context)).thenReturn(context);
		when(r2.evaluate(context)).thenReturn(Decision.PERMIT);
		InOrder order = inOrder(context, r1, r2);
		assertEquals(Decision.PERMIT, algorithm.combine(context, decisions));
		order.verify(r1).createContext(context);
		order.verify(r1).evaluate(context);
		order.verify(r2).createContext(context);
		order.verify(r2).evaluate(context);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(r1, times(1)).createContext(context);
		verify(r1, times(1)).evaluate(context);
		verify(r2, times(1)).createContext(context);
		verify(r2, times(1)).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, r1, r2);
	}

	@Test
	public void testCombineFirstPermitSecondDeny() throws EvaluationException
	{
		CompositeDecisionRule r1 = mock(CompositeDecisionRule.class);
		CompositeDecisionRule r2 = mock(CompositeDecisionRule.class);

		decisions = new LinkedList<CompositeDecisionRule>();
		decisions.add(r1);
		decisions.add(r2);

		when(r1.createContext(context)).thenReturn(context);
		when(r1.evaluate(context)).thenReturn(Decision.PERMIT);
		when(r2.createContext(context)).thenReturn(context);
		when(r2.evaluate(context)).thenReturn(Decision.DENY);
		InOrder order = inOrder(context, r1, r2);
		assertEquals(Decision.DENY, algorithm.combine(context, decisions));
		order.verify(r1).createContext(context);
		order.verify(r1).evaluate(context);
		order.verify(r2).createContext(context);
		order.verify(r2).evaluate(context);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(r1, times(1)).createContext(context);
		verify(r1, times(1)).evaluate(context);
		verify(r2, times(1)).createContext(context);
		verify(r2, times(1)).evaluate(context);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(context, r1, r2);
	}

	// CS01 2.1: "Obligations and advice MUST be combined as described in [XACML3]". XACML 3.0 Core 7.18
	// returns obligations and advice only from the paths where every level has the returned decision.
	// The tests below use real policies and read the result the way the PDP does: the root policy set is
	// evaluated in a root context, then the obligations and advice that match the decision are taken.

	@Test
	public void permitFirstChildAndPermitSecondChildKeepTheirObligationsAndAdvice()
	{
		PolicySet conditional = onPermitApplySecond("conditional",
				policy("gate", Decision.PERMIT), policy("selected", Decision.PERMIT));
		EvaluationContext root = ModelFixtures.context(false);
		assertEquals(Decision.PERMIT, conditional.evaluate(conditional.createContext(root)));
		assertEquals(Set.of("gate-obligation", "selected-obligation"),
				ids(root.getMatchingObligations(Decision.PERMIT)));
		assertEquals(Set.of("gate-advice", "selected-advice"), ids(root.getMatchingAdvices(Decision.PERMIT)));
	}

	@Test
	public void permitFirstChildIsNotReturnedWithADenySecondChild()
	{
		PolicySet conditional = onPermitApplySecond("conditional",
				policy("gate", Decision.PERMIT), policy("selected", Decision.DENY));
		EvaluationContext root = ModelFixtures.context(false);
		assertEquals(Decision.DENY, conditional.evaluate(conditional.createContext(root)));
		assertEquals(Set.of("selected-obligation"), ids(root.getMatchingObligations(Decision.DENY)));
		assertEquals(Set.of("selected-advice"), ids(root.getMatchingAdvices(Decision.DENY)));
	}

	@Test
	public void denyFirstChildAndDenyThirdChildKeepTheirObligationsAndAdviceWithoutTheSecondChild()
	{
		// The second child would add its Deny obligation and advice if it were evaluated.
		PolicySet conditional = onPermitApplySecond("conditional", policy("gate", Decision.DENY),
				policy("unselected", Decision.DENY), policy("selected", Decision.DENY));
		EvaluationContext root = ModelFixtures.context(false);
		assertEquals(Decision.DENY, conditional.evaluate(conditional.createContext(root)));
		assertEquals(Set.of("gate-obligation", "selected-obligation"),
				ids(root.getMatchingObligations(Decision.DENY)));
		assertEquals(Set.of("gate-advice", "selected-advice"), ids(root.getMatchingAdvices(Decision.DENY)));
	}

	@Test
	public void denyFirstChildIsNotReturnedWithAPermitThirdChild()
	{
		// The second child would add its Permit obligation and advice if it were evaluated.
		PolicySet conditional = onPermitApplySecond("conditional", policy("gate", Decision.DENY),
				policy("unselected", Decision.PERMIT), policy("selected", Decision.PERMIT));
		EvaluationContext root = ModelFixtures.context(false);
		assertEquals(Decision.PERMIT, conditional.evaluate(conditional.createContext(root)));
		assertEquals(Set.of("selected-obligation"), ids(root.getMatchingObligations(Decision.PERMIT)));
		assertEquals(Set.of("selected-advice"), ids(root.getMatchingAdvices(Decision.PERMIT)));
	}

	@Test
	public void unselectedSecondChildDoesNotAddObligationsToALaterSiblingDecision()
	{
		// CS01 2.1: a Deny first child with two children is NotApplicable and the second child is not evaluated.
		PolicySet conditional = onPermitApplySecond("conditional",
				policy("gate", Decision.DENY), policy("unselected", Decision.PERMIT));
		PolicySet enclosing = enclosing(PERMIT_OVERRIDES_POLICIES, conditional, policy("sibling", Decision.PERMIT));
		EvaluationContext root = ModelFixtures.context(false);
		assertEquals(Decision.PERMIT, enclosing.evaluate(enclosing.createContext(root)));
		assertEquals(Set.of("sibling-obligation"), ids(root.getMatchingObligations(Decision.PERMIT)));
		assertEquals(Set.of("sibling-advice"), ids(root.getMatchingAdvices(Decision.PERMIT)));
	}

	/**
	 * Reproducer for the shared evaluation contexts, not for the combining algorithm: the first child is
	 * Permit, the selected second child is NotApplicable, so the policy set is NotApplicable and nothing
	 * from inside it may be returned (XACML 3.0 Core 7.18). A child context sends its obligations and
	 * advice to the root context at once, and no enclosing policy set takes them back.
	 */
	@Test
	public void discardedFirstChildDoesNotAddObligationsToALaterSiblingDecision()
	{
		PolicySet conditional = onPermitApplySecond("conditional",
				policy("gate", Decision.PERMIT), policy("selected", Decision.NOT_APPLICABLE));
		PolicySet enclosing = enclosing(FIRST_APPLICABLE_POLICIES, conditional, policy("sibling", Decision.PERMIT));
		EvaluationContext root = ModelFixtures.context(false);
		assertEquals(Decision.PERMIT, enclosing.evaluate(enclosing.createContext(root)));
		assertEquals(Set.of("sibling-obligation"), ids(root.getMatchingObligations(Decision.PERMIT)));
		assertEquals(Set.of("sibling-advice"), ids(root.getMatchingAdvices(Decision.PERMIT)));
	}

	@Test
	public void discardedDenyChildDoesNotAddResponsesToALaterDenySibling()
	{
		PolicySet conditional = onPermitApplySecond("conditional",
				policy("gate", Decision.DENY), policy("unselected", Decision.PERMIT));
		PolicySet enclosing = enclosing(FIRST_APPLICABLE_POLICIES, conditional, policy("sibling", Decision.DENY));
		EvaluationContext root = ModelFixtures.context(false);
		assertEquals(Decision.DENY, enclosing.evaluate(enclosing.createContext(root)));
		assertEquals(Set.of("sibling-obligation"), ids(root.getMatchingObligations(Decision.DENY)));
		assertEquals(Set.of("sibling-advice"), ids(root.getMatchingAdvices(Decision.DENY)));
	}

	@Test
	public void nestedPolicySetPublishesItsOwnMatchingResponsesWithTheSelectedChild()
	{
		PolicySet conditional = PolicySet.builder("conditional").version("1.0")
				.withCombiningAlgorithm(algorithm)
				.policy(policy("gate", Decision.DENY))
				.policy(policy("unselected", Decision.DENY))
				.policy(policy("selected", Decision.PERMIT))
				.obligation(ObligationExpression.builder("parent-obligation", Effect.PERMIT)
						.attribute("origin", StringVal.of("parent")))
				.advice(AdviceExpression.builder("parent-advice", Effect.PERMIT)
						.attribute("origin", StringVal.of("parent")))
				.build();
		PolicySet enclosing = enclosing(FIRST_APPLICABLE_POLICIES, conditional, policy("sibling", Decision.DENY));
		EvaluationContext root = ModelFixtures.context(false);
		assertEquals(Decision.PERMIT, enclosing.evaluate(enclosing.createContext(root)));
		assertEquals(Set.of("parent-obligation", "selected-obligation"), ids(root.getMatchingObligations(Decision.PERMIT)));
		assertEquals(Set.of("parent-advice", "selected-advice"), ids(root.getMatchingAdvices(Decision.PERMIT)));
		assertEquals(Set.of(), ids(root.getMatchingObligations(Decision.DENY)));
		assertEquals(Set.of(), ids(root.getMatchingAdvices(Decision.DENY)));
	}

	private PolicySet onPermitApplySecond(String id, Policy... children)
	{
		PolicySet.Builder builder = PolicySet.builder(id).version("1.0").withCombiningAlgorithm(algorithm);
		for (Policy child : children) {
			builder.policy(child);
		}
		return builder.build();
	}

	private static PolicySet enclosing(String algorithmId, PolicySet first, Policy sibling)
	{
		return PolicySet.builder("enclosing").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(algorithmId))
				.policySet(first).policy(sibling).build();
	}

	/** A policy that is Permit, Deny or NotApplicable, with one obligation and one advice for its effect. */
	private static Policy policy(String id, Decision decision)
	{
		Effect effect = decision == Decision.DENY ? Effect.DENY : Effect.PERMIT;
		Rule.Builder rule = Rule.builder(id + "-rule", effect);
		if (decision == Decision.NOT_APPLICABLE) {
			rule.condition(new Condition(BooleanVal.of(false)));
		}
		return Policy.builder(id).version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(FIRST_APPLICABLE_RULES))
				.rule(rule.build())
				.obligation(ObligationExpression.builder(id + "-obligation", effect)
						.attribute("origin", StringVal.of(id)))
				.advice(AdviceExpression.builder(id + "-advice", effect)
						.attribute("origin", StringVal.of(id)))
				.build();
	}

	private static Set<String> ids(Collection<? extends DecisionRuleResponse> responses)
	{
		Set<String> ids = new TreeSet<>();
		for (DecisionRuleResponse response : responses) {
			ids.add(response.getId());
		}
		return ids;
	}
}
