package org.xacml4j.v30.policy;

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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * Today's variable results of a policy context, which cycle F of change
 * optimize-pdp-evaluation must keep (design D10): a policy without variables has none and
 * rejects every id; a policy with variables keeps the results of its own ids per context.
 */
public class PolicyVariableContextCharacterizationTest
{
	@SuppressWarnings("unchecked")
	private static EvaluationContext root()
	{
		return new RootEvaluationContext(false, Duration.ZERO,
				mock(PolicyReferenceResolver.class), mock(EvaluationContextHandler.class));
	}

	@SuppressWarnings("unchecked")
	private static Policy.Builder policy(String id)
	{
		return Policy.builder(id).combiningAlgorithm(mock(DecisionCombiningAlgorithm.class));
	}

	@Test
	public void policyWithoutVariablesHasNoResultsAndRejectsEveryId()
	{
		EvaluationContext context = policy("urn:p:none").build().createContext(root());

		assertThat(context.getVariableEvaluationResult("v")).isNull();
		assertThrows(IllegalArgumentException.class,
				() -> context.setVariableEvaluationResult("v", XacmlTypes.STRING.ofAny("x")));
		assertThat(context.getVariableEvaluationResult("v")).isNull();
	}

	@Test
	public void policyWithVariablesKeepsResultsPerContext()
	{
		Policy policy = policy("urn:p:vars")
				.var(new VariableDefinition("v1", XacmlTypes.STRING.ofAny("a")))
				.var(new VariableDefinition("v2", XacmlTypes.STRING.ofAny("b")))
				.build();
		EvaluationContext first = policy.createContext(root());
		EvaluationContext second = policy.createContext(root());

		first.setVariableEvaluationResult("v1", XacmlTypes.STRING.ofAny("x"));

		assertThat(first.getVariableEvaluationResult("v1")).isEqualTo(XacmlTypes.STRING.ofAny("x"));
		assertThat(first.getVariableEvaluationResult("v2")).isNull();
		assertThat(second.getVariableEvaluationResult("v1")).isNull();
		assertThrows(IllegalArgumentException.class,
				() -> first.setVariableEvaluationResult("v3", XacmlTypes.STRING.ofAny("y")));
		assertThat(first.createExtIndeterminateEvalContext().getVariableEvaluationResult("v1")).isNull();
	}
}
