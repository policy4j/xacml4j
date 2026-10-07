package org.xacml4j.v30.policy.combine;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2026 Xacml4J.org
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import com.google.common.truth.Truth;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xacml4j.v30.policy.DecisionCombiningAlgorithm;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.ValueExpTypeInfo;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.XacmlTypes;
import org.xacml4j.v30.policy.ModelFixtures;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.policy.PolicyTreeVisitor;
import org.xacml4j.v30.policy.Rule;
import org.xacml4j.v30.policy.combine.impl.DefaultXacml30DecisionCombiningAlgorithms;

public class DecisionCombiningAlgorithmRegistryTest
{
	private static final DecisionCombiningAlgorithmProvider DEFAULT_ALGORITHMS =
			DecisionCombiningAlgorithmProviderBuilder.builder().withDefaultAlgorithms().build();

	// XACML 3.0 Additional Combining Algorithms Profile 1.0, CS01 2.1: a policy-combining algorithm.
	// https://docs.oasis-open.org/xacml/xacml-3.0-combalgs/v1.0/cs01/xacml-3.0-combalgs-v1.0-cs01.html
	private static final String ON_PERMIT_APPLY_SECOND =
			"urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:on-permit-apply-second";

	/** The identifiers that the default provider registered at a8a90d46: 11 rule and 12 policy algorithms. */
	public static Stream<Arguments> algorithmIdentifiers()
	{
		return Stream.of(
				Arguments.of("rule", "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:deny-overrides"),
				Arguments.of("rule", "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:permit-overrides"),
				Arguments.of("rule", "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable"),
				Arguments.of("rule", "urn:oasis:names:tc:xacml:1.1:rule-combining-algorithm:ordered-deny-overrides"),
				Arguments.of("rule", "urn:oasis:names:tc:xacml:1.1:rule-combining-algorithm:ordered-permit-overrides"),
				Arguments.of("rule", "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides"),
				Arguments.of("rule", "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides"),
				Arguments.of("rule", "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-deny-overrides"),
				Arguments.of("rule", "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-permit-overrides"),
				Arguments.of("rule", "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit"),
				Arguments.of("rule", "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-unless-deny"),
				Arguments.of("policy", "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:deny-overrides"),
				Arguments.of("policy", "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:permit-overrides"),
				Arguments.of("policy", "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:first-applicable"),
				Arguments.of("policy", "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:only-one-applicable"),
				Arguments.of("policy", "urn:oasis:names:tc:xacml:1.1:policy-combining-algorithm:ordered-deny-overrides"),
				Arguments.of("policy", "urn:oasis:names:tc:xacml:1.1:policy-combining-algorithm:ordered-permit-overrides"),
				Arguments.of("policy", "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-overrides"),
				Arguments.of("policy", "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-overrides"),
				Arguments.of("policy", "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:ordered-deny-overrides"),
				Arguments.of("policy", "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:ordered-permit-overrides"),
				Arguments.of("policy", "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-unless-permit"),
				Arguments.of("policy", "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-unless-deny"));
	}

	public static Stream<Arguments> algorithmFingerprints()
	{
		return algorithmIdentifiers().flatMap(identifierArguments -> {
			Object[] identifier = identifierArguments.get();
			String kind = (String) identifier[0];
			String id = (String) identifier[1];
			return Stream.of(
					fingerprint(kind, id, "NotApplicable-only", List.of(Decision.NOT_APPLICABLE)),
					fingerprint(kind, id, "Permit+Deny", List.of(Decision.PERMIT, Decision.DENY)),
					fingerprint(kind, id, "Deny+Indeterminate{D}",
							List.of(Decision.DENY, Decision.INDETERMINATE_D)),
					fingerprint(kind, id, "Permit+Indeterminate{D}",
							List.of(Decision.PERMIT, Decision.INDETERMINATE_D)));
		});
	}

	private static Arguments fingerprint(String kind, String id, String vector, List<Decision> children)
	{
		return Arguments.of(kind, id, vector, children, appendixCExpected(id, vector));
	}

	private static Decision appendixCExpected(String identifier, String vector)
	{
		boolean legacy = identifier.contains(":1.0:") || identifier.contains(":1.1:");
		boolean policy = identifier.contains(":policy-combining-algorithm:");
		if ("NotApplicable-only".equals(vector)) {
			if (identifier.endsWith("deny-unless-permit")) {
				return Decision.DENY;
			}
			if (identifier.endsWith("permit-unless-deny")) {
				return Decision.PERMIT;
			}
			return Decision.NOT_APPLICABLE;
		}
		if (identifier.endsWith("only-one-applicable")) {
			return Decision.INDETERMINATE;
		}
		if (legacy && policy && ("Deny+Indeterminate{D}".equals(vector)
				|| "Permit+Indeterminate{D}".equals(vector) && identifier.contains("deny-overrides"))) {
			return Decision.DENY;
		}
		if (legacy && !policy && identifier.contains("permit-overrides")
				&& "Deny+Indeterminate{D}".equals(vector)) {
			return Decision.DENY;
		}
		if (identifier.endsWith("first-applicable")) {
			return "Permit+Deny".equals(vector) || "Permit+Indeterminate{D}".equals(vector)
					? Decision.PERMIT : Decision.DENY;
		}
		if (identifier.endsWith("deny-unless-permit")) {
			return "Permit+Deny".equals(vector) || "Permit+Indeterminate{D}".equals(vector)
					? Decision.PERMIT : Decision.DENY;
		}
		if (identifier.endsWith("permit-unless-deny")) {
			return "Deny+Indeterminate{D}".equals(vector) || "Permit+Deny".equals(vector)
					? Decision.DENY : Decision.PERMIT;
		}
		if (identifier.contains("permit-overrides")) {
			if ("Permit+Deny".equals(vector) || "Permit+Indeterminate{D}".equals(vector)) {
				return Decision.PERMIT;
			}
			return legacy ? Decision.INDETERMINATE : Decision.DENY;
		}
		if ("Permit+Deny".equals(vector) || "Deny+Indeterminate{D}".equals(vector)) {
			return Decision.DENY;
		}
		return legacy ? Decision.INDETERMINATE : Decision.INDETERMINATE_DP;
	}

	@ParameterizedTest(name = "{1} on {2}")
	@MethodSource("algorithmFingerprints")
	public void everyAlgorithmUrnMatchesAppendixCFingerprint(String kind, String identifier,
			String vector, List<Decision> children, Decision expected)
	{
		Decision actual;
		if ("rule".equals(kind)) {
			Policy holder = Policy.builder("fingerprint-holder").version("1.0")
					.combiningAlgorithm(DEFAULT_ALGORITHMS.getRuleAlgorithm(
							"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable"))
					.build();
			List<Rule> rules = new ArrayList<>();
			for (int index = 0; index < children.size(); index++) {
				rules.add(rule("fingerprint-rule-" + index, children.get(index)));
			}
			actual = DEFAULT_ALGORITHMS.getRuleAlgorithm(identifier)
					.combine(holder.createContext(ModelFixtures.context(false)), rules);
		} else {
			PolicySet holder = PolicySet.builder("fingerprint-holder").version("1.0")
					.withCombiningAlgorithm(DEFAULT_ALGORITHMS.getPolicyAlgorithm(
							"urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:first-applicable"))
					.build();
			List<org.xacml4j.v30.CompositeDecisionRule> policies = new ArrayList<>();
			for (int index = 0; index < children.size(); index++) {
				Decision childDecision = children.get(index);
				Policy policy = Policy.builder("fingerprint-policy-" + index).version("1.0")
						.combiningAlgorithm(DEFAULT_ALGORITHMS.getRuleAlgorithm(
								"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable"))
						.rule(rule("fingerprint-child-rule-" + index, childDecision)).build();
				policies.add(policy);
			}
			actual = DEFAULT_ALGORITHMS.getPolicyAlgorithm(identifier)
					.combine(holder.createContext(ModelFixtures.context(false)), policies);
		}
		assertEquals(expected, actual, identifier + " / " + vector);
	}

	@ParameterizedTest(name = "{0} algorithm resolves: {1}")
	@MethodSource("algorithmIdentifiers")
	public void resolvesStandardAlgorithmIdentifier(String kind, String identifier)
	{
		DecisionCombiningAlgorithm<?> algorithm = "policy".equals(kind)
				? DEFAULT_ALGORITHMS.getPolicyAlgorithm(identifier)
				: DEFAULT_ALGORITHMS.getRuleAlgorithm(identifier);

		assertNotNull(algorithm, "No combining algorithm for " + identifier);
		Truth.assertThat(algorithm.getId()).isEqualTo(identifier);
		List<Decision> children;
		Decision expected;
		if (identifier.endsWith("first-applicable")) {
			children = List.of(Decision.NOT_APPLICABLE, Decision.DENY);
			expected = Decision.DENY;
		} else if (identifier.endsWith("only-one-applicable")) {
			children = List.of(Decision.PERMIT, Decision.DENY);
			expected = Decision.INDETERMINATE;
		} else if (identifier.endsWith("deny-unless-permit")) {
			children = List.of(Decision.PERMIT, Decision.DENY);
			expected = Decision.PERMIT;
		} else if (identifier.endsWith("permit-unless-deny")) {
			children = List.of(Decision.DENY, Decision.PERMIT);
			expected = Decision.DENY;
		} else if (identifier.contains("permit-overrides")) {
			children = List.of(Decision.DENY, Decision.PERMIT);
			expected = Decision.PERMIT;
		} else {
			children = List.of(Decision.PERMIT, Decision.DENY);
			expected = Decision.DENY;
		}
		Decision actual;
		if ("rule".equals(kind)) {
			Policy holder = Policy.builder("registry-holder").version("1.0")
					.combiningAlgorithm(DEFAULT_ALGORITHMS.getRuleAlgorithm(
							"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable"))
					.build();
			EvaluationContext context = holder.createContext(ModelFixtures.context(false));
			List<Rule> rules = new ArrayList<>();
			for (int i = 0; i < children.size(); i++) {
				rules.add(rule("child-" + i, children.get(i)));
			}
			@SuppressWarnings("unchecked")
			DecisionCombiningAlgorithm<Rule> ruleAlgorithm = (DecisionCombiningAlgorithm<Rule>) algorithm;
			actual = ruleAlgorithm.combine(context, rules);
		} else {
			PolicySet holder = PolicySet.builder("registry-holder").version("1.0")
					.withCombiningAlgorithm(DEFAULT_ALGORITHMS.getPolicyAlgorithm(
							"urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:first-applicable"))
					.build();
			EvaluationContext context = holder.createContext(ModelFixtures.context(false));
			List<Policy> policies = new ArrayList<>();
			for (int i = 0; i < children.size(); i++) {
				Policy policy = Policy.builder("child-" + i).version("1.0")
						.combiningAlgorithm(DEFAULT_ALGORITHMS.getRuleAlgorithm(
								"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable"))
						.rule(rule("rule-" + i, children.get(i))).build();
				policies.add(policy);
			}
			@SuppressWarnings("unchecked")
			DecisionCombiningAlgorithm<org.xacml4j.v30.CompositeDecisionRule> policyAlgorithm =
					(DecisionCombiningAlgorithm<org.xacml4j.v30.CompositeDecisionRule>) algorithm;
			actual = policyAlgorithm.combine(context,
					new ArrayList<org.xacml4j.v30.CompositeDecisionRule>(policies));
		}
		assertEquals(expected, actual, "Decision fingerprint for " + identifier);
	}

	private static Rule rule(String id, Decision decision)
	{
		if (decision == Decision.INDETERMINATE_D) {
			return indeterminateRule(id, Effect.DENY);
		}
		if (decision == Decision.INDETERMINATE_P) {
			return indeterminateRule(id, Effect.PERMIT);
		}
		Rule.Builder builder = Rule.builder(id, decision == Decision.DENY ? org.xacml4j.v30.Effect.DENY
				: org.xacml4j.v30.Effect.PERMIT);
		if (decision == Decision.NOT_APPLICABLE) {
			builder.condition(new org.xacml4j.v30.policy.Condition(BooleanVal.of(false)));
		}
		return builder.build();
	}

	@Test
	public void legacyAndXacml30OverrideAlgorithmsDistinguishExtendedIndeterminateVectors()
	{
		String v10DenyId = "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:deny-overrides";
		String v10PermitId = "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:permit-overrides";
		String v30DenyId = "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides";
		String v30PermitId = "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides";
		Rule indeterminateD = indeterminateRule("indeterminate-d", Effect.DENY);
		Rule indeterminateP = indeterminateRule("indeterminate-p", Effect.PERMIT);
		Rule permit = rule("permit", Decision.PERMIT);
		Rule deny = rule("deny", Decision.DENY);

		// Ordered and unordered variants share Appendix C's decision table; fixed child lists cannot reveal visitation differences.
		assertRuleDecision(v30DenyId, List.of(indeterminateD), Decision.INDETERMINATE_D);
		assertRuleDecision(v30DenyId, List.of(indeterminateP), Decision.INDETERMINATE_P);
		assertRuleDecision(v30DenyId, List.of(indeterminateD, permit), Decision.INDETERMINATE_DP);
		assertFixedRuleDecision(v30DenyId, Decision.INDETERMINATE_DP, Decision.INDETERMINATE_DP);
		assertRuleDecision(v30PermitId, List.of(indeterminateP), Decision.INDETERMINATE_P);
		assertRuleDecision(v30PermitId, List.of(indeterminateD), Decision.INDETERMINATE_D);
		assertRuleDecision(v30PermitId, List.of(indeterminateP, deny), Decision.INDETERMINATE_DP);
		assertFixedRuleDecision(v30PermitId, Decision.INDETERMINATE_DP, Decision.INDETERMINATE_DP);
		assertRuleDecision(v10DenyId, List.of(indeterminateD, permit), Decision.INDETERMINATE);
		assertRuleDecision(v10PermitId, List.of(indeterminateP, deny), Decision.INDETERMINATE);
	}

	@SuppressWarnings("unchecked")
	private static void assertFixedRuleDecision(String identifier, Decision child, Decision expected)
	{
		DecisionCombiningAlgorithm<DecisionRule> algorithm =
				(DecisionCombiningAlgorithm<DecisionRule>) (DecisionCombiningAlgorithm<?>)
						DEFAULT_ALGORITHMS.getRuleAlgorithm(identifier);
		EvaluationContext context = ModelFixtures.context(false);
		assertEquals(expected, algorithm.combine(context, List.of(new FixedResultRule(child, context))), identifier);
	}

	private static final class FixedResultRule implements DecisionRule
	{
		private final Decision result;
		private final EvaluationContext context;

		private FixedResultRule(Decision result, EvaluationContext context)
		{
			this.result = result;
			this.context = context;
		}

		@Override
		public String getId()
		{
			return "fixed-result";
		}

		@Override
		public EvaluationContext createContext(EvaluationContext parent)
		{
			return context;
		}

		@Override
		public MatchResult isMatch(EvaluationContext evaluation)
		{
			throw new AssertionError("Combining must consume the fixed result directly");
		}

		@Override
		public Decision evaluate(EvaluationContext evaluation)
		{
			return result;
		}

		@Override
		public void accept(PolicyTreeVisitor visitor)
		{
		}
	}

	private static void assertRuleDecision(String identifier, List<Rule> rules, Decision expected)
	{
		Policy holder = Policy.builder("registry-holder").version("1.0")
				.combiningAlgorithm(DEFAULT_ALGORITHMS.getRuleAlgorithm(
						"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable"))
				.build();
		DecisionCombiningAlgorithm<Rule> algorithm = DEFAULT_ALGORITHMS.getRuleAlgorithm(identifier);
		assertEquals(expected, algorithm.combine(holder.createContext(ModelFixtures.context(false)), rules), identifier);
	}

	private static Rule indeterminateRule(String id, Effect effect)
	{
		Expression errorExpression = new Expression() {
			@Override
			public ValueExpTypeInfo getEvaluatesTo()
			{
				return XacmlTypes.BOOLEAN;
			}

			@Override
			@SuppressWarnings("unchecked")
			public <T extends Expression> T evaluate(EvaluationContext context) throws EvaluationException
			{
				throw new EvaluationException(Status.processingError().build(), "test indeterminate result");
			}

			@Override
			public void accept(org.xacml4j.v30.ExpressionVisitor visitor)
			{
			}
		};
		return Rule.builder(id, effect).condition(new org.xacml4j.v30.policy.Condition(errorExpression)).build();
	}

	@Test
	public void resolvesOnPermitApplySecondProfileIdentifier()
	{
		DecisionCombiningAlgorithm<CompositeDecisionRule> algorithm =
				DEFAULT_ALGORITHMS.getPolicyAlgorithm(ON_PERMIT_APPLY_SECOND);
		assertNotNull(algorithm, "No combining algorithm for " + ON_PERMIT_APPLY_SECOND);
		Truth.assertThat(algorithm.getId()).isEqualTo(ON_PERMIT_APPLY_SECOND);
		assertTrue(DEFAULT_ALGORITHMS.isPolicyAlgorithmProvided(ON_PERMIT_APPLY_SECOND));
		// The profile defines no rule-combining algorithm with this identifier.
		assertNull(DEFAULT_ALGORITHMS.getRuleAlgorithm(ON_PERMIT_APPLY_SECOND));
		assertFalse(DEFAULT_ALGORITHMS.isRuleAlgorithmProvided(ON_PERMIT_APPLY_SECOND));
	}

	/** CS01 2.1 vectors; kept apart from the Appendix C oracle above. */
	public static Stream<Arguments> onPermitApplySecondProfileVectors()
	{
		return Stream.of(
				// First child Permit: the decision of the second child.
				Arguments.of(List.of(Decision.PERMIT, Decision.DENY), Decision.DENY),
				// First child Deny, NotApplicable or Indeterminate{D}: NotApplicable without a third child,
				// otherwise the decision of the third child.
				Arguments.of(List.of(Decision.DENY, Decision.PERMIT), Decision.NOT_APPLICABLE),
				Arguments.of(List.of(Decision.NOT_APPLICABLE, Decision.PERMIT, Decision.DENY), Decision.DENY),
				Arguments.of(List.of(Decision.INDETERMINATE_D, Decision.DENY, Decision.PERMIT), Decision.PERMIT),
				// First child Indeterminate{P}: Indeterminate{DP}.
				Arguments.of(List.of(Decision.INDETERMINATE_P, Decision.PERMIT), Decision.INDETERMINATE_DP));
	}

	@ParameterizedTest(name = "on-permit-apply-second {0} -> {1}")
	@MethodSource("onPermitApplySecondProfileVectors")
	public void registeredOnPermitApplySecondFollowsItsProfile(List<Decision> children, Decision expected)
	{
		DecisionCombiningAlgorithm<CompositeDecisionRule> algorithm =
				DEFAULT_ALGORITHMS.getPolicyAlgorithm(ON_PERMIT_APPLY_SECOND);
		assertNotNull(algorithm, "No combining algorithm for " + ON_PERMIT_APPLY_SECOND);
		PolicySet holder = PolicySet.builder("profile-holder").version("1.0")
				.withCombiningAlgorithm(algorithm).build();
		List<CompositeDecisionRule> policies = new ArrayList<>();
		for (int index = 0; index < children.size(); index++) {
			policies.add(Policy.builder("profile-policy-" + index).version("1.0")
					.combiningAlgorithm(DEFAULT_ALGORITHMS.getRuleAlgorithm(
							"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable"))
					.rule(rule("profile-rule-" + index, children.get(index))).build());
		}
		assertEquals(expected, algorithm.combine(holder.createContext(ModelFixtures.context(false)), policies),
				ON_PERMIT_APPLY_SECOND + " / " + children);
	}

	@Test
	public void defaultProviderRegistersExactlyTheBaselineAndProfileIdentifiers()
	{
		assertExactIdentifierSets(new DefaultXacml30DecisionCombiningAlgorithms());
		assertExactIdentifierSets(DEFAULT_ALGORITHMS);
	}

	@Test
	public void discoveredProviderRegistersExactlyTheBaselineAndProfileIdentifiers()
	{
		DecisionCombiningAlgorithmProvider discovered = DecisionCombiningAlgorithmProviderBuilder.builder()
				.withDiscoveredAlgorithms().build();
		assertExactIdentifierSets(discovered);
		DecisionCombiningAlgorithm<CompositeDecisionRule> algorithm =
				discovered.getPolicyAlgorithm(ON_PERMIT_APPLY_SECOND);
		assertNotNull(algorithm, "ServiceLoader discovery does not resolve " + ON_PERMIT_APPLY_SECOND);
		Truth.assertThat(algorithm.getId()).isEqualTo(ON_PERMIT_APPLY_SECOND);
	}

	/** The exact sets: every identifier of the a8a90d46 baseline, plus the one profile policy algorithm. */
	private static void assertExactIdentifierSets(DecisionCombiningAlgorithmProvider provider)
	{
		Set<String> rules = new TreeSet<>();
		Set<String> policies = new TreeSet<>();
		algorithmIdentifiers().forEach(arguments -> {
			Object[] identifier = arguments.get();
			("rule".equals(identifier[0]) ? rules : policies).add((String) identifier[1]);
		});
		policies.add(ON_PERMIT_APPLY_SECOND);
		Truth.assertThat(provider.getSupportedRuleAlgorithms()).containsExactlyElementsIn(rules);
		Truth.assertThat(provider.getSupportedPolicyAlgorithms()).containsExactlyElementsIn(policies);
	}

	@Test
	public void defaultCombiningProviderIsDiscoveredAndExplicitDefaultsStillWork()
	{
		Truth.assertThat(ServiceLoader.load(DecisionCombiningAlgorithmProvider.class)
				.stream().map(ServiceLoader.Provider::type).collect(Collectors.toSet()))
				.contains(DefaultXacml30DecisionCombiningAlgorithms.class);
		String id = "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides";
		assertNotNull(DecisionCombiningAlgorithmProviderBuilder.builder()
				.withDefaultAlgorithms().build().getRuleAlgorithm(id));
	}
}
