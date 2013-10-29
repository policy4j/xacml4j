package org.xacml4j.v30.spi.pip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.AttributeCategories;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagOfAttributeExp;
import org.xacml4j.v30.types.IntegerType;
import org.xacml4j.v30.types.StringType;

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
		AttributeDesignatorKey.Builder key =
				AttributeDesignatorKey
				.builder()
				.category(AttributeCategories.SUBJECT_ACCESS)
				.attributeId("testId1")
				.dataType(IntegerType.INTEGER)
				.issuer("issuer");

		AttributeDesignatorKey.Builder key1 =
				AttributeDesignatorKey
				.builder()
				.category(AttributeCategories.SUBJECT_ACCESS)
				.attributeId("testId2")
				.issuer("issuer")
				.dataType(StringType.STRING);

		AttributeSet v = AttributeSet
				.builder(withIssuer)
				.build();
		assertNotNull(v.get("testId1"));
		assertNotNull(v.get("testId2"));
		BagOfAttributeExp v1 = v.get(key.build());
		assertNotNull(v1);
		assertTrue(v1.isEmpty());
		assertEquals(IntegerType.INTEGER, v1.getDataType());

		BagOfAttributeExp v2 = v.get(key.build());
		assertNotNull(v2);
		assertTrue(v1.isEmpty());
		assertEquals(IntegerType.INTEGER, v1.getDataType());

		Iterable<AttributeDesignatorKey> keys = v.getAttributeKeys();
		assertEquals(key.build(), Iterables.get(keys, 0));
		assertEquals(key1.build(), Iterables.get(keys, 1));
	}

	@Test
	public void testAttributeSetWithNoIssuer()
	{
		AttributeDesignatorKey.Builder key =
				AttributeDesignatorKey
				.builder()
				.category(AttributeCategories.SUBJECT_ACCESS)
				.attributeId("testId1")
				.dataType(IntegerType.INTEGER);

		AttributeSet v = AttributeSet
				.builder(noIssuer)
				.build();
		assertNotNull(v.get("testId1"));
		assertNotNull(v.get("testId2"));
		BagOfAttributeExp v1 = v.get(key.build());
		assertNotNull(v1);
		assertTrue(v1.isEmpty());
		assertEquals(IntegerType.INTEGER, v1.getDataType());
	}

	@Test
	public void testIsEmpty(){
		AttributeSet v = AttributeSet
				.builder(noIssuer)
				.build();
		assertTrue(v.isEmpty());
	}

}
