package org.xacml4j.v30.pdp;

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
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.AttributeAssignment;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRuleResponse;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.policy.AdviceExpression;
import org.xacml4j.v30.policy.ObligationExpression;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicyIDReference;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.policy.PolicySetIDReference;
import org.xacml4j.v30.policy.Rule;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.PolicyRepository;
import org.xacml4j.v30.types.StringVal;

/**
 * Bead policy4j-the.4.54, scenario 3 through the PDP and a real policy
 * repository: a PolicyIdReference and a PolicySetIdReference below a nested
 * policy set are resolved from the repository, and the response carries
 * the obligations and advice of every level exactly once.
 */
class NestedResponseStagingPdpTest
{
	private static final String URN = "urn:oasis:names:tc:xacml:";
	private static final DecisionCombiningAlgorithmProvider ALGORITHMS =
			DecisionCombiningAlgorithmProvider.builder().withDefaultAlgorithms().build();

	@Test
	void referencesBelowANestedSetAreResolvedFromTheRepositoryAndPublishOnce()
	{
		PolicySet referencedSet = PolicySet.builder("referenced-set").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(
						URN + "1.0:policy-combining-algorithm:first-applicable"))
				.policy(permit("inner"))
				.obligation(obligation("referenced-set-obligation", "referenced-set"))
				.build();
		PolicyRepository repository = PolicyRepository.build("nested-staging-repository").build();
		repository.add(permit("referenced"));
		repository.add(referencedSet);
		// deny-overrides evaluates every child, so both references are used.
		PolicySet nested = PolicySet.builder("nested").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(
						URN + "3.0:policy-combining-algorithm:deny-overrides"))
				.compositeDecisionRules(PolicyIDReference.builder("referenced").build(),
						PolicySetIDReference.builder("referenced-set").build())
				.obligation(obligation("nested-obligation", "nested"))
				.build();
		PolicySet root = PolicySet.builder("root").version("1.0")
				.withCombiningAlgorithm(ALGORITHMS.getPolicyAlgorithm(
						URN + "3.0:policy-combining-algorithm:deny-overrides"))
				.policySet(nested)
				.obligation(obligation("root-obligation", "root"))
				.build();
		PolicyDecisionPoint pdp = PolicyDecisionPointBuilder.builder("nested-staging")
				.policyRepository(repository)
				// The builder needs a PIP. The policies read no attribute.
				.pip(mock(PolicyInformationPoint.class))
				.rootPolicy(root)
				.build();

		Collection<Result> results = pdp.decide(RequestContext.builder().build()).getResults();

		assertEquals(1, results.size());
		Result result = results.iterator().next();
		assertEquals(Decision.PERMIT, result.getDecision());
		assertEquals(StatusCodeId.OK, result.getStatus().getStatusCode().getValue());
		assertEquals(List.of("inner-obligation", "nested-obligation", "referenced-obligation",
				"referenced-set-obligation", "root-obligation"), ids(result.getObligations()));
		assertEquals(List.of("inner-advice", "referenced-advice"), ids(result.getAssociatedAdvice()));
		for (DecisionRuleResponse response : result.getObligations()) {
			assertOrigin(response);
		}
		for (DecisionRuleResponse response : result.getAssociatedAdvice()) {
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

	/** A Permit policy with one obligation and one advice. */
	private static Policy permit(String id)
	{
		return Policy.builder(id).version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(
						URN + "1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder(id + "-rule", Effect.PERMIT).build())
				.obligation(obligation(id + "-obligation", id))
				.advice(AdviceExpression.builder(id + "-advice", Effect.PERMIT)
						.attribute("origin", StringVal.of(id)))
				.build();
	}

	private static ObligationExpression.Builder obligation(String id, String origin)
	{
		return ObligationExpression.builder(id, Effect.PERMIT).attribute("origin", StringVal.of(origin));
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
}
