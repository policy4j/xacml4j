package com.artagon.xacml.v3.spi.pip;

import static org.easymock.EasyMock.createStrictMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.AttributeCategories;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.StringType;

public class AnnotatedAttributeResolverBuilderTest 
{
	private AttributeResolver resolver;
	
	@Before
	public void init(){
		this.resolver = AnnotatedAttributeResolver.create(new TestAnnotatedResolver());
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
		PolicyInformationPointContext context = createStrictMock(PolicyInformationPointContext.class);
		
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
