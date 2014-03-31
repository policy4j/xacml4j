package org.xacml4j.v30.pdp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.CategoryId;


public class AttributeCategoriesTest
{
	@Test
	public void testCategoryParse() throws Exception
	{
		CategoryId c1 = Categories.parse("test");
		CategoryId c2 = Categories.parse("test");
		assertEquals(c1, c2);
		assertEquals("test", c1.getId());
		assertFalse(c1.isDelegated());
		assertEquals("test", c1.toString());
		CategoryId d1 = c1.toDelegatedCategory();
		CategoryId d2 = c1.toDelegatedCategory();
		assertEquals(d1, d2);
		assertEquals("urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:test", d1.getId());
		assertEquals("urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:test", d2.getId());
		assertTrue(d1.isDelegated());
		assertTrue(d2.isDelegated());
		assertNull(d1.toDelegatedCategory());
		assertNull(d2.toDelegatedCategory());
		CategoryId c3 = Categories.parse(Categories.ENVIRONMENT.toDelegatedCategory().getId());
		assertEquals(Categories.ENVIRONMENT.toDelegatedCategory(), c3);
		assertSame(Categories.ENVIRONMENT.toDelegatedCategory(), c3);
	}

	@Test
	public void testDelegatedCategory() throws Exception
	{
		CategoryId c = Categories.parse("urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:testCategory");
		assertEquals("urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:testCategory", c.getId());
		assertTrue(c.isDelegated());
		assertNull(c.toDelegatedCategory());

	}
}
