package org.xacml4j.v30.policy.external;

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

import java.time.Duration;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.AttributeAssignment;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.policy.EvaluationContextHandler;
import org.xacml4j.v30.policy.ObligationExpression;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicyReferenceResolver;
import org.xacml4j.v30.policy.RootEvaluationContext;
import org.xacml4j.v30.policy.Rule;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.types.StringVal;

/**
 * A subclass of the public {@link ObligationExpression} declared outside its package: its
 * evaluation of the assignments decides the obligation also when every assignment is a literal
 * (review R-AN of change optimize-pdp-evaluation, Codex Sol, P2-3).
 */
public class ExternalObligationExpressionCompatibilityTest
{
	private static final DecisionCombiningAlgorithmProvider ALGORITHMS =
			DecisionCombiningAlgorithmProvider.builder().withDefaultAlgorithms().build();

	@Test
	public void failingAssignmentEvaluationOfASubclassMakesThePolicyIndeterminate()
	{
		Policy policy = Policy.builder("urn:test:audited").version("1.0")
				.combiningAlgorithm(ALGORITHMS.getRuleAlgorithm(
						"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable"))
				.rule(Rule.builder("urn:test:permit", Effect.PERMIT))
				.obligation(new Failing(ObligationExpression.builder("urn:test:audit", Effect.PERMIT)
						.attribute("origin", StringVal.of("audited")), "audit unavailable"))
				.build();
		RootEvaluationContext root = new RootEvaluationContext(false, Duration.ZERO,
				mock(PolicyReferenceResolver.class), mock(EvaluationContextHandler.class));

		Decision decision = policy.evaluate(policy.createContext(root));

		// XACML 3.0, section 7.18: an obligation that cannot be evaluated makes its policy Indeterminate
		assertEquals(Decision.INDETERMINATE_P, decision);
		Status status = root.getEvaluationStatus().orElseThrow();
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, status.getStatusCode().getValue());
		assertEquals("audit unavailable", status.getMessage().orElse(null));
		assertTrue(root.getMatchingObligations(Decision.PERMIT).isEmpty());
		assertTrue(root.getMatchingObligations(Decision.DENY).isEmpty());
	}

	/**
	 * An expression whose assignments are not available at evaluation; its identifier depends on
	 * a field of its own, which only a constructor that calls no overridable method leaves intact
	 */
	private static final class Failing extends ObligationExpression
	{
		private final String message;

		private Failing(ObligationExpression.Builder b, String message) {
			super(b);
			this.message = message;
		}

		@Override
		public String getId() {
			return super.getId() + ":" + message;
		}

		@Override
		protected Collection<AttributeAssignment> evaluateAttributeAssignments(EvaluationContext context)
				throws EvaluationException
		{
			throw new EvaluationException(Status.processingError().message(message).build(), message);
		}
	}

	@Test
	public void theIdentifierOfTheSubclassIsItsOwn()
	{
		Failing failing = new Failing(ObligationExpression.builder("urn:test:audit", Effect.PERMIT), "audit unavailable");

		assertEquals("urn:test:audit:audit unavailable", failing.getId());
		assertEquals(List.of(), failing.getAttributeAssignmentExpressions().stream().toList());
	}
}
