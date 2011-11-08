package com.artagon.xacml.v30;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.artagon.xacml.v30.core.AttributeCategories;
import com.artagon.xacml.v30.core.AttributeCategory;

public class AttributeCategoriesTest 
{
	@Test
	public void testCategoryParse() throws Exception
	{
		AttributeCategory c1 = AttributeCategories.parse("test");
		AttributeCategory c2 = AttributeCategories.parse("test");
		assertEquals(c1, c2);
		assertEquals("test", c1.getId());
		assertFalse(c1.isDelegated());
		assertEquals("test", c1.toString());
		AttributeCategory d1 = c1.toDelegatedCategory();
		AttributeCategory d2 = c1.toDelegatedCategory();
		assertEquals(d1, d2);
		assertEquals("urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:test", d1.getId());
		assertEquals("urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:test", d2.getId());
		assertTrue(d1.isDelegated());
		assertTrue(d2.isDelegated());
		assertNull(d1.toDelegatedCategory());
		assertNull(d2.toDelegatedCategory());
		AttributeCategory c3 = AttributeCategories.parse(AttributeCategories.ENVIRONMENT.toDelegatedCategory().getId());
		assertEquals(AttributeCategories.ENVIRONMENT.toDelegatedCategory(), c3);
		assertSame(AttributeCategories.ENVIRONMENT.toDelegatedCategory(), c3);
	}
	
	@Test
	public void testDelegatedCategory() throws Exception
	{
		AttributeCategory c = AttributeCategories.parse("urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:testCategory");
		assertEquals("urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:testCategory", c.getId());
		assertTrue(c.isDelegated());
		assertNull(c.toDelegatedCategory());
	
	}
}
