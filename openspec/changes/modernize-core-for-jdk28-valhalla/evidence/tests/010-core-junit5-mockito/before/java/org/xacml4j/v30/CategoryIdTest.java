package org.xacml4j.v30;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2019 Xacml4J.org
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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.common.truth.Truth8;

public class CategoryIdTest
{
    @Test
    public void testParseCategory(){
        assertSame(CategoryId.RESOURCE, CategoryId.parse(CategoryId.RESOURCE.getId()).get());
        assertSame(CategoryId.RESOURCE, CategoryId.parse(CategoryId.RESOURCE.getId().toUpperCase()).get());
        assertSame(CategoryId.RESOURCE, CategoryId.parse(CategoryId.RESOURCE.getAbbreviatedId()).get());
        assertSame(CategoryId.RESOURCE, CategoryId.parse(CategoryId.RESOURCE.getAbbreviatedId().toUpperCase()).get());
    }


    @Test
    public void testCategoryParse() throws Exception
    {
        CategoryId c1 = CategoryId.parse("test").get();
        CategoryId c2 = CategoryId.parse("test").get();
        assertEquals(c1, c2);
        assertEquals("test", c1.getId());
        assertFalse(c1.isDelegated());
        assertEquals("test", c1.getAbbreviatedId());
        CategoryId d1 = c1.toDelegatedCategory().get();
        CategoryId d2 = c2.toDelegatedCategory().get();
        assertTrue(d1.isDelegated());
        assertTrue(d2.isDelegated());
        assertEquals(d1, d2);
        assertEquals("urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:test", d1.getId());
        assertEquals("urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:test", d2.getId());
        Truth8.assertThat(d1.toDelegatedCategory()).isEmpty();
    }

    @Test
    public void testDelegatedCategory() throws Exception
    {
        CategoryId c = CategoryId.parse("urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:testCategory").get();
        assertEquals("urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:testCategory", c.getId());
        assertTrue(c.isDelegated());
        Truth8.assertThat(c.toDelegatedCategory()).isEmpty();
    }


    @Test
    public void testSystemCategories() throws Exception
    {

    }

}
