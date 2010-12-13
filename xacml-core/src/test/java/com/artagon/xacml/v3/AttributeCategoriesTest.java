package com.artagon.xacml.v3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class AttributeCategoriesTest 
{
	@Test
	public void testCategoryParse() throws Exception
	{
		AttributeCategory c1 = AttributeCategories.parse("test");
		AttributeCategory c2 = AttributeCategories.parse("test");
		assertEquals(c1, c2);
		assertEquals("test", c1.getId());
		assertNull(c1.getDelegatedCategory());
		assertFalse(c1.isDelegated());
		assertEquals("test", c1.toString());
		
		AttributeCategory c3 = AttributeCategories.parse(AttributeCategories.ENVIRONMENT.getId());
		assertEquals(AttributeCategories.ENVIRONMENT, c3);
		assertSame(AttributeCategories.ENVIRONMENT, c3);
	}
	
	@Test
	public void testDelegatedCategory() throws Exception
	{
		AttributeCategory c1 = AttributeCategories.parse("urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:urn:oasis:names:tc:xacml:3.0:attribute-category:action");
		assertEquals("urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:urn:oasis:names:tc:xacml:3.0:attribute-category:action", c1.getId());
		assertNotNull(c1.getDelegatedCategory());
		assertTrue(c1.isDelegated());
		assertEquals(AttributeCategories.ACTION, c1.getDelegatedCategory());
		assertSame(AttributeCategories.ACTION, c1.getDelegatedCategory());
		
		AttributeCategory c2 = AttributeCategories.parse("urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:testCategory");
		
		assertEquals("urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:testCategory", c2.getId());
		assertNotNull(c2.getDelegatedCategory());
		assertTrue(c2.isDelegated());
		assertEquals(AttributeCategories.parse("testCategory"), c2.getDelegatedCategory());
	}
}
