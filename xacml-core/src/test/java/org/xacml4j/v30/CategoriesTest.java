package org.xacml4j.v30;

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
