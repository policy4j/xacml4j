package com.artagon.xacml.v3.spi.pip;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.sdk.AttributeResolverDescriptorBuilder;
import com.artagon.xacml.v3.types.XacmlDataTypes;

public class DefaultPolicyInformationPointTest 
{
	private DefaultPolicyInformationPoint pip;
	
	
	@Before
	public void init()
	{
		DefaultPolicyInformationPoint pip = new DefaultPolicyInformationPoint();
	
		this.pip = pip;
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testAddResolversWithTheSameAttributeIdDifferentAttributeDataTypes()
	{
		AttributeResolver r1 = createStrictMock(AttributeResolver.class);
		AttributeResolver r2 = createStrictMock(AttributeResolver.class);
	
		AttributeResolverDescriptor d1 = AttributeResolverDescriptorBuilder.create("TestResolver").
		attribute(AttributeCategoryId.ACTION, "test1", XacmlDataTypes.BOOLEAN).build();
		AttributeResolverDescriptor d2 = AttributeResolverDescriptorBuilder.create("TestResolver", "TestIssuer").
		attribute(AttributeCategoryId.ACTION, "test1", XacmlDataTypes.INTEGER).build();
		
		expect(r1.getDescriptor()).andReturn(d1).times(2);
		expect(r2.getDescriptor()).andReturn(d2).times(2);
		
		replay(r1, r2);
		
		pip.addResolver(r1);
		pip.addResolver(r2);
		
		verify(r1, r2);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testAddResolversWithTheSameAttributeIdTheSameTypeDifferentIssuers()
	{
		AttributeResolver r1 = createStrictMock(AttributeResolver.class);
		AttributeResolver r2 = createStrictMock(AttributeResolver.class);
	
		
		AttributeResolverDescriptor d1 = AttributeResolverDescriptorBuilder.create("TestResolver", "TestIssuer")
		.attribute(AttributeCategoryId.ACTION, "test1", XacmlDataTypes.BOOLEAN).build();
		AttributeResolverDescriptor d2 = AttributeResolverDescriptorBuilder.create("Test Resolver").
		attribute(AttributeCategoryId.ACTION, "test1", XacmlDataTypes.BOOLEAN).build();
		
		expect(r1.getDescriptor()).andReturn(d1).times(2);
		expect(r2.getDescriptor()).andReturn(d2).times(2);
		
		replay(r1, r2);
		
		pip.addResolver(r1);
		pip.addResolver(r2);
		
		verify(r1, r2);
	}
	
	@Test
	public void testAddResolversDifferentCategoryTheSameAttributeId()
	{
		AttributeResolver r1 = createStrictMock(AttributeResolver.class);
		AttributeResolver r2 = createStrictMock(AttributeResolver.class);
	
		AttributeResolverDescriptor d1 = AttributeResolverDescriptorBuilder.create("TestResolver", "test").
		attribute(AttributeCategoryId.ACTION, "test1", XacmlDataTypes.BOOLEAN).build();
		AttributeResolverDescriptor d2 = AttributeResolverDescriptorBuilder.create("TestResolver").
		attribute(AttributeCategoryId.ENVIRONMENT, "test1", XacmlDataTypes.BOOLEAN).build();
		
		expect(r1.getDescriptor()).andReturn(d1).times(2);
		expect(r2.getDescriptor()).andReturn(d2).times(2);
		
		replay(r1, r2);
		
		pip.addResolver(r1);
		pip.addResolver(r2);
		
		verify(r1, r2);
	}
}