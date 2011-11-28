package com.artagon.xacml.v30.spi.pip;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Collections;
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
	private AttributeResolverDescriptor descriptor2;
	private AttributeResolverDescriptor deascriptor1NoCache;
	
	private IMocksControl control;
	
	@Before
	public void init()
	{
		this.control = createControl();
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
		.builder("testId", "Test Resolver", AttributeCategories.SUBJECT_ACCESS)
		.cache(30)
		.attribute("testAttributeId1", StringType.STRING)
		.attribute("testAttributeId2", IntegerType.INTEGER)
		.designatorRef(AttributeCategories.SUBJECT_ACCESS, "username", StringType.STRING, null)
		.build();

		this.descriptor2 = AttributeResolverDescriptorBuilder
				.builder("testId", "Test Resolver", "Issuer", AttributeCategories.SUBJECT_ACCESS)
				.cache(30)
				.attribute("testAttributeId1", StringType.STRING)
				.attribute("testAttributeId2", IntegerType.INTEGER)
				.designatorRef(AttributeCategories.SUBJECT_ACCESS, "username", StringType.STRING, null)
				.build();

		
		this.deascriptor1NoCache = AttributeResolverDescriptorBuilder
		.builder("testId", "Test Resolver", AttributeCategories.SUBJECT_ACCESS)
		.noCache()
		.attribute("testAttributeId3", StringType.STRING)
		.attribute("testAttributeId4", IntegerType.INTEGER)
		.designatorRef(AttributeCategories.SUBJECT_ACCESS, "username", StringType.STRING, null)
		.build();
	}
	
	@Test
	public void testAttributeResolutionWhenMatchingAttributeResolverFoundResolverResultsIsCachable() throws Exception
	{
		Map<String, BagOfAttributeExp> values = ImmutableMap.of(
				"testAttributeId1", 
				StringType.STRING.bagOf(StringType.STRING.create("v1")));
		
		AttributeDesignatorKey ref = new AttributeDesignatorKey(
				AttributeCategories.SUBJECT_ACCESS,"testAttributeId1", StringType.STRING, null);
		
		// attribute resolver found
		expect(registry.getMatchingAttributeResolvers(context, ref)).andReturn(ImmutableList.of(resolver1, resolver2));
		expect(resolver1.getDescriptor()).andReturn(descriptor1);
		
		
		// key resolved
		expect(context.resolve(
				new AttributeDesignatorKey(
						AttributeCategories.SUBJECT_ACCESS, "username", StringType.STRING, null)))
				.andReturn(StringType.STRING.bagOf(StringType.STRING.create("testUser")));
		
		Capture<ResolverContext> resolverContext1 = new Capture<ResolverContext>();
		expect(cache.getAttributes(capture(resolverContext1))).andReturn(null);
		
		Capture<ResolverContext> ctx = new Capture<ResolverContext>();
		
		AttributeSet result = new AttributeSet(descriptor1, values);
		
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
	public void testAttributeResolutionWhenMatchingAttributeResolverFoundResolverResultsIsNotCachable() 
		throws Exception
	{
		Map<String, BagOfAttributeExp> values = ImmutableMap.of(
				"testAttributeId3", 
				StringType.STRING.bagOf(StringType.STRING.create("v1")));
		
		AttributeDesignatorKey ref = new AttributeDesignatorKey(
				AttributeCategories.SUBJECT_ACCESS,"testAttributeId3", StringType.STRING, null);
		
		// attribute resolver found
		expect(registry.getMatchingAttributeResolvers(context, ref)).andReturn(ImmutableList.of(resolver1));
		expect(resolver1.getDescriptor()).andReturn(deascriptor1NoCache);
				
		// key resolved
		expect(context.resolve(
				new AttributeDesignatorKey(
						AttributeCategories.SUBJECT_ACCESS, "username", StringType.STRING, null)))
				.andReturn(StringType.STRING.bagOf(StringType.STRING.create("testUser")));
		
		context.setResolvedDesignatorValue(
				new AttributeDesignatorKey(
						AttributeCategories.SUBJECT_ACCESS, 
						"testAttributeId4", 
						IntegerType.INTEGER, null), 
						IntegerType.INTEGER.emptyBag());
		
		context.setResolvedDesignatorValue(
				new AttributeDesignatorKey(
						AttributeCategories.SUBJECT_ACCESS, 
						"testAttributeId3", 
						StringType.STRING, null), 
						StringType.STRING.bagOf(StringType.STRING.create("v1")));
			
		
		context.setDecisionCacheTTL(deascriptor1NoCache.getPreferreredCacheTTL());
		
		Capture<ResolverContext> ctx = new Capture<ResolverContext>();
		
		AttributeSet result = new AttributeSet(deascriptor1NoCache, values);
		
		expect(resolver1.resolve(capture(ctx))).andReturn(result);
		
		control.replay();
		
		BagOfAttributeExp v = pip.resolve(context, ref);
		assertEquals(StringType.STRING.bagOf(StringType.STRING.create("v1")), v);

		control.verify();
	}
}