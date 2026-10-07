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
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.XPathVersion;
import org.xacml4j.v30.policy.EvaluationContextHandler;
import org.xacml4j.v30.types.Entity;
import org.xacml4j.v30.types.XacmlTypes;

class ConditionSafetyNetTest
{
	@Test
	void conditionRejectsIntegerDeclaredExpression()
	{
		AttributeDesignator integer = AttributeDesignator.builder()
				.key(AttributeDesignatorKey.builder("urn:test:integer")
						.category(CategoryId.RESOURCE).dataType(XacmlTypes.INTEGER).build())
				.build();

		assertThrows(IllegalArgumentException.class, () -> new Condition(integer));
	}

	@Test
	void conditionRejectsStringDeclaredExpression()
	{
		AttributeDesignator string = AttributeDesignator.builder()
				.key(AttributeDesignatorKey.builder("urn:test:string")
						.category(CategoryId.RESOURCE).dataType(XacmlTypes.STRING).build())
				.build();

		assertThrows(IllegalArgumentException.class, () -> new Condition(string));
	}

	@Test
	void selectorAbsentResultMustNotSetMissingAttributeStatus()
	{
		EvaluationContextHandler handler = new EvaluationContextHandler() {
			@Override
			public Optional<BagOfValues> resolve(EvaluationContext context, AttributeReferenceKey key) {
				return null;
			}

			@Override
			public Optional<Entity> getEntity(CategoryId id) {
				return Optional.empty();
			}

			@Override
			public Map<org.xacml4j.v30.AttributeDesignatorKey, BagOfValues> getResolvedDesignators() {
				return Collections.emptyMap();
			}

			@Override
			public Map<AttributeSelectorKey, BagOfValues> getResolvedSelectors() {
				return Collections.emptyMap();
			}
		};
		org.xacml4j.v30.policy.PolicyReferenceResolver resolver = new org.xacml4j.v30.policy.PolicyReferenceResolver() {
			@Override
			public Policy resolve(PolicyIDReference ref) { return null; }
			@Override
			public PolicySet resolve(PolicySetIDReference ref) { return null; }
		};
		org.xacml4j.v30.policy.RootEvaluationContext context = new org.xacml4j.v30.policy.RootEvaluationContext(
				false, Duration.ZERO, XPathVersion.XPATH1, resolver, handler);
		AttributeSelector selector = AttributeSelector.builder()
				.key(AttributeSelectorKey.builder().category(CategoryId.RESOURCE)
						.xpath("/missing").dataType(XacmlTypes.STRING).build())
				.mustBePresent(false).build();
		assertThat(selector.evaluate(context)).isEqualTo(XacmlTypes.STRING.emptyBag());
		assertTrue(context.getEvaluationStatus().isEmpty());
	}

	@Test
	void booleanLiteralIsAValidConditionExpression()
	{
		assertThat(new Condition(XacmlTypes.BOOLEAN.ofAny(true)).getExpression().getEvaluatesTo())
				.isEqualTo(XacmlTypes.BOOLEAN);
	}
}
