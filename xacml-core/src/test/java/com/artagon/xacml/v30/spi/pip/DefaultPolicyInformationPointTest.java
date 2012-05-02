package com.artagon.xacml.v30.spi.pip;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictControl;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Map;

import org.easymock.Capture;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.pdp.AttributeDesignatorKey;
import com.artagon.xacml.v30.pdp.BagOfAttributeExp;
import com.artagon.xacml.v30.pdp.EvaluationContext;
import com.artagon.xacml.v30.types.IntegerType;
import com.artagon.xacml.v30.types.StringType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

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
		
		this.descriptor1 = AttributeResolverDescriptorBuilder
				.builder("testId1", "Test Resolver", 
						AttributeCategories.SUBJECT_ACCESS)
				.cache(30)
				.attribute("testAttributeId1", StringType.STRING)
				.attribute("testAttributeId2", IntegerType.INTEGER)
				.designatorKeyRef(AttributeCategories.SUBJECT_ACCESS, "username", StringType.STRING, null)
				.build();

		this.descriptor1WithIssuer = AttributeResolverDescriptorBuilder
				.builder("testId2", "Test Resolver", "Issuer", 
						AttributeCategories.SUBJECT_ACCESS)
				.cache(40)
				.attribute("testAttributeId1", StringType.STRING)
				.attribute("testAttributeId2", IntegerType.INTEGER)
				.designatorKeyRef(AttributeCategories.SUBJECT_ACCESS, "username", StringType.STRING, null)
				.build();

		
		this.descriptor1WithNoCache = AttributeResolverDescriptorBuilder
		.builder("testId3", "Test Resolver", "Issuer",
				AttributeCategories.SUBJECT_ACCESS)
		.noCache()
		.attribute("testAttributeId1", StringType.STRING)
		.attribute("testAttributeId2", IntegerType.INTEGER)
		.designatorKeyRef(AttributeCategories.SUBJECT_ACCESS, "username", StringType.STRING, null)
		.build();
	}
	
	@Test
	public void testAttributeResolutionWhenMatchingAttributeResolverFoundResolverResultsIsCachable() throws Exception
	{
		Map<String, BagOfAttributeExp> values = ImmutableMap.of(
				"testAttributeId1", 
				StringType.STRING.bagOf(StringType.STRING.create("v1")));
		
		AttributeDesignatorKey ref = new AttributeDesignatorKey(
				AttributeCategories.SUBJECT_ACCESS, "testAttributeId1", StringType.STRING, null);
		
		// attribute resolver found
		expect(registry.getMatchingAttributeResolvers(context, ref)).andReturn(ImmutableList.of(resolver1, resolver2));
		expect(resolver1.getDescriptor()).andReturn(descriptor1);
		
		
		// key resolved
		expect(context.resolve(
				new AttributeDesignatorKey(
						AttributeCategories.SUBJECT_ACCESS, "username", StringType.STRING, null)))
				.andReturn(StringType.STRING.bagOf(StringType.STRING.create("testUser")));
		
		Capture<ResolverContext> resolverContext1 = new Capture<ResolverContext>();
		Capture<ResolverContext> ctx = new Capture<ResolverContext>();
		
		AttributeSet result = new AttributeSet(descriptor1, values);
		
		expect(cache.getAttributes(capture(resolverContext1))).andReturn(null);
		expect(resolver1.resolve(capture(ctx))).andReturn(result);
		
		context.setResolvedDesignatorValue(
				new AttributeDesignatorKey(
						AttributeCategories.SUBJECT_ACCESS, "" +
						"testAttributeId1", 
						StringType.STRING, null), 
						StringType.STRING.bagOf(StringType.STRING.create("v1")));
		
		context.setResolvedDesignatorValue(
				new AttributeDesignatorKey(
						AttributeCategories.SUBJECT_ACCESS, 
						"testAttributeId2", IntegerType.INTEGER, null), 
						IntegerType.INTEGER.emptyBag());
		Capture<ResolverContext> resolverContext2 = new Capture<ResolverContext>();

		cache.putAttributes(capture(resolverContext2), eq(result));
		
		context.setDecisionCacheTTL(descriptor1.getPreferreredCacheTTL());
		
		control.replay();
		
		BagOfAttributeExp v = pip.resolve(context, ref);
		assertEquals(StringType.STRING.bagOf(StringType.STRING.create("v1")), v);
		assertSame(resolverContext1.getValue(), resolverContext2.getValue());

		control.verify();
	}
	
	
	@Test
	public void testFound2MatchingResolversWithDifferentIssuersFirstResolverResolvesToEmptySet() throws Exception
	{
		Map<String, BagOfAttributeExp> values = ImmutableMap.of(
				"testAttributeId1", 
				StringType.STRING.bagOf(StringType.STRING.create("v1")));
		
		AttributeDesignatorKey keyRef = new AttributeDesignatorKey(
				AttributeCategories.SUBJECT_ACCESS, "username", StringType.STRING, null);
		
		AttributeDesignatorKey ref = new AttributeDesignatorKey(
				AttributeCategories.SUBJECT_ACCESS, "testAttributeId1", StringType.STRING, null);
		
		// attribute resolver found
		expect(registry.getMatchingAttributeResolvers(context, ref)).andReturn(
				ImmutableList.of(resolver1, resolver2));
		
		
		Capture<ResolverContext> ctx1 = new Capture<ResolverContext>();
		Capture<ResolverContext> ctx2 = new Capture<ResolverContext>();
		
		Capture<ResolverContext> cacheCtx1 = new Capture<ResolverContext>();
		Capture<ResolverContext> cacheCtx2 = new Capture<ResolverContext>();
		
		AttributeSet result1 = new AttributeSet(descriptor1);
		AttributeSet result2 = new AttributeSet(descriptor1WithIssuer, values);
		
		expect(resolver1.getDescriptor()).andReturn(descriptor1);
		expect(context.resolve(keyRef))
			.andReturn(StringType.STRING.bagOf(StringType.STRING.create("testUser")));
		expect(cache.getAttributes(capture(cacheCtx1))).andReturn(null);		
		expect(resolver1.resolve(capture(ctx1))).andReturn(result1);
		
			
		
		expect(resolver2.getDescriptor()).andReturn(descriptor1WithIssuer);
		expect(context.resolve(keyRef))
			.andReturn(StringType.STRING.bagOf(StringType.STRING.create("testUser")));
		expect(cache.getAttributes(capture(cacheCtx2))).andReturn(null);
		
		
		expect(resolver2.resolve(capture(ctx2))).andReturn(result2);
		
		context.setResolvedDesignatorValue(
				new AttributeDesignatorKey(
						AttributeCategories.SUBJECT_ACCESS, "" +
						"testAttributeId1", 
						StringType.STRING, 
						descriptor1WithIssuer.getIssuer()), 
						StringType.STRING.bagOf(StringType.STRING.create("v1")));
		
		context.setResolvedDesignatorValue(
				new AttributeDesignatorKey(
						AttributeCategories.SUBJECT_ACCESS, 
						"testAttributeId2", IntegerType.INTEGER, 
						descriptor1WithIssuer.getIssuer()), 
						IntegerType.INTEGER.emptyBag());
		
	
		Capture<ResolverContext> ctx3 = new Capture<ResolverContext>();
		cache.putAttributes(capture(ctx3), eq(result2));
		
		context.setDecisionCacheTTL(descriptor1WithIssuer.getPreferreredCacheTTL());
		
		control.replay();
		
		BagOfAttributeExp v = pip.resolve(context, ref);
		assertEquals(StringType.STRING.bagOf(StringType.STRING.create("v1")), v);
		assertSame(ctx2.getValue(), ctx3.getValue());

		control.verify();
	}
	
	@Test
	public void testFound2MatchingResolversWithDifferentIssuersFirstResolverThrowsException() throws Exception
	{
		Map<String, BagOfAttributeExp> values = ImmutableMap.of(
				"testAttributeId1", 
				StringType.STRING.bagOf(StringType.STRING.create("v1")));
		
		AttributeDesignatorKey keyRef = new AttributeDesignatorKey(
				AttributeCategories.SUBJECT_ACCESS, "username", StringType.STRING, null);
		
		AttributeDesignatorKey ref = new AttributeDesignatorKey(
				AttributeCategories.SUBJECT_ACCESS, "testAttributeId1", StringType.STRING, null);
		
		// attribute resolver found
		expect(registry.getMatchingAttributeResolvers(context, ref)).andReturn(
				ImmutableList.of(resolver1, resolver2));
		
		
		Capture<ResolverContext> ctx1 = new Capture<ResolverContext>();
		Capture<ResolverContext> ctx2 = new Capture<ResolverContext>();
		
		Capture<ResolverContext> cacheCtx1 = new Capture<ResolverContext>();
		Capture<ResolverContext> cacheCtx2 = new Capture<ResolverContext>();
		
		AttributeSet result1 = new AttributeSet(descriptor1);
		AttributeSet result2 = new AttributeSet(descriptor1WithIssuer, values);
		
		expect(resolver1.getDescriptor()).andReturn(descriptor1);
		expect(context.resolve(keyRef))
			.andReturn(StringType.STRING.bagOf(StringType.STRING.create("testUser")));
		expect(cache.getAttributes(capture(cacheCtx1))).andReturn(null);		
		expect(resolver1.resolve(capture(ctx1))).andThrow(new NullPointerException());
		
			
		
		expect(resolver2.getDescriptor()).andReturn(descriptor1WithIssuer);
		expect(context.resolve(keyRef))
			.andReturn(StringType.STRING.bagOf(StringType.STRING.create("testUser")));
		expect(cache.getAttributes(capture(cacheCtx2))).andReturn(null);
		
		
		expect(resolver2.resolve(capture(ctx2))).andReturn(result2);
		
		context.setResolvedDesignatorValue(
				new AttributeDesignatorKey(
						AttributeCategories.SUBJECT_ACCESS, "" +
						"testAttributeId1", 
						StringType.STRING, 
						descriptor1WithIssuer.getIssuer()), 
						StringType.STRING.bagOf(StringType.STRING.create("v1")));
		
		context.setResolvedDesignatorValue(
				new AttributeDesignatorKey(
						AttributeCategories.SUBJECT_ACCESS, 
						"testAttributeId2", IntegerType.INTEGER, 
						descriptor1WithIssuer.getIssuer()), 
						IntegerType.INTEGER.emptyBag());
		
	
		Capture<ResolverContext> ctx3 = new Capture<ResolverContext>();
		cache.putAttributes(capture(ctx3), eq(result2));
		
		context.setDecisionCacheTTL(descriptor1WithIssuer.getPreferreredCacheTTL());
		
		control.replay();
		
		BagOfAttributeExp v = pip.resolve(context, ref);
		assertEquals(StringType.STRING.bagOf(StringType.STRING.create("v1")), v);
		assertSame(ctx2.getValue(), ctx3.getValue());

		control.verify();
	}
	
	@Test
	public void testAttributeResolutionWhenMatchingAttributeResolverFoundResolverResultsIsNotCachable() 
		throws Exception
	{
		Map<String, BagOfAttributeExp> values = ImmutableMap.of(
				"testAttributeId1", 
				StringType.STRING.bagOf(StringType.STRING.create("v1")));
		
		AttributeDesignatorKey keyRef = new AttributeDesignatorKey(
				AttributeCategories.SUBJECT_ACCESS, "username", StringType.STRING, null);
		
		AttributeDesignatorKey ref = new AttributeDesignatorKey(
				AttributeCategories.SUBJECT_ACCESS, "testAttributeId1", StringType.STRING, null);
		
		// attribute resolver found
		expect(registry.getMatchingAttributeResolvers(context, ref)).andReturn(ImmutableList.of(resolver1));
		expect(resolver1.getDescriptor()).andReturn(descriptor1WithNoCache);
				
		// key resolved
		expect(context.resolve(keyRef))
				.andReturn(StringType.STRING.bagOf(StringType.STRING.create("testUser")));
		
		Capture<ResolverContext> ctx = new Capture<ResolverContext>();
		
		AttributeSet result = new AttributeSet(descriptor1WithNoCache, values);
		
		expect(resolver1.resolve(capture(ctx))).andReturn(result);
		
		context.setResolvedDesignatorValue(
				new AttributeDesignatorKey(
						AttributeCategories.SUBJECT_ACCESS, 
						"testAttributeId1", 
						StringType.STRING, 
						descriptor1WithNoCache.getIssuer()), 
						StringType.STRING.bagOf("v1"));
		
		context.setResolvedDesignatorValue(
				new AttributeDesignatorKey(
						AttributeCategories.SUBJECT_ACCESS, 
						"testAttributeId2", 
						IntegerType.INTEGER, 
						descriptor1WithNoCache.getIssuer()), 
						IntegerType.INTEGER.emptyBag());
			
		context.setDecisionCacheTTL(descriptor1WithNoCache.getPreferreredCacheTTL());
		control.replay();
		
		BagOfAttributeExp v = pip.resolve(context, ref);
		assertEquals(StringType.STRING.bagOf(StringType.STRING.create("v1")), v);

		control.verify();
	}
}