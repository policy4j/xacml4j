package com.artagon.xacml.v3.spi.pip.impl;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.AttributeCategories;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.RequestContextCallback;
import com.artagon.xacml.v3.spi.pip.AttributeDescriptor;
import com.artagon.xacml.v3.spi.pip.AttributeResolver;
import com.artagon.xacml.v3.spi.pip.AttributeResolverDescriptor;
import com.artagon.xacml.v3.spi.pip.PolicyInformationPointContext;
import com.artagon.xacml.v3.spi.pip.impl.AnnotatedAttributeResolver;
import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.StringType;

public class AnnotatedAttributeResolverBuilderTest 
{
	private PolicyInformationPointContext context;
	private AttributeResolver resolver;
	private RequestContextCallback callback;
	
	@Before
	public void init(){
		this.resolver = AnnotatedAttributeResolver.create(new TestAnnotatedResolver());
		this.context = createStrictMock(PolicyInformationPointContext.class);
		this.callback = createStrictMock(RequestContextCallback.class);
	}
	
	@Test
	public void testBuildResolver()
	{
		AttributeResolverDescriptor d = resolver.getDescriptor();
		assertNotNull(d);
		assertTrue(d.getSupportedCategores().contains(AttributeCategories.RESOURCE));
		assertTrue(d.isCategorySupported(AttributeCategories.RESOURCE));
		assertTrue(d.isCategorySupported(AttributeCategories.ACTION));
		assertTrue(d.getSupportedCategores().contains(AttributeCategories.SUBJECT_ACCESS));
		assertTrue(d.isCategorySupported(AttributeCategories.SUBJECT_ACCESS));
		AttributeDescriptor attr1 = d.getAttributeDescriptor(AttributeCategories.RESOURCE, "testId1");
		assertNotNull(attr1);
		assertEquals("testId1", attr1.getAttributeId());
		assertEquals(StringType.STRING, attr1.getDataType());
		AttributeDescriptor attr2 = d.getAttributeDescriptor(AttributeCategories.SUBJECT_ACCESS, "testId2");
		assertNotNull(attr2);
		assertEquals("testId2", attr2.getAttributeId());
		assertEquals(IntegerType.INTEGER, attr2.getDataType());
	}
	
	@Test
	public void testInvokeResolverWithValidParameters() throws Exception
	{
		
		BagOfAttributeValues v1 = resolver.resolve(context, 
				AttributeCategories.RESOURCE, "testId1", StringType.STRING, "testIssuer");
		assertEquals(StringType.STRING.emptyBag(), v1);
		
		v1 = resolver.resolve(context, 
				AttributeCategories.ACTION, "testId1", StringType.STRING, "testIssuer");
		assertEquals(StringType.STRING.emptyBag(), v1);
		
		BagOfAttributeValues v2 = resolver.resolve(context, 
				AttributeCategories.SUBJECT_ACCESS, "testId2", IntegerType.INTEGER, "testIssuer");
		assertEquals(IntegerType.INTEGER.bagOf(IntegerType.INTEGER.create(1)), v2);
	}
	
	@Test
	public void testInvokeResolverWithValidParametersWithRequestKeys() throws Exception
	{
		expect(context.getRequestContextCallback()).andReturn(callback);
		expect(callback.getAttributeValues(AttributeCategories.SUBJECT_ACCESS, "username", 
				StringType.STRING, null)).
				andReturn(StringType.STRING.bagOf(StringType.STRING.create("test")));
		expect(context.getCategory()).andReturn(AttributeCategories.SUBJECT_ACCESS);
		replay(context, callback);
		BagOfAttributeValues v = resolver.resolve(context, 
				AttributeCategories.SUBJECT_ACCESS, "testId3", StringType.STRING, "testIssuer");
		assertEquals(StringType.STRING.bagOf(StringType.STRING.create("test")), v);
		verify(context, callback);
	}
	
	@Test
	public void testInvokeResolverWithNullIssuer() throws Exception
	{
		PolicyInformationPointContext context = createStrictMock(PolicyInformationPointContext.class);
		
		BagOfAttributeValues v1 = resolver.resolve(context, 
				AttributeCategories.RESOURCE, "testId1", StringType.STRING, null);
		assertEquals(StringType.STRING.emptyBag(), v1);
		
		BagOfAttributeValues v2 = resolver.resolve(context, 
				AttributeCategories.SUBJECT_ACCESS, "testId2", IntegerType.INTEGER, null);
		assertEquals(IntegerType.INTEGER.bagOf(IntegerType.INTEGER.create(1)), v2);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvokeResolverWithWrongIssuer() throws Exception
	{
		PolicyInformationPointContext context = createStrictMock(PolicyInformationPointContext.class);
		
		resolver.resolve(context, 
				AttributeCategories.RESOURCE, "testId1", StringType.STRING, "TestWrongIssuer");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvokeResolverWithWrongCategory() throws Exception
	{
		PolicyInformationPointContext context = createStrictMock(PolicyInformationPointContext.class);
		
		resolver.resolve(context, 
				AttributeCategories.ACTION, "testId1", StringType.STRING, "TestIssuer");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvokeResolverWithWrongAttributeId() throws Exception
	{
		PolicyInformationPointContext context = createStrictMock(PolicyInformationPointContext.class);	
		resolver.resolve(context, 
				AttributeCategories.RESOURCE, "testId1Wrong", StringType.STRING, "TestIssuer");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvokeResolverWithWrongAttributeDataType() throws Exception
	{
		PolicyInformationPointContext context = createStrictMock(PolicyInformationPointContext.class);
		
		resolver.resolve(context, 
				AttributeCategories.RESOURCE, "testId1", IntegerType.INTEGER, "TestIssuer");
	}
}
