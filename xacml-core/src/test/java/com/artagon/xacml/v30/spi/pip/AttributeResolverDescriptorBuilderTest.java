package com.artagon.xacml.v30.spi.pip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.pdp.AttributeDesignatorKey;
import com.artagon.xacml.v30.types.IntegerType;
import com.artagon.xacml.v30.types.StringType;
import com.google.common.collect.Iterables;

public class AttributeResolverDescriptorBuilderTest 
{
	
	@Test(expected=IllegalArgumentException.class)
	public void testBuildDescriptiorWithContextKeyReferringToThisDescriptor()
	{
		AttributeResolverDescriptorBuilder.builder(
				"id", "name", "issuer", AttributeCategories.SUBJECT_ACCESS)
		.attribute("testId1", IntegerType.INTEGER)
		.attribute("testId2", StringType.STRING)
		.requestContextKey(AttributeCategories.SUBJECT_ACCESS, "testId1", IntegerType.INTEGER)
		.build();
		
		
	}
	
	
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
		assertEquals(2, byKey.size());
	
		assertNotNull(byKey.get(
				new AttributeDesignatorKey(AttributeCategories.SUBJECT_ACCESS, "testId1", IntegerType.INTEGER, "issuer")));
		assertNotNull(byKey.get(
				new AttributeDesignatorKey(AttributeCategories.SUBJECT_ACCESS, "testId2", StringType.STRING, "issuer")));
	
		
		Iterable<AttributeDesignatorKey> keys = d.getAttributesByKey().keySet();
		
		assertEquals(new AttributeDesignatorKey(AttributeCategories.SUBJECT_ACCESS, "testId1", IntegerType.INTEGER, "issuer"), 
				Iterables.get(keys, 0));
		assertEquals(new AttributeDesignatorKey(AttributeCategories.SUBJECT_ACCESS, "testId2", StringType.STRING, "issuer"), 
				Iterables.get(keys, 1));
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
		
		
		Iterable<AttributeDesignatorKey> keys = d.getAttributesByKey().keySet();
		
		assertEquals(new AttributeDesignatorKey(AttributeCategories.SUBJECT_ACCESS, "testId1", IntegerType.INTEGER, null), 
				Iterables.get(keys, 0));
		assertEquals(new AttributeDesignatorKey(AttributeCategories.SUBJECT_ACCESS, "testId2", StringType.STRING, null), 
				Iterables.get(keys, 1));
	}
}
