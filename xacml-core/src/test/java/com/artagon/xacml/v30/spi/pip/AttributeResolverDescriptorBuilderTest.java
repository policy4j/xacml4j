package com.artagon.xacml.v30.spi.pip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import com.artagon.xacml.v30.AttributeDesignatorKey;
import com.artagon.xacml.v30.core.AttributeCategories;
import com.artagon.xacml.v30.types.IntegerType;
import com.artagon.xacml.v30.types.StringType;

public class AttributeResolverDescriptorBuilderTest 
{
	@Test
	public void testBuildDescriptorWithIssuer()
	{
		AttributeResolverDescriptor d = AttributeResolverDescriptorBuilder.builder(
				"id", "name", "issuer", AttributeCategories.SUBJECT_ACCESS)
		.attribute("testId1", IntegerType.INTEGER)
		.attribute("testId2", StringType.STRING).build();
		assertEquals("id", d.getId());
		assertEquals("name", d.getName());
		assertEquals("issuer", d.getIssuer());
		assertEquals(AttributeCategories.SUBJECT_ACCESS, d.getCategory());
		assertTrue(d.canResolve(
				new AttributeDesignatorKey(
						AttributeCategories.SUBJECT_ACCESS, "testId1", IntegerType.INTEGER, null)));
		assertTrue(d.canResolve(
				new AttributeDesignatorKey(
						AttributeCategories.SUBJECT_ACCESS, "testId1", IntegerType.INTEGER, "issuer")));
		assertFalse(d.canResolve(
				new AttributeDesignatorKey(
						AttributeCategories.SUBJECT_ACCESS, "testId1", StringType.STRING, "issuer")));
		
		Map<AttributeDesignatorKey, AttributeDescriptor> byKey = d.getAttributesByKey();
		assertEquals(4, byKey.size());
		assertNotNull(byKey.get(
				new AttributeDesignatorKey(AttributeCategories.SUBJECT_ACCESS, "testId1", IntegerType.INTEGER, null)));
		assertNotNull(byKey.get(
				new AttributeDesignatorKey(AttributeCategories.SUBJECT_ACCESS, "testId1", IntegerType.INTEGER, "issuer")));
		assertNotNull(byKey.get(
				new AttributeDesignatorKey(AttributeCategories.SUBJECT_ACCESS, "testId2", StringType.STRING, "issuer")));
		assertNotNull(byKey.get(
				new AttributeDesignatorKey(AttributeCategories.SUBJECT_ACCESS, "testId2", StringType.STRING, null)));
	}
	
	@Test
	public void testBuildDescriptorWithIssuerNull()
	{
		AttributeResolverDescriptor d = AttributeResolverDescriptorBuilder.builder(
				"id", "name", null, AttributeCategories.SUBJECT_ACCESS)
		.attribute("testId1", IntegerType.INTEGER)
		.attribute("testId2", StringType.STRING).build();
		assertEquals("id", d.getId());
		assertEquals("name", d.getName());
		assertNull(d.getIssuer());
		assertEquals(AttributeCategories.SUBJECT_ACCESS, d.getCategory());
		assertTrue(d.canResolve(
				new AttributeDesignatorKey(AttributeCategories.SUBJECT_ACCESS, "testId1", IntegerType.INTEGER, null)));
		assertFalse(d.canResolve(
				new AttributeDesignatorKey(AttributeCategories.SUBJECT_ACCESS, "testId1", IntegerType.INTEGER, "issuer")));
		assertFalse(d.canResolve(
				new AttributeDesignatorKey(AttributeCategories.SUBJECT_ACCESS, "testId1", StringType.STRING, "issuer")));
		
		Map<AttributeDesignatorKey, AttributeDescriptor> byKey = d.getAttributesByKey();
	}
}
