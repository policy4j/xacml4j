package com.artagon.xacml.v3.spi.pip;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.same;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.AttributeCategories;
import com.artagon.xacml.v3.AttributeDesignatorKey;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.spi.CacheProvider;
import com.artagon.xacml.v3.spi.PolicyInformationPoint;
import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.StringType;
import com.google.common.collect.ImmutableMap;

public class DefaultPolicyInformationPointTest 
{
	private PolicyInformationPoint pip;
	
	private ResolverRegistry registry;
	private AttributeResolver attributeResolver;
	private CacheProvider attributeCache;
	private CacheProvider contentCache;
	private EvaluationContext context;
	
	private AttributeResolverDescriptor descriptor;
	
	@Before
	public void init()
	{
		this.attributeCache = createStrictMock(CacheProvider.class);
		this.contentCache = createStrictMock(CacheProvider.class);
		this.registry = createStrictMock(ResolverRegistry.class);
		this.attributeResolver = createStrictMock(AttributeResolver.class);
		this.context = createStrictMock(EvaluationContext.class);
		this.pip = new DefaultPolicyInformationPoint(registry, attributeCache, contentCache);
		this.descriptor = AttributeResolverDescriptorBuilder
		.create("testId", "Test Resolver", AttributeCategories.SUBJECT_ACCESS)
		.cache(30)
		.attribute("testAttributeId1", StringType.STRING)
		.attribute("testAttributeId2", IntegerType.INTEGER)
		.designatorRef(AttributeCategories.SUBJECT_ACCESS, "username", StringType.STRING, null)
		.build();
	}
	
	
	@Test
	public void testAttributeResolutionWhenMatchingAttributeResolverFound() throws Exception
	{
		Map<String, BagOfAttributeValues> values = ImmutableMap.of(
				"testAttributeId1", StringType.STRING.bagOf(StringType.STRING.create("v1")));
		
		AttributeDesignatorKey ref = new AttributeDesignatorKey(
				AttributeCategories.SUBJECT_ACCESS,"testAttributeId1", StringType.STRING, null);
		
		expect(registry.getAttributeResolver(context, ref)).andReturn(attributeResolver);
		expect(attributeResolver.getDescriptor()).andReturn(descriptor);
		
		expect(context.resolve(
				new AttributeDesignatorKey(AttributeCategories.SUBJECT_ACCESS, "username", StringType.STRING, null)))
				.andReturn(StringType.STRING.bagOf(StringType.STRING.create("testUser")));
		
		Capture<DefaultPolicyInformationPoint.CacheKey> cacheKey1 = new Capture<DefaultPolicyInformationPoint.CacheKey>();
		expect(attributeCache.get(capture(cacheKey1))).andReturn(null);
		
		Capture<PolicyInformationPointContext> ctx = new Capture<PolicyInformationPointContext>();
		expect(attributeResolver.resolve(capture(ctx))).andReturn(values);
		
		Capture<DefaultPolicyInformationPoint.CacheKey> cacheKey2 = new Capture<DefaultPolicyInformationPoint.CacheKey>();
		attributeCache.put(capture(cacheKey2), same(values), eq(descriptor.getPreferreredCacheTTL()));
		
		expect(registry.getAttributeResolver(context, ref)).andReturn(attributeResolver);
		expect(attributeResolver.getDescriptor()).andReturn(descriptor);
		
		expect(context.resolve(
				new AttributeDesignatorKey(AttributeCategories.SUBJECT_ACCESS, "username", StringType.STRING, null)))
				.andReturn(StringType.STRING.bagOf(StringType.STRING.create("testUser")));
		
		Capture<DefaultPolicyInformationPoint.CacheKey> cacheKey3 = new Capture<DefaultPolicyInformationPoint.CacheKey>();
		expect(attributeCache.get(capture(cacheKey3))).andReturn(values);
		
		replay(registry, attributeResolver, attributeCache, context);
		
		BagOfAttributeValues v = pip.resolve(context, ref);
		assertEquals(StringType.STRING.bagOf(StringType.STRING.create("v1")), v);
		
		v = pip.resolve(context, ref);
		assertEquals(StringType.STRING.bagOf(StringType.STRING.create("v1")), v);
		
		assertEquals(cacheKey1.getValue(), cacheKey2.getValue());
		assertEquals(cacheKey2.getValue(), cacheKey3.getValue());
		verify(registry, attributeResolver, attributeCache, context);
	}
}