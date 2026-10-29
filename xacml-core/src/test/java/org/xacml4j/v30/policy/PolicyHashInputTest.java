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
import static org.mockito.Mockito.mock;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.types.HexBinary;
import org.xacml4j.v30.types.XacmlTypes;

public class PolicyHashInputTest
{
	@Test
	public void equalCopiesFindStatusesAfterTheCallersBufferChanges()
	{
		byte[] bytes = {1, 2};
		HexBinary literal = HexBinary.of(ByteBuffer.wrap(bytes));
		PolicySet tree = tree(literal);
		List<DecisionRule> elements = elements(tree);
		for(DecisionRule element : elements){
			element.hashCode();
		}

		bytes[0] = 9;
		PolicySet copy = tree(HexBinary.of(literal.get().toHexEncoded()));
		assertEquals(tree, copy);
		EvaluationContext context = new RootEvaluationContext(false, Duration.ZERO,
				mock(PolicyReferenceResolver.class), mock(EvaluationContextHandler.class));
		List<DecisionRule> copies = elements(copy);
		for(int i = 0; i < elements.size(); i++){
			Status status = Status.processingError().message("status-" + i).build();
			context.setEvaluationStatus(elements.get(i), status);
			assertEquals(Optional.of(status), context.getEvaluationStatus(copies.get(i)));
		}
	}

	private static List<DecisionRule> elements(PolicySet tree)
	{
		Policy policy = (Policy)tree.getDecisions().get(0);
		return List.of(policy.getRules().get(0), policy, tree);
	}

	private static PolicySet tree(HexBinary literal)
	{
		FunctionProvider functions = FunctionProvider.builder().withDefaultFunctions().build();
		DecisionCombiningAlgorithmProvider algorithms = DecisionCombiningAlgorithmProvider.builder()
				.withDefaultAlgorithms().build();
		Match match = Match.builder()
				.predicate(functions.getFunction("urn:oasis:names:tc:xacml:1.0:function:hexBinary-equal").orElseThrow())
				.attribute(literal)
				.attrRef(AttributeDesignator.builder().key(AttributeDesignatorKey.builder()
						.category(CategoryId.RESOURCE).attributeId("binary")
						.dataType(XacmlTypes.HEXBINARY).build()).build()).build();
		Target target = Target.builder().anyOf(MatchAnyOf.builder()
				.anyOf(MatchAllOf.builder().allOf(match).build()).build()).build();
		Rule rule = Rule.builder("rule", Effect.PERMIT).target(target).build();
		CombinerParameter parameter = new CombinerParameter("binary", literal);
		Policy policy = Policy.builder("policy").version("1.0")
				.combiningAlgorithm(algorithms.getRuleAlgorithm(
						"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable"))
				.combinerParam(parameter).rule(rule).build();
		return PolicySet.builder("set").version("1.0")
				.withCombiningAlgorithm(algorithms.getPolicyAlgorithm(
						"urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:first-applicable"))
				.combinerParam(parameter).policy(policy).build();
	}
}
