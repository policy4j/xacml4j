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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.policy.DecisionCombiningAlgorithm;
import org.xacml4j.v30.policy.ModelFixtures;
import org.xacml4j.v30.policy.PolicyTreeVisitor;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProviderBuilder;

/**
 * XACML 3.0 C.2/C.4 normative decision tables; C.3/C.5 additionally require order.
 * https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html
 * Fixed DecisionRule boundary fixtures isolate aggregation of all six possible results.
 * No policy model or value class is mocked.
 */
public class OverrideDecisionTableTest
{
	private static final Decision P = Decision.PERMIT;
	private static final Decision D = Decision.DENY;
	private static final Decision N = Decision.NOT_APPLICABLE;
	private static final Decision ID = Decision.INDETERMINATE_D;
	private static final Decision IP = Decision.INDETERMINATE_P;
	private static final Decision DP = Decision.INDETERMINATE_DP;
	private static final List<Decision> STATES = List.of(P, D, N, ID, IP, DP);

	// Rows and columns: Permit, Deny, NotApplicable, Indeterminate{D,P,DP}.
	private static final Decision[][] DENY_TABLE = {
			{P, D, P, DP, P, DP},
			{D, D, D, D, D, D},
			{P, D, N, ID, IP, DP},
			{DP, D, ID, ID, DP, DP},
			{P, D, IP, DP, IP, DP},
			{DP, D, DP, DP, DP, DP}};
	private static final Decision[][] PERMIT_TABLE = {
			{P, P, P, P, P, P},
			{P, D, D, D, DP, DP},
			{P, D, N, ID, IP, DP},
			{P, D, ID, ID, DP, DP},
			{P, DP, IP, DP, IP, DP},
			{P, DP, DP, DP, DP, DP}};

	private static final DecisionCombiningAlgorithmProvider PROVIDER =
			DecisionCombiningAlgorithmProviderBuilder.builder().withDefaultAlgorithms().build();

	private record TableCase(String id, String coverage, List<Decision> inputs, Decision expected)
	{
		@Override
		public String toString()
		{
			return id + " " + coverage + " " + inputs + " -> " + expected;
		}
	}

	static Stream<TableCase> tables()
	{
		List<TableCase> rows = new ArrayList<>();
		for (String level : List.of("rule", "policy")) {
			for (String name : List.of("deny-overrides", "permit-overrides",
					"ordered-deny-overrides", "ordered-permit-overrides")) {
				String id = "urn:oasis:names:tc:xacml:3.0:" + level + "-combining-algorithm:" + name;
				Decision[][] table = name.endsWith("permit-overrides") ? PERMIT_TABLE : DENY_TABLE;
				for (Decision a : STATES) {
					for (Decision b : STATES) {
						add(rows, id, "pair", List.of(a, b), table);
						for (Decision c : STATES) {
							add(rows, id, "triple", List.of(a, b, c), table);
						}
					}
				}
				// All 64 presence combinations, both orders, include empty/singleton/long lists.
				for (int mask = 0; mask < (1 << STATES.size()); mask++) {
					List<Decision> values = new ArrayList<>();
					for (int bit = 0; bit < STATES.size(); bit++) {
						if ((mask & (1 << bit)) != 0) {
							values.add(STATES.get(bit));
						}
					}
					add(rows, id, "subset-" + mask, List.copyOf(values), table);
					add(rows, id, "reverse-subset-" + mask, values.reversed(), table);
				}
				// Retain the engine's legacy generic-Indeterminate input as conservative DP.
				rows.add(new TableCase(id, "generic-Indeterminate",
						List.of(Decision.INDETERMINATE, N), DP));
			}
		}
		return rows.stream();
	}

	private static void add(List<TableCase> rows, String id, String coverage, List<Decision> inputs,
			Decision[][] table)
	{
		Decision expected = N;
		for (Decision input : inputs) {
			expected = table[STATES.indexOf(expected)][STATES.indexOf(input)];
		}
		rows.add(new TableCase(id, coverage, List.copyOf(inputs), expected));
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("tables")
	public void everyRegisteredOverrideVariantMatchesTheNormativeTable(TableCase row)
	{
		DecisionCombiningAlgorithm<DecisionRule> algorithm = algorithm(row.id());
		assertNotNull(algorithm);
		assertEquals(row.id(), algorithm.getId());
		EvaluationContext context = ModelFixtures.context(false);
		List<String> calls = new ArrayList<>();
		List<FixedDecision> children = new ArrayList<>();
		for (int index = 0; index < row.inputs().size(); index++) {
			children.add(new FixedDecision(index, row.inputs().get(index), context, calls));
		}
		assertEquals(row.expected(), algorithm.combine(context, new ArrayList<DecisionRule>(children)));
		Decision dominant = row.id().endsWith("permit-overrides") ? P : D;
		boolean hasDominant = row.inputs().contains(dominant);
		for (FixedDecision child : children) {
			assertEquals(child.created, child.evaluated, "create/evaluate count must match");
			if (!hasDominant) {
				assertEquals(1, child.evaluated, "Every child is required without an overriding result");
			} else {
				assertTrue(child.evaluated == 0 || child.evaluated == 1, "Never evaluate a child twice");
			}
		}
		if (hasDominant) {
			assertTrue(children.stream().anyMatch(child -> child.result == dominant && child.evaluated == 1),
					"An overriding result must come from a visited child");
		}
		if (row.id().contains(":ordered-")) {
			List<String> expectedCalls = new ArrayList<>();
			for (int index = 0; index < row.inputs().size(); index++) {
				expectedCalls.add("create:" + index);
				expectedCalls.add("evaluate:" + index);
				if (row.inputs().get(index) == dominant) {
					break;
				}
			}
			assertEquals(expectedCalls, calls, "Ordered variants visit exactly the normative prefix");
		}
	}

	@SuppressWarnings("unchecked")
	private static DecisionCombiningAlgorithm<DecisionRule> algorithm(String id)
	{
		// All registry entries operate on DecisionRule; the fixtures supply decisions at that boundary.
		return (DecisionCombiningAlgorithm<DecisionRule>)(DecisionCombiningAlgorithm<?>)
				(id.contains(":rule-") ? PROVIDER.getRuleAlgorithm(id) : PROVIDER.getPolicyAlgorithm(id));
	}

	private static final class FixedDecision implements DecisionRule
	{
		private final int index;
		private final Decision result;
		private final EvaluationContext context;
		private final List<String> calls;
		private int created;
		private int evaluated;

		private FixedDecision(int index, Decision result, EvaluationContext context, List<String> calls)
		{
			this.index = index;
			this.result = result;
			this.context = context;
			this.calls = calls;
		}

		@Override
		public String getId()
		{
			return "decision-" + index;
		}

		@Override
		public EvaluationContext createContext(EvaluationContext parent)
		{
			assertSame(context, parent);
			created++;
			calls.add("create:" + index);
			return context;
		}

		@Override
		public Decision evaluate(EvaluationContext evaluation)
		{
			assertSame(context, evaluation);
			evaluated++;
			calls.add("evaluate:" + index);
			return result;
		}

		@Override
		public MatchResult isMatch(EvaluationContext evaluation)
		{
			throw new AssertionError("Combining consumes evaluate(); it must not match a fixture separately");
		}

		@Override
		public void accept(PolicyTreeVisitor visitor)
		{
			throw new AssertionError("Unexpected fixture traversal");
		}
	}
}
