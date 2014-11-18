package org.xacml4j.v30;

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

import org.junit.Test;


public class CategoryIdTest
{
	@Test
	public void testCategoryParse() throws Exception
	{
		CategoryId c1 = Categories.parse("test");
		CategoryId c2 = Categories.parse("test");
		assertEquals(c1, c2);
		assertEquals("test", c1.getName());
		assertFalse(c1.isDelegated());
		assertEquals("test", c1.toString());
		CategoryId d1 = c1.toDelegatedCategory();
		CategoryId d2 = c1.toDelegatedCategory();
		assertEquals(d1, d2);
		assertEquals("urn:oasis:names:tc:xacml:3.0:category-category:delegated:test", d1.getName());
		assertEquals("urn:oasis:names:tc:xacml:3.0:category-category:delegated:test", d2.getName());
		assertTrue(d1.isDelegated());
		assertTrue(d2.isDelegated());
		assertNull(d1.toDelegatedCategory());
		assertNull(d2.toDelegatedCategory());
		CategoryId c3 = Categories.parse(Categories.ENVIRONMENT.toDelegatedCategory().getName());
		assertEquals(Categories.ENVIRONMENT.toDelegatedCategory(), c3);
		assertSame(Categories.ENVIRONMENT.toDelegatedCategory(), c3);
	}

	@Test
	public void testDelegatedCategory() throws Exception
	{
		CategoryId c = Categories.parse("urn:oasis:names:tc:xacml:3.0:category-category:delegated:testCategory");
		assertEquals("urn:oasis:names:tc:xacml:3.0:category-category:delegated:testCategory", c.getName());
		assertTrue(c.isDelegated());
		assertNull(c.toDelegatedCategory());

	}
}
