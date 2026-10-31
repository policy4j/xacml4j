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

import static org.mockito.Mockito.*;

import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.CompositeDecisionRuleIDReference;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.XPathVersion;
import org.xacml4j.v30.spi.observe.DecisionObservation;
import org.xacml4j.v30.types.XacmlTypes;


@ExtendWith(MockitoExtension.class)
public class RootEvaluationContextTest
{
	private EvaluationContextHandler handler;
	private PolicyReferenceResolver resolver;

	@BeforeEach
	public void init(){
		this.handler = mock(EvaluationContextHandler.class);
		this.resolver = mock(PolicyReferenceResolver.class);
	}

	@Test
	public void contextConfigurationKeepsDecisionTTLsIndependent()
	{
		Clock clock = Clock.fixed(Instant.parse("2026-10-28T12:00:00Z"), ZoneOffset.UTC);
		RootEvaluationContext.Configuration configuration = new RootEvaluationContext.Configuration(
				clock, Duration.ofSeconds(30), XPathVersion.XPATH2, resolver);
		RootEvaluationContext first = new RootEvaluationContext(configuration, true, handler, true, DecisionObservation.none());
		RootEvaluationContext second = new RootEvaluationContext(configuration, true, handler, true, DecisionObservation.none());
		RootEvaluationContext legacy = new RootEvaluationContext(clock, true, Duration.ofSeconds(30),
				XPathVersion.XPATH2, resolver, handler);

		assertEquals(first, second);
		assertEquals(legacy, first);
		assertEquals(legacy.hashCode(), first.hashCode());
		assertEquals(legacy.toString(), first.toString());
		assertEquals(first.hashCode(), second.hashCode());
		assertEquals(clock.instant(), first.getClock().instant());
		assertEquals(XPathVersion.XPATH2, first.getXPathVersion());
		first.setDecisionCacheTTL(Duration.ofSeconds(5));
		assertEquals(Duration.ofSeconds(5), first.getDecisionCacheTTL());
		assertEquals(Duration.ofSeconds(30), second.getDecisionCacheTTL());
		first.clear();
		assertEquals(Duration.ZERO, first.getDecisionCacheTTL());
		assertEquals(Duration.ofSeconds(30), second.getDecisionCacheTTL());
	}

	@Test
	public void testSetAndGetDecisionCacheTTLWithDefaultTTLZero()
	{
		RootEvaluationContext context = new RootEvaluationContext(false, Duration.ZERO, resolver, handler);
		assertThat(context.getDecisionCacheTTL(), is(Duration.ofSeconds(0)));
		context.setDecisionCacheTTL(20);
		assertThat(context.getDecisionCacheTTL(), is(Duration.ofSeconds(0)));
		context.setDecisionCacheTTL(10);
		assertThat(context.getDecisionCacheTTL(), is(Duration.ofSeconds(0)));
		context.setDecisionCacheTTL(-1);
		assertThat(context.getDecisionCacheTTL(), is(Duration.ofSeconds(0)));
		context.setDecisionCacheTTL(10);
		assertThat(context.getDecisionCacheTTL(), is(Duration.ofSeconds(0)));
		verifyNoMoreInteractions(handler, resolver);
	}

	@Test
	public void testSetAndGetDecisionCacheTTLWithDefaultTTL()
	{
		RootEvaluationContext context = new RootEvaluationContext(false, Duration.ofSeconds(10), resolver, handler);
		assertThat(context.getDecisionCacheTTL(), is(Duration.ofSeconds(10)));
		context.setDecisionCacheTTL(Duration.ofSeconds(20));
		assertThat(context.getDecisionCacheTTL(), is(Duration.ofSeconds(10)));
		context.setDecisionCacheTTL(Duration.ofSeconds(5));
		assertThat(context.getDecisionCacheTTL(), is(Duration.ofSeconds(5)));
		context.setDecisionCacheTTL(-1);
		assertThat(context.getDecisionCacheTTL(), is(Duration.ZERO));
		context.setDecisionCacheTTL(10);
		assertThat(context.getDecisionCacheTTL(), is(Duration.ZERO));
		verifyNoMoreInteractions(handler, resolver);
	}

	@Test
	public void testResolveDesignatorValueValueIsNotInContext() throws EvaluationException
	{
		RootEvaluationContext context = new RootEvaluationContext(false, Duration.ZERO, resolver, handler);
		AttributeDesignatorKey k = AttributeDesignatorKey
				.builder()
				.category(CategoryId.SUBJECT_ACCESS)
				.attributeId("testId")
				.dataType(XacmlTypes.STRING)
				.issuer("test")
				.build();
		BagOfValues expectedValue = XacmlTypes.STRING.bagBuilder()
		                                             .value("aaa", "ccc")
		                                             .build();

		when(handler.resolve(context, k)).thenReturn(Optional.of(expectedValue));

		InOrder order = inOrder(handler, resolver);
		assertThat(context.resolve(k).get(), is(expectedValue));
		order.verify(handler).resolve(context, k);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(handler, times(1)).resolve(context, k);
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(handler, resolver);
	}

	@Test
	public void testEvaluatedPolicyListContainsOnlyApplicableDecisions()
	{
		RootEvaluationContext context = new RootEvaluationContext(false, Duration.ZERO, resolver, handler);
		CompositeDecisionRule permit = mock(CompositeDecisionRule.class);
		CompositeDecisionRule notApplicable = mock(CompositeDecisionRule.class);
		CompositeDecisionRule indeterminate = mock(CompositeDecisionRule.class);
		CompositeDecisionRuleIDReference permitRef = mock(CompositeDecisionRuleIDReference.class);
		when(permit.getId()).thenReturn("permit");
		when(permit.getReference()).thenReturn(permitRef);

		context.addEvaluationResult(permit, Decision.PERMIT);
		context.addEvaluationResult(notApplicable, Decision.NOT_APPLICABLE);
		context.addEvaluationResult(indeterminate, Decision.INDETERMINATE);

		assertEquals(java.util.List.of(permitRef), context.getEvaluatedPolicies());
	}
}
