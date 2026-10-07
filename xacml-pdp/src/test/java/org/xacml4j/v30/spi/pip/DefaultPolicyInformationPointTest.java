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
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import org.xacml4j.v30.AttributeReferenceEvaluationException;
import org.xacml4j.v30.AttributeSelectorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.PathEvaluationException;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.WellKnownAttributeIds;
import org.xacml4j.v30.content.XmlContent;
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
	public void resolverFailureWithoutAnotherValueIsThrownAsProcessingError()
	{
		// Maintainer ruling 2026-10-07 (run 061 D3): a resolver failure is not a missing attribute.
		PolicyInformationPoint failing = pipWith(resolver("failing", c -> {
			throw new IllegalStateException("backend down");
		}));

		EvaluationException e = assertThrows(EvaluationException.class,
				() -> failing.resolve(context, attr0.build()));

		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, e.getStatus().getStatusCode().getValue());
	}

	@Test
	public void resolverFailureWithAnEmptyResolverIsStillAFailure()
	{
		PolicyInformationPoint failingAndEmpty = pipWith(
				resolver("failing", c -> {
					throw new IllegalStateException("backend down");
				}),
				resolver("empty", c -> Collections.emptyMap()));

		EvaluationException e = assertThrows(EvaluationException.class,
				() -> failingAndEmpty.resolve(context, attr0.build()));

		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, e.getStatus().getStatusCode().getValue());
	}

	@Test
	public void resolverFailureDoesNotHideAValueFromALaterResolver()
	{
		when(context.getClock()).thenReturn(Clock.systemUTC());
		java.util.List<String> calls = new java.util.ArrayList<>();
		// The registry keeps registration order, so the failing resolver runs first.
		PolicyInformationPoint failingThenWorking = pipWith(
				resolver("failing", c -> {
					calls.add("failing");
					throw new IllegalStateException("backend down");
				}),
				resolver("working", c -> {
					calls.add("working");
					return ImmutableMap.of("testAttributeId1", XacmlTypes.STRING.ofAny("v1").toBag());
				}));

		Optional<BagOfValues> v = failingThenWorking.resolve(context, attr0.build());

		assertEquals(XacmlTypes.STRING.ofAny("v1").toBag(), v.get());
		assertEquals(java.util.Arrays.asList("failing", "working"), calls);
	}

	@Test
	public void contentResolverFailureIsThrownAndNotReportedAsNoValue()
	{
		ContentResolverDescriptor failing = ContentResolverDescriptor
				.builder("failingContent", "Test Content Resolver", CategoryId.SUBJECT_ACCESS)
				.noCache()
				.build(c -> {
					throw new IllegalStateException("content backend down");
				});
		PolicyInformationPoint contentPip = PolicyInformationPoint.builder("failClosedContentPip")
				.cacheProvider(cache)
				.registry(ResolverRegistry.builder().withContentResolver(failing).build())
				.build();
		AttributeSelectorKey selector = AttributeSelectorKey.builder()
				.category(CategoryId.SUBJECT_ACCESS)
				.xpath("/a/text()")
				.dataType(XacmlTypes.STRING)
				.build();

		AttributeReferenceEvaluationException e = assertThrows(AttributeReferenceEvaluationException.class,
				() -> contentPip.resolve(context, selector));

		assertResolverFailure(e);
	}

	/**
	 * A resolver cannot choose the status or the response message of its failure,
	 * whatever exception type it throws (rv079 round 2, findings 1 and 2).
	 */
	@Test
	public void attributeResolverCannotChooseTheStatusOrTheResponseMessage()
	{
		for (RuntimeException thrown : resolverChosenFailures()) {
			PolicyInformationPoint failing = pipWith(resolver("failing", c -> {
				throw thrown;
			}));

			AttributeReferenceEvaluationException e = assertThrows(AttributeReferenceEvaluationException.class,
					() -> failing.resolve(context, attr0.build()), thrown.getClass().getSimpleName());

			assertResolverFailure(e);
		}
	}

	@Test
	public void contentResolverCannotChooseTheStatusOrTheResponseMessage()
	{
		AttributeSelectorKey selector = AttributeSelectorKey.builder()
				.category(CategoryId.SUBJECT_ACCESS)
				.xpath("/a/text()")
				.dataType(XacmlTypes.STRING)
				.build();
		for (RuntimeException thrown : resolverChosenFailures()) {
			PolicyInformationPoint failing = contentPipWith(c -> {
				throw thrown;
			});

			AttributeReferenceEvaluationException e = assertThrows(AttributeReferenceEvaluationException.class,
					() -> failing.resolve(context, selector), thrown.getClass().getSimpleName());

			assertResolverFailure(e);
		}
	}

	@Test
	public void selectorPathErrorOnSuppliedContentKeepsItsOwnPathStatus() throws Exception
	{
		when(context.getClock()).thenReturn(Clock.systemUTC());
		XmlContent content = XmlContent.of(XmlContent.fromString("<a>1</a>"));
		PolicyInformationPoint contentPip = contentPipWith(c -> content);
		AttributeSelectorKey malformedPath = AttributeSelectorKey.builder()
				.category(CategoryId.SUBJECT_ACCESS)
				.xpath("///")
				.dataType(XacmlTypes.STRING)
				.build();

		// The path is evaluated by the engine on the supplied content, outside the resolver
		// boundary: the path exception is not mapped to a resolver failure.
		PathEvaluationException e = assertThrows(PathEvaluationException.class,
				() -> contentPip.resolve(context, malformedPath));

		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, e.getStatus().getStatusCode().getValue());
	}

	@Test
	public void selectorWithoutAMatchOnSuppliedContentReportsNoValue() throws Exception
	{
		when(context.getClock()).thenReturn(Clock.systemUTC());
		XmlContent content = XmlContent.of(XmlContent.fromString("<a>1</a>"));
		PolicyInformationPoint contentPip = contentPipWith(c -> content);
		AttributeSelectorKey noMatch = AttributeSelectorKey.builder()
				.category(CategoryId.SUBJECT_ACCESS)
				.xpath("/b/text()")
				.dataType(XacmlTypes.STRING)
				.build();

		assertEquals(Optional.empty(), contentPip.resolve(context, noMatch));
	}

	/**
	 * rv080 round 3, finding 1: a resolver that supplies attributes without the
	 * requested one has no value. It does not end the search, and it does not
	 * drop the failure of an earlier resolver.
	 */
	@Test
	public void resolverFailureWithAResolverThatLacksTheAttributeIsStillAFailure()
	{
		when(context.getClock()).thenReturn(Clock.systemUTC());
		PolicyInformationPoint failingThenOther = pipWith(
				resolver("failing", c -> {
					throw new IllegalStateException("backend down");
				}),
				resolverOfTwoAttributes("other", c ->
						ImmutableMap.of("testAttributeId2", XacmlTypes.INTEGER.ofAny(1).toBag())));

		AttributeReferenceEvaluationException e = assertThrows(AttributeReferenceEvaluationException.class,
				() -> failingThenOther.resolve(context, attr0.build()));

		assertResolverFailure(e);
	}

	@Test
	public void resolverThatLacksTheAttributeDoesNotHideAValueFromALaterResolver()
	{
		when(context.getClock()).thenReturn(Clock.systemUTC());
		java.util.List<String> calls = new java.util.ArrayList<>();
		PolicyInformationPoint otherThenWorking = pipWith(
				resolverOfTwoAttributes("other", c -> {
					calls.add("other");
					return ImmutableMap.of("testAttributeId2", XacmlTypes.INTEGER.ofAny(1).toBag());
				}),
				resolver("working", c -> {
					calls.add("working");
					return ImmutableMap.of("testAttributeId1", XacmlTypes.STRING.ofAny("v1").toBag());
				}));

		Optional<BagOfValues> v = otherThenWorking.resolve(context, attr0.build());

		assertEquals(Optional.of(XacmlTypes.STRING.ofAny("v1").toBag()), v);
		assertEquals(java.util.Arrays.asList("other", "working"), calls);
	}

	@Test
	public void resolverThatLacksTheAttributeAndNoFailureReportNoValue()
	{
		when(context.getClock()).thenReturn(Clock.systemUTC());
		PolicyInformationPoint other = pipWith(resolverOfTwoAttributes("other", c ->
				ImmutableMap.of("testAttributeId2", XacmlTypes.INTEGER.ofAny(1).toBag())));

		assertEquals(Optional.empty(), other.resolve(context, attr0.build()));
	}

	/**
	 * rv080 round 3, finding 1, for content resolvers. The default registry
	 * keeps one content resolver per category, so the registry is a stub that
	 * matches two.
	 */
	@Test
	public void contentResolverFailureWithContentThatSelectsNothingIsStillAFailure() throws Exception
	{
		when(context.getClock()).thenReturn(Clock.systemUTC());
		XmlContent content = XmlContent.of(XmlContent.fromString("<a>1</a>"));
		AttributeSelectorKey noMatch = AttributeSelectorKey.builder()
				.category(CategoryId.SUBJECT_ACCESS).xpath("/b/text()").dataType(XacmlTypes.STRING).build();
		for (boolean failingFirst : new boolean[] {true, false}) {
			java.util.List<String> calls = new java.util.ArrayList<>();
			ContentResolverDescriptor failing = contentResolver("failing", c -> {
				calls.add("failing");
				throw new IllegalStateException("content backend down");
			});
			ContentResolverDescriptor selectsNothing = contentResolver("selectsNothing", c -> {
				calls.add("selectsNothing");
				return content;
			});
			PolicyInformationPoint contentPip = failingFirst
					? contentPipMatching(failing, selectsNothing)
					: contentPipMatching(selectsNothing, failing);

			AttributeReferenceEvaluationException e = assertThrows(AttributeReferenceEvaluationException.class,
					() -> contentPip.resolve(context, noMatch), "failingFirst=" + failingFirst);

			assertResolverFailure(e);
			// Both resolvers are asked in either order.
			assertEquals(2, calls.size(), "failingFirst=" + failingFirst);
		}
	}

	@Test
	public void contentThatSelectsNothingDoesNotHideAValueFromALaterContentResolver() throws Exception
	{
		when(context.getClock()).thenReturn(Clock.systemUTC());
		XmlContent withoutNode = XmlContent.of(XmlContent.fromString("<b>1</b>"));
		XmlContent withNode = XmlContent.of(XmlContent.fromString("<a>v1</a>"));
		AttributeSelectorKey selector = AttributeSelectorKey.builder()
				.category(CategoryId.SUBJECT_ACCESS).xpath("/a/text()").dataType(XacmlTypes.STRING).build();
		PolicyInformationPoint contentPip = contentPipMatching(
				contentResolver("withoutNode", c -> withoutNode),
				contentResolver("withNode", c -> withNode));

		assertEquals(Optional.of(XacmlTypes.STRING.ofAny("v1").toBag()), contentPip.resolve(context, selector));
	}

	private static ContentResolverDescriptor contentResolver(String id,
			java.util.function.Function<ResolverContext, org.xacml4j.v30.Content> function)
	{
		return ContentResolverDescriptor
				.builder(id, "Test Content Resolver", CategoryId.SUBJECT_ACCESS)
				.noCache()
				.build(function);
	}

	private PolicyInformationPoint contentPipMatching(ContentResolverDescriptor... matched)
	{
		ResolverRegistry registry = Mockito.mock(ResolverRegistry.class);
		when(registry.getMatchingContentResolver(Mockito.any(), Mockito.any()))
				.thenReturn(java.util.Arrays.asList(matched));
		return PolicyInformationPoint.builder("contentPipMatching")
				.cacheProvider(cache)
				.registry(registry)
				.build();
	}

	private static AttributeResolverDescriptor resolverOfTwoAttributes(String id,
			java.util.function.Function<ResolverContext, java.util.Map<String, BagOfValues>> function)
	{
		return AttributeResolverDescriptor
				.builder(id, "Test Resolver", CategoryId.SUBJECT_ACCESS)
				.noCache()
				.attribute("testAttributeId1", XacmlTypes.STRING)
				.attribute("testAttributeId2", XacmlTypes.INTEGER)
				.build(function);
	}

	/**
	 * rv079 round 4: a resolver failure whose {@code getMessage()} throws an
	 * exception with a status of its own choice is still mapped.
	 */
	@Test
	public void resolverFailureWithAThrowingMessageIsStillMapped()
	{
		AttributeDesignatorKey key = attr0.build();
		@SuppressWarnings("serial")
		RuntimeException throwingMessage = new RuntimeException() {
			@Override
			public String getMessage() {
				throw new AttributeReferenceEvaluationException(key, "backend secret");
			}
		};
		PolicyInformationPoint failing = pipWith(resolver("failing", c -> {
			throw throwingMessage;
		}));
		AttributeSelectorKey selector = AttributeSelectorKey.builder()
				.category(CategoryId.SUBJECT_ACCESS).xpath("/a/text()").dataType(XacmlTypes.STRING).build();
		PolicyInformationPoint failingContent = contentPipWith(c -> {
			throw throwingMessage;
		});

		assertResolverFailure(assertThrows(AttributeReferenceEvaluationException.class,
				() -> failing.resolve(context, key)));
		assertResolverFailure(assertThrows(AttributeReferenceEvaluationException.class,
				() -> failingContent.resolve(context, selector)));
	}

	private static java.util.List<RuntimeException> resolverChosenFailures()
	{
		AttributeDesignatorKey key = AttributeDesignatorKey.builder()
				.category(CategoryId.SUBJECT_ACCESS).attributeId("k").dataType(XacmlTypes.STRING).build();
		return java.util.Arrays.asList(
				new IllegalStateException("backend secret"),
				PathEvaluationException.invalidXpath("/x", Status.syntaxError().message("backend secret").build(),
						new IllegalStateException("backend secret")),
				new AttributeReferenceEvaluationException(Status.processingError().build(), key, "backend secret"),
				new AttributeReferenceEvaluationException(key, "backend secret"),
				new EvaluationException(Status.syntaxError().build(), "backend secret"));
	}

	private static void assertResolverFailure(AttributeReferenceEvaluationException e)
	{
		assertEquals(true, e.isResolverFailure());
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, e.getStatus().getStatusCode().getValue());
		assertEquals(Optional.of(AttributeReferenceEvaluationException.RESOLUTION_FAILED_MESSAGE),
				e.getStatus().getMessage());
	}

	private PolicyInformationPoint contentPipWith(
			java.util.function.Function<ResolverContext, org.xacml4j.v30.Content> function)
	{
		ContentResolverDescriptor descriptor = ContentResolverDescriptor
				.builder("contentResolver", "Test Content Resolver", CategoryId.SUBJECT_ACCESS)
				.noCache()
				.build(function);
		return PolicyInformationPoint.builder("contentPip")
				.cacheProvider(cache)
				.registry(ResolverRegistry.builder().withContentResolver(descriptor).build())
				.build();
	}

	@Test
	public void resolversWithoutAValueAndWithoutAFailureReportNoValue()
	{
		PolicyInformationPoint empty = pipWith(resolver("empty", c -> Collections.emptyMap()));

		assertEquals(Optional.empty(), empty.resolve(context, attr0.build()));
	}

	private static AttributeResolverDescriptor resolver(String id,
			java.util.function.Function<ResolverContext, java.util.Map<String, BagOfValues>> function)
	{
		return AttributeResolverDescriptor
				.builder(id, "Test Resolver", CategoryId.SUBJECT_ACCESS)
				.noCache()
				.attribute("testAttributeId1", XacmlTypes.STRING)
				.build(function);
	}

	private PolicyInformationPoint pipWith(AttributeResolverDescriptor... resolvers)
	{
		ResolverRegistry.Builder builder = ResolverRegistry.builder();
		for (AttributeResolverDescriptor resolver : resolvers) {
			builder.withAttributeResolver(resolver);
		}
		return PolicyInformationPoint.builder("failClosedPip")
				.cacheProvider(cache)
				.registry(builder.build())
				.build();
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
