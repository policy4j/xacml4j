package org.xacml4j.v30.spi.pip;

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

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.WellKnownAttributeIds;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.ImmutableMap;

public class DefaultPolicyInformationPointTest
{
	private PolicyInformationPoint pip;

	private ResolverRegistry registry;


	private PolicyInformationPointCacheProvider cache;
	private EvaluationContext context;

	private AttributeResolverDescriptor descriptor1;
	private AttributeResolverDescriptor testId2WithIssuerEmpty;
	private AttributeResolverDescriptor testId2WithIssuerException;
	private AttributeResolverDescriptor testId3WithIssuerNoCacheWithValues;


	private AttributeDesignatorKey.Builder attr0;
	private AttributeDesignatorKey.Builder attr1;
	private AttributeDesignatorKey usernameContextKey;

	@BeforeEach
	public void init()
	{
		this.cache = Mockito.mock(PolicyInformationPointCacheProvider.class);
		ResolverRegistry.Builder builder = ResolverRegistry.builder();

		this.context = Mockito.mock(EvaluationContext.class);

		this.attr0 = AttributeDesignatorKey
				.builder()
				.category(CategoryId.SUBJECT_ACCESS)
				.attributeId("testAttributeId1")
				.dataType(XacmlTypes.STRING);

		this.attr1 = AttributeDesignatorKey
				.builder()
				.category(CategoryId.SUBJECT_ACCESS)
				.attributeId("testAttributeId2")
				.issuer("Issuer")
				.dataType(XacmlTypes.INTEGER);

		this.usernameContextKey = AttributeDesignatorKey
				.builder()
				.category(CategoryId.SUBJECT_ACCESS)
				.attributeId("username")
				.dataType(XacmlTypes.STRING).build();

		this.descriptor1 = AttributeResolverDescriptor
				.builder("testId1", "Test Resolver",
						CategoryId.SUBJECT_ACCESS)
				.cache(30)
				.attribute("testAttributeId1", XacmlTypes.STRING)
				.attribute("testAttributeId2", XacmlTypes.INTEGER)
				.contextRef(AttributeDesignatorKey
						.builder()
						.category(CategoryId.SUBJECT_ACCESS)
						.attributeId("username")
						.dataType(XacmlTypes.STRING)
						.build())
				.build(c->
				       {
						   c.resolveContextRef(usernameContextKey);
						   return ImmutableMap.of("testAttributeId1", XacmlTypes.STRING.ofAny("v1").toBag());
					   });

		builder.withAttributeResolver(descriptor1);


		this.testId2WithIssuerEmpty = AttributeResolverDescriptor
				.builder("testId2WithIssuerEmpty", "Test Resolver", "Issuer",
				         CategoryId.SUBJECT_ACCESS)
				.cache(40)
				.attribute("testAttributeId1", XacmlTypes.STRING)
				.attribute("testAttributeId2", XacmlTypes.INTEGER)
				.contextRef(AttributeDesignatorKey
						            .builder()
						            .category(CategoryId.SUBJECT_ACCESS)
						            .attributeId("username")
						            .dataType(XacmlTypes.STRING)
						            .build())
				.build(c-> Collections.emptyMap());
		builder.withAttributeResolver(testId2WithIssuerEmpty);

		this.testId2WithIssuerException = AttributeResolverDescriptor
				.builder("testId2WithIssuerException", "Test Resolver", "Issuer",
				         CategoryId.SUBJECT_ACCESS)
				.cache(40)
				.attribute("testAttributeId1", XacmlTypes.STRING)
				.attribute("testAttributeId2", XacmlTypes.INTEGER)
				.contextRef(AttributeDesignatorKey
						            .builder()
						            .category(CategoryId.SUBJECT_ACCESS)
						            .attributeId("username")
						            .dataType(XacmlTypes.STRING)
						            .build())
				.build(c->
				       {
					       c.resolveContextRef(usernameContextKey);
					       throw new IllegalArgumentException();
				       });
		builder.withAttributeResolver(testId2WithIssuerException);


		this.testId3WithIssuerNoCacheWithValues = AttributeResolverDescriptor
		.builder("testId3WithIssuerNoCacheWithValues", "Test Resolver", "Issuer",
				CategoryId.SUBJECT_ACCESS)
		.noCache()
		.attribute("testAttributeId1", XacmlTypes.STRING)
		.attribute("testAttributeId2", XacmlTypes.INTEGER)
		.contextRef(
				AttributeDesignatorKey
				.builder()
				.category(CategoryId.SUBJECT_ACCESS)
				.attributeId("username")
				.dataType(XacmlTypes.STRING)
				.build())
		.build(c->
		       {
			       c.resolveContextRef(usernameContextKey);
			       return ImmutableMap.of("testAttributeId1", XacmlTypes.STRING.ofAny("v1").toBag());
		       });

		builder.withAttributeResolver(testId3WithIssuerNoCacheWithValues);
		builder.withDefaultResolvers();

		this.pip = PolicyInformationPoint
				.builder("testPip")
				.cacheProvider(cache)
				.registry(builder.build())
				.build();

	}

	@Test
	public void testMatchingResolverFoundAndResultIsCacheable() throws Exception
	{
		AttributeDesignatorKey a0 = attr0.build();

		// attribute resolver found
		ArgumentCaptor<ResolverContext> resolverContext1 = ArgumentCaptor.forClass(ResolverContext.class);
		when(cache.getAttributes(Mockito.any())).thenReturn(Optional.empty());
		when(context.resolve(usernameContextKey))
				.thenReturn(Optional.of(XacmlTypes.STRING.ofAny("testUser").toBag()));
		when(context.getClock()).thenReturn(Clock.systemUTC());

		ArgumentCaptor<ResolverContext> resolverContext2 = ArgumentCaptor.forClass(ResolverContext.class);
		ArgumentCaptor<AttributeSet> attributeSetCapture = ArgumentCaptor.forClass(AttributeSet.class);

		Optional<BagOfValues> v = pip.resolve(context, a0);
		InOrder order = Mockito.inOrder(cache, context);
		order.verify(cache).getAttributes(resolverContext1.capture());
		order.verify(context).resolve(usernameContextKey);
		order.verify(context).getClock();
		order.verify(cache).putAttributes(resolverContext2.capture(), attributeSetCapture.capture());
		Mockito.verifyNoMoreInteractions(cache, context);
		assertEquals(XacmlTypes.STRING.ofAny("v1").toBag(), v.get());
		assertSame(resolverContext1.getValue(), resolverContext2.getValue());
	}


	@Test
	@Disabled
	public void testFound2MatchingResolversWithDifferentIssuersFirstResolverResolvesToEmptySet() throws Exception
	{

		AttributeDesignatorKey a0 = attr1.build();


		// attribute resolver found
		when(cache.getAttributes(Mockito.any())).thenReturn(Optional.empty(), Optional.empty());

		Optional<BagOfValues> v = pip.resolve(context, a0);
		Mockito.verify(cache, Mockito.times(2)).getAttributes(Mockito.any());
		Mockito.verifyNoMoreInteractions(cache, context);
		assertEquals(XacmlTypes.STRING.ofAny("v1").toBag(), v.get());
	}

	@Test
	public void testFound2MatchingResolversWithDifferentIssuersFirstResolverThrowsException() throws Exception
	{
		AttributeDesignatorKey a0 = attr0.build();

		// attribute resolver found
		ArgumentCaptor<ResolverContext> resolverContext1 = ArgumentCaptor.forClass(ResolverContext.class);
		when(cache.getAttributes(Mockito.any())).thenReturn(Optional.empty());
		when(context.resolve(usernameContextKey))
				.thenReturn(Optional.of(XacmlTypes.STRING.ofAny("testUser").toBag()));
		when(context.getClock()).thenReturn(Clock.systemUTC());

		ArgumentCaptor<ResolverContext> resolverContext2 = ArgumentCaptor.forClass(ResolverContext.class);
		ArgumentCaptor<AttributeSet> attributeSetCapture = ArgumentCaptor.forClass(AttributeSet.class);

		Optional<BagOfValues> v = pip.resolve(context, a0);
		InOrder order = Mockito.inOrder(cache, context);
		order.verify(cache).getAttributes(resolverContext1.capture());
		order.verify(context).resolve(usernameContextKey);
		order.verify(context).getClock();
		order.verify(cache).putAttributes(resolverContext2.capture(), attributeSetCapture.capture());
		Mockito.verifyNoMoreInteractions(cache, context);
		assertEquals(XacmlTypes.STRING.ofAny("v1").toBag(), v.get());
	}

	@Test
	@Disabled
	public void testAttributeResolutionWhenMatchingAttributeResolverFoundResolverResultsIsNotCachable()
		throws Exception
	{
		AttributeDesignatorKey a0 = attr1.build();

		// attribute resolver found
		ArgumentCaptor<ResolverContext> resolverContext1 = ArgumentCaptor.forClass(ResolverContext.class);
		when(cache.getAttributes(Mockito.any())).thenReturn(Optional.empty());
		when(context.resolve(usernameContextKey))
				.thenReturn(Optional.of(XacmlTypes.STRING.ofAny("testUser").toBag()));

		ArgumentCaptor<ResolverContext> resolverContext2 = ArgumentCaptor.forClass(ResolverContext.class);
		ArgumentCaptor<AttributeSet> attributeSetCapture = ArgumentCaptor.forClass(AttributeSet.class);

		Optional<BagOfValues> v = pip.resolve(context, a0);
		InOrder order = Mockito.inOrder(cache, context);
		order.verify(cache).getAttributes(resolverContext1.capture());
		order.verify(context).resolve(usernameContextKey);
		order.verify(cache).putAttributes(resolverContext2.capture(), attributeSetCapture.capture());
		Mockito.verifyNoMoreInteractions(cache, context);
		assertEquals(XacmlTypes.STRING.ofAny("v1").toBag(), v.get());
	}

	@Test
	public void testEnvironmentResolver()
			throws Exception
	{
		ZonedDateTime now = ZonedDateTime.now();
		AttributeDesignatorKey key  = AttributeDesignatorKey
				.builder().category(CategoryId.ENVIRONMENT).dataType(XacmlTypes.DATETIME)
				.attributeId(WellKnownAttributeIds.CURRENT_TIME.getId()).build();
		when(context.getCurrentDateTime()).thenReturn(now);
		when(context.getClock()).thenReturn(Clock.systemUTC());

		Optional<BagOfValues> v = pip.resolve(context, key);
		assertEquals(XacmlTypes.TIME.ofAny(now).toBag(), v.get());
		InOrder order = Mockito.inOrder(context);
		order.verify(context).getCurrentDateTime();
		order.verify(context).getClock();
		Mockito.verifyNoMoreInteractions(context);
	}
}
