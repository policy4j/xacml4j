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

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationException;
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
		order.verifyNoMoreInteractions();
		verifyNoMoreInteractions(handler, resolver);
	}
}
