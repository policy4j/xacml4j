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

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

public class CategoriesTest {

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
                .add(Category.Action().build())
                .add(Category.Enviroment().build())
                .add(Category.Custom("Test").build())
               .build();
        Collection<Category> defaultCategories = Categories.getDefaultCategories(all);
        assertEquals(2, defaultCategories.size());
        Collection<Category> customCategories = Categories.getCustomCategories(all);
        assertEquals(1, customCategories.size());
    }
}
