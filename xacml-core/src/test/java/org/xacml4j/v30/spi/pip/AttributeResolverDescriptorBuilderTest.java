package org.xacml4j.v30.spi.pip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;
import org.xacml4j.v30.AttributeCategories;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.types.IntegerType;
import org.xacml4j.v30.types.StringType;

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
		
		AttributeDesignatorKey.Builder key = AttributeDesignatorKey
				.builder()
				.category(AttributeCategories.SUBJECT_ACCESS)
				.attributeId("testId1")
				.dataType(IntegerType.INTEGER);
		
		assertTrue(d.canResolve(key.build()));
		assertTrue(d.canResolve(key.issuer("issuer").build()));
		assertFalse(d.canResolve(key.dataType(StringType.STRING).build()));
		
		Map<AttributeDesignatorKey, AttributeDescriptor> byKey = d.getAttributesByKey();
		assertEquals(2, byKey.size());
	
		AttributeDesignatorKey.Builder key0 = AttributeDesignatorKey.builder().category(AttributeCategories.SUBJECT_ACCESS).attributeId("testId1").dataType(IntegerType.INTEGER).issuer("issuer");
		
		AttributeDesignatorKey.Builder key1 = AttributeDesignatorKey.builder().category(AttributeCategories.SUBJECT_ACCESS).attributeId("testId2").dataType(StringType.STRING).issuer("issuer");
		
		assertNotNull(byKey.get(key0.build()));
		assertNotNull(byKey.get(key1.build()));
	
		
		Iterable<AttributeDesignatorKey> keys = d.getAttributesByKey().keySet();
		
		assertEquals(key0.build(), Iterables.get(keys, 0));
		assertEquals(key1.build(), Iterables.get(keys, 1));
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
		
		AttributeDesignatorKey.Builder key = AttributeDesignatorKey
				.builder()
				.category(AttributeCategories.SUBJECT_ACCESS)
				.attributeId("testId1")
				.dataType(IntegerType.INTEGER);
		
		assertTrue(d.canResolve(key.build()));
		assertFalse(d.canResolve(key.issuer("issuer").build()));
		assertFalse(d.canResolve(key.dataType(StringType.STRING).build()));
		
		
		Iterable<AttributeDesignatorKey> keys = d.getAttributesByKey().keySet();
		
		AttributeDesignatorKey.Builder key0 = AttributeDesignatorKey.builder().category(AttributeCategories.SUBJECT_ACCESS).attributeId("testId1").dataType(IntegerType.INTEGER);
		AttributeDesignatorKey.Builder key1 = AttributeDesignatorKey.builder().category(AttributeCategories.SUBJECT_ACCESS).attributeId("testId2").dataType(StringType.STRING);
		
		assertEquals(key0.build(), Iterables.get(keys, 0));
		assertEquals(key1.build(), Iterables.get(keys, 1));
	}
}
