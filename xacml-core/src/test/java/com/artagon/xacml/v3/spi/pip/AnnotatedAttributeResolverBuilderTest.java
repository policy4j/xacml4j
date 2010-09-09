package com.artagon.xacml.v3.spi.pip;

import org.junit.Test;

import com.artagon.xacml.v3.AttributeCategoryId;
import com.artagon.xacml.v3.types.XacmlDataTypes;

import static org.junit.Assert.*;

public class AnnotatedAttributeResolverBuilderTest 
{
	@Test
	public void testBuildResolver()
	{
		TestAnnotatedResolver r = new TestAnnotatedResolver();
		AnnotatedAttributeResolverBuilder b = new AnnotatedAttributeResolverBuilder();
		AttributeResolver resolver = b.build(r);
		AttributeResolverDescriptor d = resolver.getDescriptor();
		assertNotNull(d);
		assertTrue(d.getSupportedCategores().contains(AttributeCategoryId.RESOURCE));
		assertTrue(d.isCategorySupported(AttributeCategoryId.RESOURCE));
		assertTrue(d.getSupportedCategores().contains(AttributeCategoryId.SUBJECT_ACCESS));
		assertTrue(d.isCategorySupported(AttributeCategoryId.SUBJECT_ACCESS));
		AttributeDescriptor attr1 = d.getAttributeDescriptor(AttributeCategoryId.RESOURCE, "testId1");
		assertNotNull(attr1);
		assertEquals("testId1", attr1.getAttributeId());
		assertEquals(XacmlDataTypes.STRING.getType(), attr1.getDataType());
		AttributeDescriptor attr2 = d.getAttributeDescriptor(AttributeCategoryId.SUBJECT_ACCESS, "testId2");
		assertNotNull(attr2);
		assertEquals("testId2", attr2.getAttributeId());
		assertEquals(XacmlDataTypes.INTEGER.getType(), attr2.getDataType());
	}
}
