package com.artagon.xacml.v3;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

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
		assertEquals("test", c1.toString());
		
		AttributeCategory c3 = AttributeCategories.parse(AttributeCategories.ENVIRONMENT.getId());
		assertEquals(AttributeCategories.ENVIRONMENT, c3);
		assertSame(AttributeCategories.ENVIRONMENT, c3);
	}
}
