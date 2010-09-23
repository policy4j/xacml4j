package com.artagon.xacml.v3.spi.pip;

import static org.easymock.EasyMock.createStrictMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.sdk.AnnotatedAttributeResolver;
import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.StringType;
import com.artagon.xacml.v3.types.XacmlDataTypes;

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
		assertTrue(d.getSupportedCategores().contains(AttributeCategoryId.RESOURCE));
		assertTrue(d.isCategorySupported(AttributeCategoryId.RESOURCE));
		assertTrue(d.isCategorySupported(AttributeCategoryId.ACTION));
		assertTrue(d.getSupportedCategores().contains(AttributeCategoryId.SUBJECT_ACCESS));
		assertTrue(d.isCategorySupported(AttributeCategoryId.SUBJECT_ACCESS));
		AttributeDescriptor attr1 = d.getAttributeDescriptor(AttributeCategoryId.RESOURCE, "testId1");
		assertNotNull(attr1);
		assertEquals("testId1", attr1.getAttributeId());
		assertEquals(XacmlDataTypes.STRING.getDataType(), attr1.getDataType());
		AttributeDescriptor attr2 = d.getAttributeDescriptor(AttributeCategoryId.SUBJECT_ACCESS, "testId2");
		assertNotNull(attr2);
		assertEquals("testId2", attr2.getAttributeId());
		assertEquals(XacmlDataTypes.INTEGER.getDataType(), attr2.getDataType());
	}
	
	@Test
	public void testInvokeResolverWithValidParameters() throws Exception
	{
		PolicyInformationPointContext context = createStrictMock(PolicyInformationPointContext.class);
		
		BagOfAttributeValues v1 = resolver.resolve(context, 
				AttributeCategoryId.RESOURCE, "testId1", XacmlDataTypes.STRING.getDataType(), "testIssuer");
		assertEquals(StringType.Factory.emptyBag(), v1);
		
		v1 = resolver.resolve(context, 
				AttributeCategoryId.ACTION, "testId1", XacmlDataTypes.STRING.getDataType(), "testIssuer");
		assertEquals(StringType.Factory.emptyBag(), v1);
		
		BagOfAttributeValues v2 = resolver.resolve(context, 
				AttributeCategoryId.SUBJECT_ACCESS, "testId2", XacmlDataTypes.INTEGER.getDataType(), "testIssuer");
		assertEquals(IntegerType.Factory.bagOf(IntegerType.Factory.create(1)), v2);
	}
	
	@Test
	public void testInvokeResolverWithNullIssuer() throws Exception
	{
		PolicyInformationPointContext context = createStrictMock(PolicyInformationPointContext.class);
		
		BagOfAttributeValues v1 = resolver.resolve(context, 
				AttributeCategoryId.RESOURCE, "testId1", XacmlDataTypes.STRING.getDataType(), null);
		assertEquals(StringType.Factory.emptyBag(), v1);
		
		BagOfAttributeValues v2 = resolver.resolve(context, 
				AttributeCategoryId.SUBJECT_ACCESS, "testId2", XacmlDataTypes.INTEGER.getDataType(), null);
		assertEquals(IntegerType.Factory.bagOf(IntegerType.Factory.create(1)), v2);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvokeResolverWithWrongIssuer() throws Exception
	{
		PolicyInformationPointContext context = createStrictMock(PolicyInformationPointContext.class);
		
		resolver.resolve(context, 
				AttributeCategoryId.RESOURCE, "testId1", XacmlDataTypes.STRING.getDataType(), "TestWrongIssuer");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvokeResolverWithWrongCategory() throws Exception
	{
		PolicyInformationPointContext context = createStrictMock(PolicyInformationPointContext.class);
		
		resolver.resolve(context, 
				AttributeCategoryId.ACTION, "testId1", XacmlDataTypes.STRING.getDataType(), "TestIssuer");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvokeResolverWithWrongAttributeId() throws Exception
	{
		PolicyInformationPointContext context = createStrictMock(PolicyInformationPointContext.class);	
		resolver.resolve(context, 
				AttributeCategoryId.RESOURCE, "testId1Wrong", XacmlDataTypes.STRING.getDataType(), "TestIssuer");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvokeResolverWithWrongAttributeDataType() throws Exception
	{
		PolicyInformationPointContext context = createStrictMock(PolicyInformationPointContext.class);
		
		resolver.resolve(context, 
				AttributeCategoryId.RESOURCE, "testId1", XacmlDataTypes.INTEGER.getDataType(), "TestIssuer");
	}
}
