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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.AttributeAssignment;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRuleResponse;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ExpressionVisitor;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.ValueExpTypeInfo;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * Bead policy4j-the.4.54: obligations and advice of nested policies and
 * policy sets. A nested rule buffers its responses and publishes them to
 * its parent only for its own final decision. Real policies, policy sets,
 * combining algorithms, and contexts; only the reference resolver and one
 * failing expression are test doubles.
 */
public class NestedResponseStagingTest
{
	private static final String RULES = "urn:oasis:names:tc:xacml:";
	private static final String FIRST_APPLICABLE_RULES = RULES + "1.0:rule-combining-algorithm:first-applicable";
	private static final String PERMIT_OVERRIDES_RULES = RULES + "3.0:rule-combining-algorithm:permit-overrides";
	private static final String FIRST_APPLICABLE = RULES + "1.0:policy-combining-algorithm:first-applicable";
	private static final String PERMIT_OVERRIDES = RULES + "3.0:policy-combining-algorithm:permit-overrides";
	private static final String DENY_OVERRIDES = RULES + "3.0:policy-combining-algorithm:deny-overrides";

	private static final DecisionCombiningAlgorithmProvider ALGORITHMS =
			DecisionCombiningAlgorithmProvider.builder().withDefaultAlgorithms().build();

	/**
	 * 1. Enclosing permit-overrides; one child has an Indeterminate target
	 * and a Permit rule with an obligation; the sibling is Permit. Only the
	 * responses of the sibling are returned. The rule of the child is
	 * evaluated to find the extended Indeterminate value (XACML 3.0,
	 * section 7.14), with its obligation (section 7.18), which is never
	 * returned: the value of the child is Indeterminate.
	 */
	@Test
	public void childWithAnIndeterminateTargetAddsNoResponsesToThePermitOfItsSibling()
	{
		Policy indeterminate = Policy.builder("indeterminate").version("1.0")
				.target(ModelFixtures.target(new FailingExpression("target failed")))
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(FIRST_APPLICABLE_RULES))
				.rule(Rule.builder("indeterminate-rule", Effect.PERMIT)
						.obligation(obligation("indeterminate-rule-obligation", Effect.PERMIT, "indeterminate")))
				.obligation(obligation("indeterminate-obligation", Effect.PERMIT, "indeterminate"))
				.advice(advice("indeterminate-advice", Effect.PERMIT, "indeterminate"))
				.build();
		// Alone, the child is Indeterminate with the status of its target; an Indeterminate
		// result carries no responses.
		EvaluationContext alone = ModelFixtures.context(false);
		Decision childDecision = indeterminate.evaluate(indeterminate.createContext(alone));
		assertTrue(childDecision.isIndeterminate(), () -> "decision=" + childDecision);
		Status childStatus = alone.getEvaluationStatus().orElseThrow();
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, childStatus.getStatusCode().getValue());
		assertEquals("target failed", childStatus.getMessage().orElse(null));

		// The sibling's responses are the only ones of the Permit inside a policy set.

		PolicySet enclosing = set("enclosing", PERMIT_OVERRIDES, indeterminate, policy("sibling", Decision.PERMIT));
		EvaluationContext root = ModelFixtures.context(false);

		assertEquals(Decision.PERMIT, enclosing.evaluate(enclosing.createContext(root)));

		assertEquals(List.of("sibling-obligation"), ids(root.getMatchingObligations(Decision.PERMIT)));
		assertEquals(List.of("sibling-advice"), ids(root.getMatchingAdvices(Decision.PERMIT)));
		assertOrigins(root, Decision.PERMIT);
		assertEquals(List.of(), ids(root.getMatchingObligations(Decision.DENY)));
		assertEquals(List.of(), ids(root.getMatchingAdvices(Decision.DENY)));
	}

	/**
	 * 2. An enclosed policy whose own obligation expression fails is
	 * Indeterminate. The status of the failure is kept, and the responses
	 * that its rule had buffered are not published.
	 */
	@Test
	public void enclosedPolicyWhoseObligationFailsIsIndeterminateAndPublishesNothing()
	{
		Policy failing = Policy.builder("failing").version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(FIRST_APPLICABLE_RULES))
				.rule(Rule.builder("failing-rule", Effect.PERMIT)
						.obligation(obligation("failing-rule-obligation", Effect.PERMIT, "rule"))
						.advice(advice("failing-rule-advice", Effect.PERMIT, "rule")))
				.obligation(ObligationExpression.builder("failing-obligation", Effect.PERMIT)
						.attribute("origin", new FailingExpression("assignment failed")))
				.build();
		PolicySet enclosing = set("enclosing", FIRST_APPLICABLE, failing, policy("sibling", Decision.PERMIT));
		EvaluationContext root = ModelFixtures.context(false);

		Decision decision = enclosing.evaluate(enclosing.createContext(root));

		assertTrue(decision.isIndeterminate(), () -> "decision=" + decision);
		Status status = root.getEvaluationStatus().orElseThrow();
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, status.getStatusCode().getValue());
		assertEquals("assignment failed", status.getMessage().orElse(null));
		for (Decision d : new Decision[] {Decision.PERMIT, Decision.DENY}) {
			assertEquals(List.of(), ids(root.getMatchingObligations(d)), "obligations for " + d);
			assertEquals(List.of(), ids(root.getMatchingAdvices(d)), "advice for " + d);
		}
	}

	/**
	 * 3. A PolicyIdReference and a PolicySetIdReference below a nested
	 * policy set. Both are resolved once, and their responses arrive at the
	 * top once, together with the responses of the sets around them.
	 */
	@Test
	public void referencedPolicyAndPolicySetPublishTheirResponsesThroughNestedSets()
	{
		Policy referenced = policy("referenced", Decision.PERMIT);
		PolicySet referencedSet = PolicySet.builder("referenced-set").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(FIRST_APPLICABLE))
				.policy(policy("inner", Decision.PERMIT))
				.obligation(obligation("referenced-set-obligation", Effect.PERMIT, "referenced-set"))
				.build();
		PolicyIDReference policyReference = PolicyIDReference.builder("referenced").build();
		PolicySetIDReference setReference = PolicySetIDReference.builder("referenced-set").build();
		PolicyReferenceResolver resolver = mock(PolicyReferenceResolver.class);
		when(resolver.resolve(policyReference)).thenReturn(referenced);
		when(resolver.resolve(setReference)).thenReturn(referencedSet);
		EvaluationContextHandler handler = mock(EvaluationContextHandler.class, invocation -> {
			throw new AssertionError("Unexpected attribute resolution: " + invocation.getMethod());
		});
		EvaluationContext root = new RootEvaluationContext(false, Duration.ZERO, resolver, handler);
		// deny-overrides evaluates every child, so both references are used.
		PolicySet nested = PolicySet.builder("nested").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(DENY_OVERRIDES))
				.compositeDecisionRules(policyReference, setReference)
				.obligation(obligation("nested-obligation", Effect.PERMIT, "nested"))
				.build();
		PolicySet enclosing = PolicySet.builder("enclosing").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(DENY_OVERRIDES))
				.policySet(nested)
				.policy(policy("sibling", Decision.NOT_APPLICABLE))
				.build();

		assertEquals(Decision.PERMIT, enclosing.evaluate(enclosing.createContext(root)));

		assertEquals(List.of("inner-obligation", "nested-obligation", "referenced-obligation",
				"referenced-set-obligation"), ids(root.getMatchingObligations(Decision.PERMIT)));
		assertEquals(List.of("inner-advice", "referenced-advice"), ids(root.getMatchingAdvices(Decision.PERMIT)));
		assertOrigins(root, Decision.PERMIT);
		assertEquals(List.of(), ids(root.getMatchingObligations(Decision.DENY)));
		assertEquals(List.of(), ids(root.getMatchingAdvices(Decision.DENY)));
		verify(resolver, times(1)).resolve(policyReference);
		verify(resolver, times(1)).resolve(setReference);
	}

	/**
	 * 4. A rule whose effect differs from the decision of its policy. The
	 * policy is Permit by permit-overrides; its Deny rule has a Deny
	 * obligation. A later sibling is Deny and decides the enclosing set.
	 * The Deny obligation of that rule is not part of the result.
	 */
	@Test
	public void ruleResponseForAnotherEffectDoesNotReachTheDecisionOfALaterSibling()
	{
		Policy mixed = Policy.builder("mixed").version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(PERMIT_OVERRIDES_RULES))
				.rule(Rule.builder("mixed-deny-rule", Effect.DENY)
						.obligation(obligation("mixed-deny-rule-obligation", Effect.DENY, "mixed"))
						.advice(advice("mixed-deny-rule-advice", Effect.DENY, "mixed")))
				.rule(Rule.builder("mixed-permit-rule", Effect.PERMIT)
						.obligation(obligation("mixed-permit-rule-obligation", Effect.PERMIT, "mixed")))
				.build();
		PolicySet enclosing = set("enclosing", DENY_OVERRIDES, mixed, policy("sibling", Decision.DENY));
		EvaluationContext root = ModelFixtures.context(false);

		assertEquals(Decision.DENY, enclosing.evaluate(enclosing.createContext(root)));

		assertEquals(List.of("sibling-obligation"), ids(root.getMatchingObligations(Decision.DENY)));
		assertEquals(List.of("sibling-advice"), ids(root.getMatchingAdvices(Decision.DENY)));
		// The Permit policy did publish its Permit obligation: the outermost set is not staged.
		assertEquals(List.of("mixed-permit-rule-obligation"), ids(root.getMatchingObligations(Decision.PERMIT)));
		assertEquals(List.of(), ids(root.getMatchingAdvices(Decision.PERMIT)));

		// The same set one level down: it is staged, so it publishes only the responses
		// for its own decision. The Permit obligation of the overruled policy stays behind.
		PolicySet outer = PolicySet.builder("outer").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(FIRST_APPLICABLE))
				.policySet(enclosing)
				.build();
		EvaluationContext outerRoot = ModelFixtures.context(false);

		assertEquals(Decision.DENY, outer.evaluate(outer.createContext(outerRoot)));

		assertEquals(List.of("sibling-obligation"), ids(outerRoot.getMatchingObligations(Decision.DENY)));
		assertEquals(List.of("sibling-advice"), ids(outerRoot.getMatchingAdvices(Decision.DENY)));
		assertEquals(List.of(), ids(outerRoot.getMatchingObligations(Decision.PERMIT)));
		assertEquals(List.of(), ids(outerRoot.getMatchingAdvices(Decision.PERMIT)));
	}

	/**
	 * 5. Four policy sets nested in each other, around one policy. Every
	 * level adds one obligation and one advice for Permit. Each arrives
	 * exactly once, with its own value.
	 */
	@Test
	public void fourLevelsOfNestingPublishEveryResponseExactlyOnce()
	{
		Policy leaf = policy("leaf", Decision.PERMIT);
		PolicySet level4 = level("level4", leaf);
		PolicySet level3 = level("level3", level4);
		PolicySet level2 = level("level2", level3);
		PolicySet level1 = level("level1", level2);
		EvaluationContext root = ModelFixtures.context(false);

		assertEquals(Decision.PERMIT, level1.evaluate(level1.createContext(root)));

		assertEquals(List.of("leaf-obligation", "level1-obligation", "level2-obligation", "level3-obligation",
				"level4-obligation"), ids(root.getMatchingObligations(Decision.PERMIT)));
		assertEquals(List.of("leaf-advice", "level1-advice", "level2-advice", "level3-advice", "level4-advice"),
				ids(root.getMatchingAdvices(Decision.PERMIT)));
		assertOrigins(root, Decision.PERMIT);
		assertEquals(List.of(), ids(root.getMatchingObligations(Decision.DENY)));
		assertEquals(List.of(), ids(root.getMatchingAdvices(Decision.DENY)));
	}

	/** Every obligation and advice for the decision names the policy or policy set that declared it. */
	private static void assertOrigins(EvaluationContext context, Decision decision)
	{
		for (DecisionRuleResponse response : context.getMatchingObligations(decision)) {
			assertOrigin(response);
		}
		for (DecisionRuleResponse response : context.getMatchingAdvices(decision)) {
			assertOrigin(response);
		}
	}

	/** The single "origin" assignment of a response names the policy or policy set that declared it. */
	private static void assertOrigin(DecisionRuleResponse response)
	{
		Collection<AttributeAssignment> origin = response.getAttribute("origin");
		assertEquals(1, origin.size(), response.getId());
		assertEquals(1, response.getAttributes().size(), response.getId());
		String declaredBy = response.getId().replaceFirst("-(obligation|advice)$", "");
		assertEquals(StringVal.of(declaredBy), origin.iterator().next().getAttribute(), response.getId());
	}

	private static PolicySet level(String id, CompositeDecisionRule child)
	{
		return PolicySet.builder(id).version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(FIRST_APPLICABLE))
				.compositeDecisionRules(child)
				.obligation(obligation(id + "-obligation", Effect.PERMIT, id))
				.advice(advice(id + "-advice", Effect.PERMIT, id))
				.build();
	}

	private static PolicySet set(String id, String algorithmId, Policy... children)
	{
		PolicySet.Builder builder = PolicySet.builder(id).version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(algorithmId));
		for (Policy child : children) {
			builder.policy(child);
		}
		return builder.build();
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
				.obligation(obligation(id + "-obligation", effect, id))
				.advice(advice(id + "-advice", effect, id))
				.build();
	}

	private static ObligationExpression.Builder obligation(String id, Effect effect, String origin)
	{
		return ObligationExpression.builder(id, effect).attribute("origin", StringVal.of(origin));
	}

	private static AdviceExpression.Builder advice(String id, Effect effect, String origin)
	{
		return AdviceExpression.builder(id, effect).attribute("origin", StringVal.of(origin));
	}

	/** The ids of the responses, sorted, with duplicates kept. */
	private static List<String> ids(Collection<? extends DecisionRuleResponse> responses)
	{
		List<String> ids = new ArrayList<>();
		for (DecisionRuleResponse response : responses) {
			ids.add(response.getId());
		}
		Collections.sort(ids);
		return ids;
	}

	/** A string expression whose evaluation fails with processing-error and the given message. */
	private static final class FailingExpression implements Expression
	{
		private final String message;

		FailingExpression(String message) {
			this.message = message;
		}

		@Override
		public ValueExpTypeInfo getEvaluatesTo() {
			return XacmlTypes.STRING;
		}

		@Override
		public <T extends Expression> T evaluate(EvaluationContext context) {
			throw new EvaluationException(Status.processingError().message(message).build(), message);
		}

		@Override
		public void accept(ExpressionVisitor visitor) {
		}
	}
}
