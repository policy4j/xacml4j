package org.xacml4j.v30.pdp;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;
import org.xacml4j.v30.Categories;
import org.xacml4j.v30.Category;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.Entity;

import com.google.common.collect.ImmutableList;


public class CategoriesTest
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

	@Test
	public void testShortCategoryNames(){
		assertEquals(Categories.ACTION, Categories.parse("Action"));
		assertEquals(Categories.SUBJECT_ACCESS, Categories.parse("AccessSubject"));
		assertEquals(Categories.RESOURCE, Categories.parse("Resource"));
		assertEquals(Categories.ENVIRONMENT, Categories.parse("Environment"));
	}

	@Test
	public void testFilterCategoriesMethods()
	{
		ImmutableList<Category> all = ImmutableList
				.<Category>builder()
				.add(Category.builder(Categories.ACTION).entity(Entity.builder().build()).build())
				.add(Category.builder(Categories.ENVIRONMENT).entity(Entity.builder().build()).build())
				.add(Category.builder().category(Categories.parse("Test")).entity(Entity.builder().build()).build())
				.build();
		Collection<Category> defaultCategories = Categories.getDefaultCategories(all);
		assertEquals(2, defaultCategories.size());
		Collection<Category> customCategories = Categories.getCustomCategories(all);
		assertEquals(1, customCategories.size());
	}
}
