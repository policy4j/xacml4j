package com.artagon.xacml.v30.spi.pip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.AttributeDesignatorKey;
import com.artagon.xacml.v30.BagOfAttributeExp;
import com.artagon.xacml.v30.types.IntegerType;
import com.artagon.xacml.v30.types.StringType;
import com.google.common.collect.Iterables;

public class AttributeSetTest 
{
	private AttributeResolverDescriptor noIssuer;
	private AttributeResolverDescriptor withIssuer;
	
	@Before
	public void init(){
		this.noIssuer = AttributeResolverDescriptorBuilder.builder(
				"id", "name", null, AttributeCategories.SUBJECT_ACCESS)
				.attribute("testId1", IntegerType.INTEGER)
				.attribute("testId2", StringType.STRING)
				.build();
		this.withIssuer = AttributeResolverDescriptorBuilder.builder(
				"id", "name", "issuer", AttributeCategories.SUBJECT_ACCESS)
				.attribute("testId1", IntegerType.INTEGER)
				.attribute("testId2", StringType.STRING)
				.build();
		assertEquals("issuer", withIssuer.getIssuer());
	}
	
	@Test
	public void testAttributeSetWithIssuer()
	{
		AttributeSet v = new AttributeSet(withIssuer);
		assertNotNull(v.get("testId1"));
		assertNotNull(v.get("testId2"));
		BagOfAttributeExp v1 = v.get(
				new AttributeDesignatorKey(
						AttributeCategories.SUBJECT_ACCESS, "testId1", IntegerType.INTEGER, "issuer"));
		assertNotNull(v1);
		assertTrue(v1.isEmpty());
		assertEquals(IntegerType.INTEGER, v1.getDataType());
		
		BagOfAttributeExp v2 = v.get(
				new AttributeDesignatorKey(
						AttributeCategories.SUBJECT_ACCESS, "testId1", IntegerType.INTEGER, null));
		assertNotNull(v2);
		assertTrue(v1.isEmpty());
		assertEquals(IntegerType.INTEGER, v1.getDataType());
		
		Iterable<AttributeDesignatorKey> keys = v.getAttributeKeys();
		assertEquals(
				new AttributeDesignatorKey(
				AttributeCategories.SUBJECT_ACCESS, "testId1", IntegerType.INTEGER, withIssuer.getIssuer()), 
				Iterables.get(keys, 0));
		assertEquals(
				new AttributeDesignatorKey(
				AttributeCategories.SUBJECT_ACCESS, "testId2", StringType.STRING, withIssuer.getIssuer()), 
				Iterables.get(keys, 1));
	}
	
	@Test
	public void testAttributeSetWithNoIssuer()
	{
		AttributeSet v = new AttributeSet(noIssuer);
		assertNotNull(v.get("testId1"));
		assertNotNull(v.get("testId2"));
		BagOfAttributeExp v1 = v.get(
				new AttributeDesignatorKey(
						AttributeCategories.SUBJECT_ACCESS, "testId1", IntegerType.INTEGER, null));
		assertNotNull(v1);
		assertTrue(v1.isEmpty());
		assertEquals(IntegerType.INTEGER, v1.getDataType());
	}
	
	@Test
	public void testIsEmpty(){
		AttributeSet v = new AttributeSet(noIssuer);
		assertTrue(v.isEmpty());
	}
	
}
