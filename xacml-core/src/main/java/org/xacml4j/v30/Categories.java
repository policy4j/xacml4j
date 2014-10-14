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

import java.net.URI;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.*;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableBiMap;

public enum Categories implements CategoryId
{

	ACTION("urn:oasis:names:tc:xacml:3.0:attribute-category:action"),
	ENVIRONMENT("urn:oasis:names:tc:xacml:3.0:attribute-category:environment"),
	RESOURCE("urn:oasis:names:tc:xacml:3.0:attribute-category:resource"),
	OBLIGATION("urn:oasis:names:tc:xacml:3.0:attribute-category:obligation"),
	STATUS_DETAIL("urn:oasis:names:tc:xacml:3.0:attribute-category:status-detail"),
	SUBJECT_ACCESS("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject"),
	SUBJECT_CODEBASE("urn:oasis:names:tc:xacml:1.0:subject-category:codebase"),
	SUBJECT_INTERMEDIARY("urn:oasis:names:tc:xacml:1.0:subject-category:intermediary-subject"),
	SUBJECT_RECIPIENT("urn:oasis:names:tc:xacml:1.0:subject-category:recipient-subject"),
	SUBJECT_REQUESTING_MACHINE("urn:oasis:names:tc:xacml:1.0:subject-category:requesting-machine"),
	SUBJECT_ROLE_ENABLEMENT_AUTHORITY("urn:oasis:names:tc:xacml:2.0:subject-category:role-enablement-authority"),
	DELEGATE("urn:oasis:names:tc:xacml:3.0:attribute-category:delegate"),
	DELEGATE_INFO("urn:oasis:names:tc:xacml:3.0:attribute-category:delegate-info");


	private String categoryURI;

	private CategoryId delegated;

	private static final String DELEGATED_CATEGORY_PREFIX= "urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:";

	private static final Map<String, CategoryId> BY_ID = new HashMap<String, CategoryId>();


    private final static ImmutableBiMap<String, CategoryId> SHORT_NAMES =
            ImmutableBiMap.<String, CategoryId>builder()
                    .put("Action", Categories.ACTION)
                    .put("Environment", Categories.ENVIRONMENT)
                    .put("Resource", Categories.RESOURCE)
                    .put("AccessSubject", Categories.SUBJECT_ACCESS)
                    .put("Codebase", Categories.SUBJECT_CODEBASE)
                    .put("IntermediarySubject", Categories.SUBJECT_INTERMEDIARY)
                    .put("RecipientSubject", Categories.SUBJECT_RECIPIENT)
                    .put("RequestingMachine", Categories.SUBJECT_REQUESTING_MACHINE)
                    .build();

	static
	{
		for(CategoryId category : EnumSet.allOf(Categories.class)){
			BY_ID.put(category.getName(), category);
			CategoryId delegate = category.toDelegatedCategory();
			if(delegate != null){
				BY_ID.put(delegate.getName(), delegate);
			}
		}
	}

	private Categories(
			String categoryURI){
		this.categoryURI = categoryURI;
		if(!isDelegate(categoryURI)){
			this.delegated = new CustomCategory(toDelegateURI(categoryURI));
		}
	}

	@Override
	public String getName(){
		return categoryURI;
	}

    @Override
    public String getShortName() {
        String shortName = SHORT_NAMES.inverse().get(this);
        return shortName == null? getName():shortName;
    }

    @Override
	public boolean isDelegated() {
		return delegated != null;
	}


	@Override
	public CategoryId toDelegatedCategory() {
		return delegated;
	}

	@Override
	public String toString(){
		return categoryURI;
	}

	/**
	 * Parses given value to the {@link Categories}
	 *
	 * @param v a value
	 * @return {@link Categories}
	 * @throws XacmlSyntaxException if given
	 * value can not be converted to the
	 * {@link Categories} value
	 */
	public static CategoryId parse(String v)
		throws XacmlSyntaxException
	{
		if(Strings.isNullOrEmpty(v)){
			throw new XacmlSyntaxException("Given value can't be " +
					"converted to XACML CategoryId");
		}
		CategoryId c = BY_ID.get(v);
		if(c == null){
            c = SHORT_NAMES.get(v);
            if(c == null){
                c = new CustomCategory(v);
            }
		}
		return c;
	}

    public static CategoryId parse(Supplier<String> s)
            throws XacmlSyntaxException
    {
        String v = s.get();
        if(Strings.isNullOrEmpty(v)){
            throw new XacmlSyntaxException("Given value can't be " +
                    "converted to XACML CategoryId");
        }
        CategoryId c = BY_ID.get(v);
        if(c == null){
            c = SHORT_NAMES.get(v);
            if(c == null){
                c = new CustomCategory(v);
            }
        }
        return c;
    }

	public static CategoryId parse(URI categoryUri) throws XacmlSyntaxException{
		Preconditions.checkArgument(categoryUri != null);
		return parse(categoryUri.toString());
	}

	/**
	 * Tests if a given category URI represents
	 * a delegated category
	 *
	 * @param categoryURI a category URI
	 * @return {@code true} if a given category
	 * URI represents a delegated category
	 */
	private static boolean isDelegate(String categoryURI){
		return categoryURI.startsWith(DELEGATED_CATEGORY_PREFIX);
	}

	/**
	 * Creates a XACML 3.0 delegated category
	 * from a given category URI
	 *
	 * @param categoryURI a category URI
	 * @return a delegated category URI
	 */
	private static String toDelegateURI(String categoryURI){
		if(categoryURI.startsWith(DELEGATED_CATEGORY_PREFIX)){
			return categoryURI;
		}
		return DELEGATED_CATEGORY_PREFIX + categoryURI;
	}

    /**
     * Gets category name alias if its available
     *
     * @param id a category
     * @return an optional category short alias
     */
    public static Optional<String> getShortName(CategoryId id){
        return Optional.fromNullable(SHORT_NAMES.inverse().get(id.getName()));
    }

    /**
     * Gets all short names available for categories
     *
     * @return an iterator over short category names
     */
    public static Iterable<String> getCategoryShortNames(){
        return SHORT_NAMES.keySet();
    }


    public static Collection<Category> getDefaultCategories(final Iterable<Category> categories){
        return FluentIterable.from(categories).filter(new Predicate<Category>() {
            @Override
            public boolean apply(Category category) {
                return SHORT_NAMES.inverse().containsKey(category.getCategoryId());
            }
        }).toList();
    }

    public static Collection<Category> getCustomCategories(final Iterable<Category> categories){
        return FluentIterable.from(categories).filter(new Predicate<Category>() {
            @Override
            public boolean apply(Category category) {
                return !SHORT_NAMES.inverse().containsKey(category.getCategoryId());
            }
        }).toList();
    }


    private static class CustomCategory
		implements CategoryId
	{
		private String categoryURI;
		private CategoryId delegated;

		private CustomCategory(
				String categoryURI)
		{
			this.categoryURI = categoryURI;
			if(!Categories.isDelegate(categoryURI)){
				this.delegated = new CustomCategory(toDelegateURI(categoryURI));
			}
		}

		@Override
		public String getName(){
			return categoryURI;
		}

        @Override
        public String getShortName() {
            String shortName = SHORT_NAMES.inverse().get(this);
            return shortName == null? getName():shortName;
        }

        @Override
		public boolean isDelegated() {
			return delegated == null;
		}

		@Override
		public CategoryId toDelegatedCategory() {
			return delegated;
		}

		@Override
		public int hashCode() {
			return categoryURI.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj){
				return true;
			}
			if(!(obj instanceof CategoryId)){
				return false;
			}
			CategoryId c = (CategoryId)obj;
			return c.getName().equals(categoryURI);
		}

		@Override
		public String toString() {
			return categoryURI;
		}
	}
}
