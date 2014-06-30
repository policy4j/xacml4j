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

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictControl;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.easymock.Capture;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.types.StringExp;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.ImmutableList;

public class DefaultPolicyInformationPointTest
{
	private PolicyInformationPoint pip;

	private ResolverRegistry registry;

	private AttributeResolver resolver1;
	private AttributeResolver resolver2;

	private PolicyInformationPointCacheProvider cache;
	private EvaluationContext context;

	private AttributeResolverDescriptor descriptor1;
	private AttributeResolverDescriptor descriptor1WithIssuer;
	private AttributeResolverDescriptor descriptor1WithNoCache;

	private IMocksControl control;

	private AttributeDesignatorKey.Builder attr0;
	private AttributeDesignatorKey.Builder attr1;
	private AttributeDesignatorKey.Builder key;

	@Before
	public void init()
	{
		this.control = createStrictControl();
		this.cache = control.createMock(PolicyInformationPointCacheProvider.class);
		this.registry = control.createMock(ResolverRegistry.class);
		this.resolver1 = control.createMock(AttributeResolver.class);
		this.resolver2 = control.createMock(AttributeResolver.class);

		this.context = control.createMock(EvaluationContext.class);

		this.pip = PolicyInformationPointBuilder
		.builder("testPip")
		.withCacheProvider(cache)
		.build(registry);

		this.attr0 = AttributeDesignatorKey
				.builder()
				.category(Categories.SUBJECT_ACCESS)
				.attributeId("testAttributeId1")
				.dataType(XacmlTypes.STRING);

		this.attr1 = AttributeDesignatorKey
				.builder()
				.category(Categories.SUBJECT_ACCESS)
				.attributeId("testAttributeId2")
				.dataType(XacmlTypes.INTEGER);

		this.key = AttributeDesignatorKey
				.builder()
				.category(Categories.SUBJECT_ACCESS)
				.attributeId("username")
				.dataType(XacmlTypes.STRING);

		this.descriptor1 = AttributeResolverDescriptorBuilder
				.builder("testId1", "Test Resolver",
						Categories.SUBJECT_ACCESS)
				.cache(30)
				.attribute("testAttributeId1", XacmlTypes.STRING)
				.attribute("testAttributeId2", XacmlTypes.INTEGER)
				.designatorKeyRef(Categories.SUBJECT_ACCESS, "username", XacmlTypes.STRING, null)
				.build();

		this.descriptor1WithIssuer = AttributeResolverDescriptorBuilder
				.builder("testId2", "Test Resolver", "Issuer",
						Categories.SUBJECT_ACCESS)
				.cache(40)
				.attribute("testAttributeId1", XacmlTypes.STRING)
				.attribute("testAttributeId2", XacmlTypes.INTEGER)
				.designatorKeyRef(Categories.SUBJECT_ACCESS, "username", XacmlTypes.STRING, null)
				.build();


		this.descriptor1WithNoCache = AttributeResolverDescriptorBuilder
		.builder("testId3", "Test Resolver", "Issuer",
				Categories.SUBJECT_ACCESS)
		.noCache()
		.attribute("testAttributeId1", XacmlTypes.STRING)
		.attribute("testAttributeId2", XacmlTypes.INTEGER)
		.designatorKeyRef(Categories.SUBJECT_ACCESS, "username", XacmlTypes.STRING, null)
		.build();
	}

	@Test
	public void testMatchingResolverFoundAndResultIsCachable() throws Exception
	{
		AttributeDesignatorKey a0 = attr0.build();
		AttributeDesignatorKey a1 = attr1.build();
		AttributeDesignatorKey k = key.build();

		AttributeSet result = AttributeSet
				.builder(descriptor1)
				.attribute("testAttributeId1", StringExp.valueOf("v1").toBag())
				.build();

		// attribute resolver found
		expect(registry.getMatchingAttributeResolvers(context, a0))
		.andReturn(ImmutableList.of(resolver1, resolver2));
		expect(resolver1.getDescriptor()).andReturn(descriptor1);

		expect(context.resolve(eq(k))).andReturn(StringExp.valueOf("testUser").toBag());

		Capture<ResolverContext> resolverContext1 = new Capture<ResolverContext>();
		Capture<ResolverContext> ctx = new Capture<ResolverContext>();

		expect(cache.getAttributes(capture(resolverContext1))).andReturn(null);
		expect(resolver1.resolve(capture(ctx))).andReturn(result);

		Capture<ResolverContext> resolverContext2 = new Capture<ResolverContext>();

		cache.putAttributes(capture(resolverContext2), eq(result));

		context.setDecisionCacheTTL(descriptor1.getPreferredCacheTTL());

		control.replay();

		BagOfAttributeExp v = pip.resolve(context, a0);
		assertEquals(XacmlTypes.STRING.bagOf(StringExp.valueOf("v1")), v);
		assertSame(resolverContext1.getValue(), resolverContext2.getValue());

		control.verify();
	}


	@Test
	public void testFound2MatchingResolversWithDifferentIssuersFirstResolverResolvesToEmptySet() throws Exception
	{

		AttributeDesignatorKey a0 = attr0.build();

		// attribute resolver found
		expect(registry.getMatchingAttributeResolvers(context, a0)).andReturn(
				ImmutableList.of(resolver1, resolver2));

		AttributeSet result1 = AttributeSet
				.builder(descriptor1)
				.build();

		AttributeSet result2 = AttributeSet
				.builder(descriptor1WithIssuer)
				.attribute("testAttributeId1", StringExp.valueOf("v1").toBag())
				.build();

		expect(resolver1.getDescriptor()).andReturn(descriptor1);
		expect(context.resolve(key.build()))
		.andReturn(StringExp.valueOf("testUser").toBag());

		Capture<ResolverContext> ctx1 = new Capture<ResolverContext>();
		Capture<ResolverContext> ctx2 = new Capture<ResolverContext>();

		Capture<ResolverContext> cacheCtx1 = new Capture<ResolverContext>();
		Capture<ResolverContext> cacheCtx2 = new Capture<ResolverContext>();

		expect(cache.getAttributes(capture(cacheCtx1))).andReturn(null);
		expect(resolver1.resolve(capture(ctx1))).andReturn(result1);

		expect(resolver2.getDescriptor()).andReturn(descriptor1WithIssuer);
		expect(context.resolve(key.build()))
		.andReturn(StringExp.valueOf("testUser").toBag());

		expect(cache.getAttributes(capture(cacheCtx2))).andReturn(null);
		expect(resolver2.resolve(capture(ctx2))).andReturn(result2);

		Capture<ResolverContext> cacheCtx3 = new Capture<ResolverContext>();

		cache.putAttributes(capture(cacheCtx3), eq(result2));
		context.setDecisionCacheTTL(descriptor1WithIssuer.getPreferredCacheTTL());

		control.replay();

		BagOfAttributeExp v = pip.resolve(context, a0);
		assertEquals(StringExp.valueOf("v1").toBag(), v);

		control.verify();
	}

	@Test
	public void testFound2MatchingResolversWithDifferentIssuersFirstResolverThrowsException() throws Exception
	{
		AttributeDesignatorKey a0 = attr0.build();
		AttributeDesignatorKey k = key.build();

		// attribute resolver found
		expect(registry.getMatchingAttributeResolvers(context, a0)).andReturn(
				ImmutableList.of(resolver1, resolver2));


		Capture<ResolverContext> ctx1 = new Capture<ResolverContext>();
		Capture<ResolverContext> ctx2 = new Capture<ResolverContext>();

		Capture<ResolverContext> cacheCtx1 = new Capture<ResolverContext>();
		Capture<ResolverContext> cacheCtx2 = new Capture<ResolverContext>();

		AttributeSet result2 = AttributeSet
				.builder(descriptor1WithIssuer)
				.attribute("testAttributeId1", StringExp.valueOf("v1").toBag())
				.build();

		expect(resolver1.getDescriptor()).andReturn(descriptor1);
		expect(context.resolve(k))
			.andReturn(StringExp.valueOf("testUser").toBag());
		expect(cache.getAttributes(capture(cacheCtx1))).andReturn(null);
		expect(resolver1.resolve(capture(ctx1))).andThrow(new NullPointerException());



		expect(resolver2.getDescriptor()).andReturn(descriptor1WithIssuer);
		expect(context.resolve(k))
			.andReturn(StringExp.valueOf("testUser").toBag());
		expect(cache.getAttributes(capture(cacheCtx2))).andReturn(null);


		expect(resolver2.resolve(capture(ctx2))).andReturn(result2);

		Capture<ResolverContext> ctx3 = new Capture<ResolverContext>();
		cache.putAttributes(capture(ctx3), eq(result2));

		context.setDecisionCacheTTL(descriptor1WithIssuer.getPreferredCacheTTL());

		control.replay();

		BagOfAttributeExp v = pip.resolve(context, a0);
		assertEquals(StringExp.valueOf("v1").toBag(), v);
		assertSame(ctx2.getValue(), ctx3.getValue());

		control.verify();
	}

	@Test
	public void testAttributeResolutionWhenMatchingAttributeResolverFoundResolverResultsIsNotCachable()
		throws Exception
	{
		AttributeDesignatorKey a0 = attr0.build();
		AttributeDesignatorKey k = key.build();

		// attribute resolver found
		expect(registry.getMatchingAttributeResolvers(context, a0)).andReturn(ImmutableList.of(resolver1));
		expect(resolver1.getDescriptor()).andReturn(descriptor1WithNoCache);

		// key resolved
		expect(context.resolve(k))
				.andReturn(StringExp.valueOf("testUser").toBag());

		Capture<ResolverContext> ctx = new Capture<ResolverContext>();

		AttributeSet result = AttributeSet
				.builder(descriptor1WithNoCache)
				.attribute("testAttributeId1", StringExp.valueOf("v1").toBag())
				.build();

		expect(resolver1.resolve(capture(ctx))).andReturn(result);

		context.setDecisionCacheTTL(descriptor1WithNoCache.getPreferredCacheTTL());
		control.replay();

		BagOfAttributeExp v = pip.resolve(context, a0);
		assertEquals(StringExp.valueOf("v1").toBag(), v);

		control.verify();
	}
}
