package com.artagon.xacml.v30.spi.pip;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.artagon.xacml.v30.AttributeCategories;
import com.artagon.xacml.v30.AttributeDesignatorKey;
import com.artagon.xacml.v30.BagOfAttributeValues;
import com.artagon.xacml.v30.types.IntegerType;
import com.artagon.xacml.v30.types.StringType;

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
	}
	
	@Test
	public void testAttributeSetWithIssuer()
	{
		AttributeSet v = new AttributeSet(withIssuer);
		assertNotNull(v.get("testId1"));
		assertNotNull(v.get("testId2"));
		BagOfAttributeValues v1 = v.get(
				new AttributeDesignatorKey(
						AttributeCategories.SUBJECT_ACCESS, "testId1", IntegerType.INTEGER, "issuer"));
		assertNotNull(v1);
		assertTrue(v1.isEmpty());
		assertEquals(IntegerType.INTEGER, v1.getDataType());
		
		BagOfAttributeValues v2 = v.get(
				new AttributeDesignatorKey(
						AttributeCategories.SUBJECT_ACCESS, "testId1", IntegerType.INTEGER, null));
		assertNotNull(v2);
		assertTrue(v1.isEmpty());
		assertEquals(IntegerType.INTEGER, v1.getDataType());
	}
	
	@Test
	public void testAttributeSetWithNoIssuer()
	{
		AttributeSet v = new AttributeSet(noIssuer);
		assertNotNull(v.get("testId1"));
		assertNotNull(v.get("testId2"));
		BagOfAttributeValues v1 = v.get(
				new AttributeDesignatorKey(
						AttributeCategories.SUBJECT_ACCESS, "testId1", IntegerType.INTEGER, null));
		assertNotNull(v1);
		assertTrue(v1.isEmpty());
		assertEquals(IntegerType.INTEGER, v1.getDataType());
	}
	
}
