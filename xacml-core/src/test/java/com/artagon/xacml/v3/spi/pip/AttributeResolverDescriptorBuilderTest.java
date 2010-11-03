package com.artagon.xacml.v3.spi.pip;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.artagon.xacml.v3.AttributeCategories;
import com.artagon.xacml.v3.AttributeDesignatorKey;
import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.StringType;

public class AttributeResolverDescriptorBuilderTest 
{
	@Test
	public void testBuildDescriptor()
	{
		AttributeResolverDescriptor d = AttributeResolverDescriptorBuilder.create(
				"id", "name", "issuer", AttributeCategories.SUBJECT_ACCESS)
		.attribute("testId1", IntegerType.INTEGER)
		.attribute("testId2", StringType.STRING).build();
		assertEquals("id", d.getId());
		assertEquals("name", d.getName());
		assertEquals("issuer", d.getIssuer());
		assertEquals(AttributeCategories.SUBJECT_ACCESS, d.getCategory());
		assertTrue(d.canResolve(
				new AttributeDesignatorKey(AttributeCategories.SUBJECT_ACCESS, "testId1", IntegerType.INTEGER, null)));
		assertTrue(d.canResolve(
				new AttributeDesignatorKey(AttributeCategories.SUBJECT_ACCESS, "testId1", IntegerType.INTEGER, "issuer")));
		assertFalse(d.canResolve(
				new AttributeDesignatorKey(AttributeCategories.SUBJECT_ACCESS, "testId1", StringType.STRING, "issuer")));
		
		
	}


}
