package com.artagon.xacml.v3.spi.pip;

import static com.artagon.xacml.v3.types.BooleanType.BOOLEAN;
import static com.artagon.xacml.v3.types.IntegerType.INTEGER;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.AttributeCategories;
import com.artagon.xacml.v3.AttributeDesignatorKey;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.pdp.RequestContextCallback;
import com.artagon.xacml.v3.spi.CacheProvider;

public class DefaultPolicyInformationPointTest 
{
	private DefaultPolicyInformationPoint pip;
	
	private CacheProvider attributeCache;
	private CacheProvider contentCache;
	@Before
	public void init()
	{
		this.attributeCache = createStrictMock(CacheProvider.class);
		this.contentCache = createStrictMock(CacheProvider.class);
		DefaultPolicyInformationPoint pip = new DefaultPolicyInformationPoint(attributeCache, contentCache);
		this.pip = pip;
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testAddResolversWithTheSameAttributeIdDifferentAttributeDataTypes()
	{
		AttributeResolver r1 = createStrictMock(AttributeResolver.class);
		AttributeResolver r2 = createStrictMock(AttributeResolver.class);
	
		AttributeResolverDescriptor d1 = AttributeResolverDescriptorBuilder.create("id", "TestResolver", 
				AttributeCategories.ACTION).
		attribute("test1", BOOLEAN).build();
		AttributeResolverDescriptor d2 = AttributeResolverDescriptorBuilder.create("id1", "TestResolver", "TestIssuer", 
				AttributeCategories.ACTION).
		attribute("test1", INTEGER).build();
		
		expect(r1.getDescriptor()).andReturn(d1).times(2);
		expect(r2.getDescriptor()).andReturn(d2).times(2);
		
		replay(r1, r2, attributeCache, contentCache);
		
		pip.addResolver(r1);
		pip.addResolver(r2);
		
		verify(r1, r2, attributeCache, contentCache);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testAddResolversWithTheSameAttributeIdTheSameTypeDifferentIssuers()
	{
		AttributeResolver r1 = createStrictMock(AttributeResolver.class);
		AttributeResolver r2 = createStrictMock(AttributeResolver.class);
	
		
		AttributeResolverDescriptor d1 = AttributeResolverDescriptorBuilder.create("id", "TestResolver", "TestIssuer", 
				AttributeCategories.ACTION)
		.attribute("test1", BOOLEAN).build();
		AttributeResolverDescriptor d2 = AttributeResolverDescriptorBuilder.create("id1", "Test Resolver", 
				AttributeCategories.ACTION)
		.attribute("test1", BOOLEAN).build();
		
		expect(r1.getDescriptor()).andReturn(d1).times(2);
		expect(r2.getDescriptor()).andReturn(d2).times(2);
		
		replay(r1, r2, attributeCache, contentCache);
		
		pip.addResolver(r1);
		pip.addResolver(r2);
		
		verify(r1, r2, attributeCache, contentCache);
	}
	
	@Test
	public void testAddResolversDifferentCategoryTheSameAttributeId()
	{
		AttributeResolver r1 = createStrictMock(AttributeResolver.class);
		AttributeResolver r2 = createStrictMock(AttributeResolver.class);
	
		AttributeResolverDescriptor d1 = AttributeResolverDescriptorBuilder.create(
				"id", "TestResolver", "test", AttributeCategories.ACTION)
		.attribute("test1", BOOLEAN).build();
		AttributeResolverDescriptor d2 = AttributeResolverDescriptorBuilder.create(
				"id1", "Test Resolver", AttributeCategories.ACTION)
		.attribute("test1", BOOLEAN).build();
		
		expect(r1.getDescriptor()).andReturn(d1).times(2);
		expect(r2.getDescriptor()).andReturn(d2).times(2);
		
		replay(r1, r2, attributeCache, contentCache);
		
		pip.addResolver(r1);
		pip.addResolver(r2);
		
		verify(r1, r2, attributeCache, contentCache);
	}
	
	@Test
	public void testResolveAttributeViaRootResolver() throws Exception
	{
		AttributeResolver r1 = createStrictMock(AttributeResolver.class);
		
		EvaluationContext context = createStrictMock(EvaluationContext.class);
		RequestContextCallback callback = createStrictMock(RequestContextCallback.class);
		Policy p = createStrictMock(Policy.class);
		
		AttributeDesignatorKey ref = new AttributeDesignatorKey(
				AttributeCategories.ACTION, "testAttributeId", BOOLEAN, null);
		
	
		AttributeResolverDescriptor d1 = AttributeResolverDescriptorBuilder.create("TestResolver", "test", 
				AttributeCategories.ACTION)
				.attribute("testAttributeId", BOOLEAN).build();
		 
		expect(context.getCurrentPolicy()).andReturn(p);
		expect(p.getId()).andReturn("testPolicyId");
		expect(context.getParentContext()).andReturn(null);
		
		expect(r1.getDescriptor()).andReturn(d1).times(3);

		replay(r1, context, callback, p, attributeCache, contentCache);
		
		
		pip.addResolver(r1);
		assertEquals(BOOLEAN.bagOf(BOOLEAN.create(true)), pip.resolve(context, ref));
		
		verify(r1, context, callback, p, attributeCache, contentCache);
	}
}