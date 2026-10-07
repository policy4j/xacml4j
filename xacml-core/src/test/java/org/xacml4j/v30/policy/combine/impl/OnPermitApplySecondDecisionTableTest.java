package org.xacml4j.v30.policy.combine.impl;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.CompositeDecisionRuleIDReference;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.StatusCode;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.StatusDetail;
import org.xacml4j.v30.policy.DecisionCombiningAlgorithm;
import org.xacml4j.v30.policy.ModelFixtures;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.policy.PolicyTreeVisitor;
import org.xacml4j.v30.policy.Rule;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProviderBuilder;

/**
 * XACML 3.0 Additional Combining Algorithms Profile 1.0, CS01 (18 August 2014), section 2.1.
 * https://docs.oasis-open.org/xacml/xacml-3.0-combalgs/v1.0/cs01/xacml-3.0-combalgs-v1.0-cs01.html
 * The tables below transcribe the normative pseudo-code; no expectation comes from the implementation.
 * Recording children fix each result at the CompositeDecisionRule boundary and report their error
 * status through their own production child context. No policy model or value class is mocked.
 */
public class OnPermitApplySecondDecisionTableTest
{
	private static final Decision P = Decision.PERMIT;
	private static final Decision D = Decision.DENY;
	private static final Decision N = Decision.NOT_APPLICABLE;
	private static final Decision ID = Decision.INDETERMINATE_D;
	private static final Decision IP = Decision.INDETERMINATE_P;
	private static final Decision DP = Decision.INDETERMINATE_DP;
	private static final List<Decision> STATES = List.of(P, D, N, ID, IP, DP);

	// CS01 2.1: the status code for a policy set that has not two or three children.
	private static final String PROCESSING_ERROR = "urn:oasis:names:tc:xacml:1.0:status:processing-error";

	private static final DecisionCombiningAlgorithm<Rule> CONTEXT_OWNER_ALGORITHM =
			DecisionCombiningAlgorithmProviderBuilder.builder().withDefaultAlgorithms().build()
					.getRuleAlgorithm("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable");

	/** What the decision of the first child selects. */
	private enum Branch
	{
		SECOND_CHILD, THIRD_CHILD, NOT_APPLICABLE, FIRST_CHILD_ERROR
	}

	// First child, in STATES order: Permit, Deny, NotApplicable, Indeterminate{D}, Indeterminate{P}, Indeterminate{DP}.
	private static final List<Branch> TWO_CHILDREN = List.of(
			Branch.SECOND_CHILD, Branch.NOT_APPLICABLE, Branch.NOT_APPLICABLE, Branch.NOT_APPLICABLE,
			Branch.FIRST_CHILD_ERROR, Branch.FIRST_CHILD_ERROR);
	private static final List<Branch> THREE_CHILDREN = List.of(
			Branch.SECOND_CHILD, Branch.THIRD_CHILD, Branch.THIRD_CHILD, Branch.THIRD_CHILD,
			Branch.FIRST_CHILD_ERROR, Branch.FIRST_CHILD_ERROR);

	private record TableCase(String coverage, List<Decision> children, Decision expected, List<Integer> evaluated)
	{
		@Override
		public String toString()
		{
			return coverage + " " + children + " -> " + expected + " evaluating " + evaluated;
		}
	}

	private record Evaluation(Decision decision, List<String> calls, List<RecordingChild> children,
			EvaluationContext root)
	{
	}

	static Stream<TableCase> decisionTable()
	{
		List<TableCase> rows = new ArrayList<>();
		// All 36 pairs and 216 triples: the children that are not selected vary too.
		for (Decision first : STATES) {
			for (Decision second : STATES) {
				rows.add(row("pair", List.of(first, second), TWO_CHILDREN.get(STATES.indexOf(first))));
				for (Decision third : STATES) {
					rows.add(row("triple", List.of(first, second, third),
							THREE_CHILDREN.get(STATES.indexOf(first))));
				}
			}
		}
		return rows.stream();
	}

	static Stream<TableCase> legacyIndeterminateFirstChild()
	{
		List<TableCase> rows = new ArrayList<>();
		for (Decision second : STATES) {
			rows.add(row("legacy-pair", List.of(Decision.INDETERMINATE, second), Branch.FIRST_CHILD_ERROR));
			rows.add(row("legacy-triple", List.of(Decision.INDETERMINATE, second, P), Branch.FIRST_CHILD_ERROR));
		}
		return rows.stream();
	}

	static Stream<Arguments> invalidChildCounts()
	{
		List<Arguments> rows = new ArrayList<>();
		rows.add(Arguments.of(0, List.of()));
		for (int count : new int[] {1, 4, 5, 8}) {
			for (Decision first : STATES) {
				List<Decision> children = new ArrayList<>();
				children.add(first);
				for (int index = 1; index < count; index++) {
					children.add(STATES.get(index % STATES.size()));
				}
				rows.add(Arguments.of(count, List.copyOf(children)));
			}
		}
		return rows.stream();
	}

	private static TableCase row(String coverage, List<Decision> children, Branch branch)
	{
		return switch (branch) {
			case SECOND_CHILD -> new TableCase(coverage, children, children.get(1), List.of(0, 1));
			case THIRD_CHILD -> new TableCase(coverage, children, children.get(2), List.of(0, 2));
			case NOT_APPLICABLE -> new TableCase(coverage, children, N, List.of(0));
			case FIRST_CHILD_ERROR -> new TableCase(coverage, children, DP, List.of(0));
		};
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("decisionTable")
	public void twoOrThreeChildrenFollowTheNormativePseudoCode(TableCase row)
	{
		assertRow(row);
	}

	/**
	 * CS01 2.1 names Indeterminate{P} and Indeterminate{DP} for the last branch of the pseudo-code. The
	 * engine's legacy plain Indeterminate is outside that list: this boundary keeps it on the same
	 * conservative branch, Indeterminate{DP} with the status of the first child.
	 */
	@ParameterizedTest(name = "{0}")
	@MethodSource("legacyIndeterminateFirstChild")
	public void legacyIndeterminateFirstChildIsIndeterminateDPWithItsStatus(TableCase row)
	{
		assertRow(row);
	}

	@ParameterizedTest(name = "{0} children {1}")
	@MethodSource("invalidChildCounts")
	public void otherThanTwoOrThreeChildrenIsAProcessingErrorWithoutEvaluation(int count, List<Decision> children)
	{
		assertEquals(count, children.size());
		Evaluation evaluation = evaluate(children);
		assertEquals(DP, evaluation.decision());
		assertEquals(List.of(), evaluation.calls(), "No child context is created and no child is evaluated");
		Optional<Status> status = evaluation.root().getEvaluationStatus();
		assertTrue(status.isPresent(), "The error must carry a status");
		assertEquals(PROCESSING_ERROR, status.get().getStatusCode().getValue().getId());
	}

	private static void assertRow(TableCase row)
	{
		Evaluation evaluation = evaluate(row.children());
		assertEquals(row.expected(), evaluation.decision(), "Decision");
		List<String> expectedCalls = new ArrayList<>();
		List<Status> reported = new ArrayList<>();
		for (int index : row.evaluated()) {
			expectedCalls.add("create:" + index);
			expectedCalls.add("evaluate:" + index);
			if (evaluation.children().get(index).status != null) {
				reported.add(evaluation.children().get(index).status);
			}
		}
		assertEquals(expectedCalls, evaluation.calls(), "Evaluation order and count");
		Optional<Status> status = evaluation.root().getEvaluationStatus();
		if (row.expected().isIndeterminate()) {
			// The deciding child is the last one evaluated; its whole status is the status of the result.
			int deciding = row.evaluated().get(row.evaluated().size() - 1);
			assertEquals(Optional.of(evaluation.children().get(deciding).status), status,
					"Status of the deciding child " + deciding);
		} else {
			assertTrue(status.isEmpty() || reported.contains(status.get()),
					"The algorithm must not report a status of its own: " + status);
		}
	}

	private static Evaluation evaluate(List<Decision> decisions)
	{
		DecisionCombiningAlgorithm<CompositeDecisionRule> algorithm =
				new OnPermitApplySecondPolicyCombiningAlgorithm();
		EvaluationContext root = ModelFixtures.context(false);
		PolicySet holder = PolicySet.builder("on-permit-apply-second-holder").version("1.0")
				.withCombiningAlgorithm(algorithm).build();
		EvaluationContext combining = holder.createContext(root);
		List<String> calls = new ArrayList<>();
		List<RecordingChild> children = new ArrayList<>();
		for (int index = 0; index < decisions.size(); index++) {
			children.add(new RecordingChild(index, decisions.get(index), combining, calls));
		}
		Decision decision = algorithm.combine(combining, new ArrayList<CompositeDecisionRule>(children));
		return new Evaluation(decision, calls, children, root);
	}

	/** Distinct code, nested code, message and detail for each child: a preserved status names its writer. */
	private static Status status(int index)
	{
		StatusCodeId[] codes = {StatusCodeId.MISSING_ATTRIBUTE, StatusCodeId.SYNTAX_ERROR,
				StatusCodeId.STATUS_PROCESSING_ERROR};
		StatusCode code = StatusCode.builder(codes[index % codes.length])
				.minorStatus(StatusCode.builder(codes[(index + 1) % codes.length]).build())
				.build();
		return Status.builder(code)
				.message("status of child " + index)
				.detail(new StatusDetail("detail of child " + index))
				.build();
	}

	private static final class RecordingChild implements CompositeDecisionRule
	{
		private final int index;
		private final Decision result;
		private final Status status;
		private final EvaluationContext combining;
		private final List<String> calls;
		// A real policy supplies a distinct production child context for each evaluation.
		private final Policy contextOwner;
		private EvaluationContext created;

		private RecordingChild(int index, Decision result, EvaluationContext combining, List<String> calls)
		{
			this.index = index;
			this.result = result;
			this.status = result.isIndeterminate() ? status(index) : null;
			this.combining = combining;
			this.calls = calls;
			this.contextOwner = Policy.builder("child-" + index + "-context").version("1.0")
					.combiningAlgorithm(CONTEXT_OWNER_ALGORITHM).build();
		}

		@Override
		public String getId()
		{
			return "child-" + index;
		}

		@Override
		public EvaluationContext createContext(EvaluationContext parent)
		{
			assertSame(combining, parent, "A child context derives from the combining context");
			calls.add("create:" + index);
			created = contextOwner.createContext(parent);
			return created;
		}

		@Override
		public Decision evaluate(EvaluationContext evaluation)
		{
			assertSame(created, evaluation, "A child is evaluated in the context it created");
			calls.add("evaluate:" + index);
			if (status != null) {
				evaluation.setEvaluationStatus(status);
			}
			return result;
		}

		@Override
		public MatchResult isMatch(EvaluationContext evaluation)
		{
			throw new AssertionError("Combining consumes evaluate(); it must not match a fixture separately");
		}

		@Override
		public CompositeDecisionRuleIDReference getReference()
		{
			throw new AssertionError("Unexpected fixture reference lookup");
		}

		@Override
		public void accept(PolicyTreeVisitor visitor)
		{
			throw new AssertionError("Unexpected fixture traversal");
		}
	}
}
