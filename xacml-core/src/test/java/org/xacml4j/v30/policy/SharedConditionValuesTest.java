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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.Attribute;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.XPathVersion;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * Only a value that the request gives, under the request-entity contract of the context handler, is
 * shared within a decision (change optimize-pdp-evaluation, design D45, maintainer decision 3 of
 * 2026-10-28). The handler of these tests gives a new address at every read it answers itself, as an
 * information point need not give one value twice: the sixth rule permits only when every rule reads
 * the address again.
 */
class SharedConditionValuesTest
{
	private static final String FUNCTION = "urn:oasis:names:tc:xacml:1.0:function:";
	private static final FunctionProvider FUNCTIONS = FunctionProviderBuilder.builder().build();
	private static final AttributeDesignatorKey EMAIL = AttributeDesignatorKey.builder("urn:example:email")
			.category(CategoryId.SUBJECT_ACCESS).dataType(XacmlTypes.STRING).build();
	private static final int RULES = 6;

	@Test
	void anAddressThatTheRequestDoesNotGiveIsReadInEachRule(){
		Policy policy = policy();
		assertEquals(Decision.PERMIT, policy.evaluate(policy.createContext(root(new Reads(RequestContext.builder().build(), true)))));
	}

	@Test
	void anAddressOfAHandlerWithoutTheRequestEntityContractIsReadInEachRule(){
		RequestContext request = RequestContext.builder().attributes(Category.subjectAccess().entity(Entity.builder()
				.attribute(Attribute.builder(EMAIL.getAttributeId()).value(StringVal.of("  Rule-9@Example.COM ")).build())
				.build()).build()).build();
		Reads handler = new Reads(request, false);
		Policy policy = policy();
		assertEquals(Decision.PERMIT, policy.evaluate(policy.createContext(root(handler))));
	}

	/** The address that the request gives is the one of every rule: no rule permits */
	@Test
	void anAddressThatTheRequestGivesIsTheSameInEachRule(){
		RequestContext request = RequestContext.builder().attributes(Category.subjectAccess().entity(Entity.builder()
				.attribute(Attribute.builder(EMAIL.getAttributeId()).value(StringVal.of("  Rule-9@Example.COM ")).build())
				.build()).build()).build();
		Policy policy = policy();
		assertEquals(Decision.NOT_APPLICABLE, policy.evaluate(policy.createContext(root(new Reads(request, true)))));
	}

	private static RootEvaluationContext root(EvaluationContextHandler handler){
		return new RootEvaluationContext(Clock.systemUTC(), false, Duration.ZERO, XPathVersion.XPATH1,
				mock(PolicyReferenceResolver.class), handler, false, PreparedPolicySets.create(false));
	}

	/** Six rules, the first applicable deciding, each permitting for the address rule-5@example.com */
	private static Policy policy(){
		Policy.Builder policy = Policy.builder("reads").version("1.0").combiningAlgorithm(
				DecisionCombiningAlgorithmProvider.builder().withDefaultAlgorithms().build()
						.getRuleAlgorithm("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable"));
		for(int i = 0; i < RULES; i++){
			Expression email = apply("string-normalize-to-lower-case", apply("string-normalize-space",
					apply("string-one-and-only", AttributeDesignator.builder().key(EMAIL).mustBePresent(false).build())));
			policy.rule(Rule.builder("rule-" + i, Effect.PERMIT).condition(new Condition(
					apply("string-equal", email, StringVal.of("rule-" + (RULES - 1) + "@example.com")))));
		}
		return policy.build();
	}

	private static Apply apply(String function, Expression... arguments){
		return Apply.builder(FUNCTIONS.getFunction(FUNCTION + function).orElseThrow()).param(arguments).build();
	}

	/**
	 * A handler that answers a designator from the request entity when it keeps the request-entity
	 * contract and the entity has a bag, and else with a new address at each read
	 */
	private static final class Reads implements EvaluationContextHandler
	{
		private final RequestContext request;
		private final boolean requestEntity;
		private int reads;

		Reads(RequestContext request, boolean requestEntity){
			this.request = request;
			this.requestEntity = requestEntity;
		}

		@Override
		public Optional<BagOfValues> resolve(EvaluationContext context, AttributeReferenceKey key){
			if(requestEntity && key instanceof AttributeDesignatorKey designator){
				Optional<BagOfValues> bag = request.getEntity(designator.getCategory()).flatMap(e -> e.resolve(designator));
				if(bag.isPresent()){
					return bag;
				}
			}
			return Optional.of(XacmlTypes.STRING.bagOf(StringVal.of("  Rule-" + reads++ + "@Example.COM ")));
		}

		@Override
		public Optional<Entity> getEntity(CategoryId id){
			return request.getEntity(id);
		}

		@Override
		public boolean usesRequestEntityForDesignators(){
			return requestEntity;
		}

		@Override
		public Map<AttributeDesignatorKey, BagOfValues> getResolvedDesignators(){
			return Map.of();
		}

		@Override
		public Map<AttributeSelectorKey, BagOfValues> getResolvedSelectors(){
			return Map.of();
		}
	}
}
