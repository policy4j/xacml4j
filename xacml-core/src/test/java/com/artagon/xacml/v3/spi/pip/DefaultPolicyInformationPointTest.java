package com.artagon.xacml.v3.spi.pip;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Map;

import org.easymock.Capture;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.AttributeCategories;
import com.artagon.xacml.v3.AttributeDesignatorKey;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.StringType;
import com.google.common.collect.ImmutableMap;

public class DefaultPolicyInformationPointTest 
{
	private PolicyInformationPoint pip;
	
	private ResolverRegistry registry;
	private AttributeResolver attributeResolver;
	private ResolverResultCacheProvider cache;
	private EvaluationContext context;
	
	private AttributeResolverDescriptor descriptor;
	private AttributeResolverDescriptor descriptorNoCache;
	
	private IMocksControl control;
	
	@Before
	public void init()
	{
		this.control = createControl();
		this.cache = control.createMock(ResolverResultCacheProvider.class);
		this.registry = control.createMock(ResolverRegistry.class);
		this.attributeResolver = control.createMock(AttributeResolver.class);
		this.context = control.createMock(EvaluationContext.class);
		this.pip = new DefaultPolicyInformationPoint(registry, cache);
		this.descriptor = AttributeResolverDescriptorBuilder
		.create("testId", "Test Resolver", AttributeCategories.SUBJECT_ACCESS)
		.cache(30)
		.attribute("testAttributeId1", StringType.STRING)
		.attribute("testAttributeId2", IntegerType.INTEGER)
		.designatorRef(AttributeCategories.SUBJECT_ACCESS, "username", StringType.STRING, null)
		.build();
		
		this.descriptorNoCache = AttributeResolverDescriptorBuilder
		.create("testId", "Test Resolver", AttributeCategories.SUBJECT_ACCESS)
		.noCache()
		.attribute("testAttributeId3", StringType.STRING)
		.attribute("testAttributeId4", IntegerType.INTEGER)
		.designatorRef(AttributeCategories.SUBJECT_ACCESS, "username", StringType.STRING, null)
		.build();
	}
	
	
	@Test
	public void testAttributeResolutionWhenMatchingAttributeResolverFoundResolverResultsIsCachable() throws Exception
	{
		Map<String, BagOfAttributeValues> values = ImmutableMap.of(
				"testAttributeId1", StringType.STRING.bagOf(StringType.STRING.create("v1")));
		
		AttributeDesignatorKey ref = new AttributeDesignatorKey(
				AttributeCategories.SUBJECT_ACCESS,"testAttributeId1", StringType.STRING, null);
		
		// attribute resolver found
		expect(registry.getAttributeResolver(context, ref)).andReturn(attributeResolver);
		expect(attributeResolver.getDescriptor()).andReturn(descriptor);
		
		
		// key resolved
		expect(context.resolve(
				new AttributeDesignatorKey(AttributeCategories.SUBJECT_ACCESS, "username", StringType.STRING, null)))
				.andReturn(StringType.STRING.bagOf(StringType.STRING.create("testUser")));
		
		Capture<ResolverContext> resolverContext1 = new Capture<ResolverContext>();
		expect(cache.getAttributes(capture(resolverContext1))).andReturn(null);
		
		Capture<ResolverContext> ctx = new Capture<ResolverContext>();
		
		AttributeSet result = new AttributeSet(descriptor, values);
		
		expect(attributeResolver.resolve(capture(ctx))).andReturn(result);
		
		Capture<ResolverContext> resolverContext2 = new Capture<ResolverContext>();

		cache.putAttributes(capture(resolverContext2), eq(result));
		
		
		control.replay();
		
		BagOfAttributeValues v = pip.resolve(context, ref);
		assertEquals(StringType.STRING.bagOf(StringType.STRING.create("v1")), v);
		assertSame(resolverContext1.getValue(), resolverContext2.getValue());

		control.verify();
	}
	
	@Test
	public void testAttributeResolutionWhenMatchingAttributeResolverFoundResolverResultsIsNotCachable() throws Exception
	{
		Map<String, BagOfAttributeValues> values = ImmutableMap.of(
				"testAttributeId3", StringType.STRING.bagOf(StringType.STRING.create("v1")));
		
		AttributeDesignatorKey ref = new AttributeDesignatorKey(
				AttributeCategories.SUBJECT_ACCESS,"testAttributeId3", StringType.STRING, null);
		
		// attribute resolver found
		expect(registry.getAttributeResolver(context, ref)).andReturn(attributeResolver);
		expect(attributeResolver.getDescriptor()).andReturn(descriptorNoCache);
				
		// key resolved
		expect(context.resolve(
				new AttributeDesignatorKey(AttributeCategories.SUBJECT_ACCESS, "username", StringType.STRING, null)))
				.andReturn(StringType.STRING.bagOf(StringType.STRING.create("testUser")));
		
		Capture<ResolverContext> ctx = new Capture<ResolverContext>();
		
		AttributeSet result = new AttributeSet(descriptorNoCache, values);
		
		expect(attributeResolver.resolve(capture(ctx))).andReturn(result);
				
		control.replay();
		
		BagOfAttributeValues v = pip.resolve(context, ref);
		assertEquals(StringType.STRING.bagOf(StringType.STRING.create("v1")), v);

		control.verify();
	}
}