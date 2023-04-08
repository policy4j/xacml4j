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

import com.google.common.collect.ImmutableMap;

import org.easymock.Capture;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.types.XacmlTypes;

import java.time.Clock;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

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

	private IMocksControl control;

	private AttributeDesignatorKey.Builder attr0;
	private AttributeDesignatorKey.Builder attr1;
	private AttributeDesignatorKey usernameContextKey;

	@Before
	public void init()
	{
		this.control = createStrictControl();
		this.cache = control.createMock(PolicyInformationPointCacheProvider.class);
		ResolverRegistry.Builder builder = ResolverRegistry.builder();

		this.context = control.createMock(EvaluationContext.class);

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
						   return ImmutableMap.of("testAttributeId1", XacmlTypes.STRING.of("v1").toBag());
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
				.build(c->
				       {
						   return Collections.emptyMap();
				       });
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
			       return ImmutableMap.of("testAttributeId1", XacmlTypes.STRING.of("v1").toBag());
		       });

		builder.withAttributeResolver(testId3WithIssuerNoCacheWithValues);

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
		Capture<ResolverContext> resolverContext1 = Capture.newInstance();
		Capture<ResolverContext> ctx = Capture.newInstance();
		expect(cache.getAttributes(capture(resolverContext1))).andReturn(Optional.empty());
		expect(context.resolve(eq(usernameContextKey)))
				.andReturn(Optional.of(XacmlTypes.STRING.of("testUser").toBag()));
		expect(context.getClock()).andReturn(Clock.systemUTC());

		Capture<ResolverContext> resolverContext2 = Capture.newInstance();
		Capture<AttributeSet> attributeSetCapture = Capture.newInstance();
		cache.putAttributes(capture(resolverContext2), capture(attributeSetCapture));

		control.replay();

		Optional<BagOfValues> v = pip.resolve(context, a0);
		assertEquals(XacmlTypes.STRING.of("v1").toBag(), v.get());
		assertSame(resolverContext1.getValue(), resolverContext2.getValue());

		control.verify();
	}


	@Test
	@Ignore
	public void testFound2MatchingResolversWithDifferentIssuersFirstResolverResolvesToEmptySet() throws Exception
	{

		AttributeDesignatorKey a0 = attr1.build();


		// attribute resolver found
		Capture<ResolverContext> resolverContext1 = Capture.newInstance();
		expect(cache.getAttributes(capture(resolverContext1))).andReturn(Optional.empty());
		Capture<ResolverContext> resolverContext2 = Capture.newInstance();
		expect(cache.getAttributes(capture(resolverContext2))).andReturn(Optional.empty());

//		expect(context.resolve(eq(usernameContextKey)))
//				.andReturn(Optional.of(XacmlTypes.STRING.of("testUser").toBag()));expect(context.resolve(eq(usernameContextKey)))
//			.andReturn(Optional.of(XacmlTypes.STRING.of("testUser").toBag()));

//		expect(context.getClock()).andReturn(Clock.systemUTC());

		Capture<ResolverContext> resolverContext3 = Capture.newInstance();
		Capture<AttributeSet> attributeSetCapture = Capture.newInstance();
		//cache.putAttributes(capture(resolverContext3), capture(attributeSetCapture));

		control.replay();

		Optional<BagOfValues> v = pip.resolve(context, a0);
		assertEquals(XacmlTypes.STRING.of("v1").toBag(), v.get());

		control.verify();
	}

	@Test
	public void testFound2MatchingResolversWithDifferentIssuersFirstResolverThrowsException() throws Exception
	{
		AttributeDesignatorKey a0 = attr0.build();

		// attribute resolver found
		Capture<ResolverContext> resolverContext1 = Capture.newInstance();
		Capture<ResolverContext> ctx = Capture.newInstance();
		expect(cache.getAttributes(capture(resolverContext1))).andReturn(Optional.empty());
		expect(context.resolve(eq(usernameContextKey)))
				.andReturn(Optional.of(XacmlTypes.STRING.of("testUser").toBag()));
		expect(context.getClock()).andReturn(Clock.systemUTC());

		Capture<ResolverContext> resolverContext2 = Capture.newInstance();
		Capture<AttributeSet> attributeSetCapture = Capture.newInstance();
		cache.putAttributes(capture(resolverContext2), capture(attributeSetCapture));

		control.replay();

		Optional<BagOfValues> v = pip.resolve(context, a0);
		assertEquals(XacmlTypes.STRING.of("v1").toBag(), v.get());

		control.verify();
	}

	@Test
	@Ignore
	public void testAttributeResolutionWhenMatchingAttributeResolverFoundResolverResultsIsNotCachable()
		throws Exception
	{
		AttributeDesignatorKey a0 = attr1.build();

		// attribute resolver found
		Capture<ResolverContext> resolverContext1 = Capture.newInstance();
		Capture<ResolverContext> ctx = Capture.newInstance();
		expect(cache.getAttributes(capture(resolverContext1))).andReturn(Optional.empty());
		expect(context.resolve(eq(usernameContextKey)))
				.andReturn(Optional.of(XacmlTypes.STRING.of("testUser").toBag()));

		Capture<ResolverContext> resolverContext2 = Capture.newInstance();
		Capture<AttributeSet> attributeSetCapture = Capture.newInstance();
		cache.putAttributes(capture(resolverContext2), capture(attributeSetCapture));

		control.replay();

		Optional<BagOfValues> v = pip.resolve(context, a0);
		assertEquals(XacmlTypes.STRING.of("v1").toBag(), v.get());

		control.verify();
	}
}
