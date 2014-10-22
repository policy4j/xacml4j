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

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;

public enum Categories implements CategoryId
{
	ACTION("urn:oasis:names:tc:xacml:3.0:attribute-category:action", "Action"),
	ENVIRONMENT("urn:oasis:names:tc:xacml:3.0:attribute-category:environment", "Environment"),
	RESOURCE("urn:oasis:names:tc:xacml:3.0:attribute-category:resource", "Resource"),
	OBLIGATION("urn:oasis:names:tc:xacml:3.0:attribute-category:obligation", "Obligation"),
	STATUS_DETAIL("urn:oasis:names:tc:xacml:3.0:attribute-category:status-detail", "StatusDetail"),
	SUBJECT_ACCESS("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject", "AccessSubject"),
	SUBJECT_CODEBASE("urn:oasis:names:tc:xacml:1.0:subject-category:codebase", "Codebase"),
	SUBJECT_INTERMEDIARY("urn:oasis:names:tc:xacml:1.0:subject-category:intermediary-subject", "IntermediarySubject"),
	SUBJECT_RECIPIENT("urn:oasis:names:tc:xacml:1.0:subject-category:recipient-subject", "Recipient"),
	SUBJECT_REQUESTING_MACHINE("urn:oasis:names:tc:xacml:1.0:subject-category:requesting-machine", "RequestingMachine"),
	SUBJECT_ROLE_ENABLEMENT_AUTHORITY("urn:oasis:names:tc:xacml:2.0:subject-category:role-enablement-authority", "RoleEnablementAuthority"),
	DELEGATE("urn:oasis:names:tc:xacml:3.0:attribute-category:delegate", "Delegate"),
	DELEGATE_INFO("urn:oasis:names:tc:xacml:3.0:attribute-category:delegate-info", "DelegateInfo");


	private final String categoryURI;
	private final CategoryId delegated;
	private final String alias;

	private static final String DELEGATED_CATEGORY_PREFIX= "urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:";

	private static final ImmutableMap<String, CategoryId> BY_ID;
	private final static ImmutableBiMap<String, CategoryId> SHORT_NAMES;

	static
	{
		ImmutableBiMap.Builder<String, CategoryId> shortNamesBuilder = ImmutableBiMap.builder();
		ImmutableMap.Builder<String, CategoryId> byIdBuilder = ImmutableMap.builder();
		for(CategoryId category : EnumSet.allOf(Categories.class)){
			byIdBuilder.put(category.getId(), category);
			shortNamesBuilder.put(category.getShortName(), category);
			CategoryId delegate = category.toDelegatedCategory();
			if(delegate != null){
				byIdBuilder.put(delegate.getId(), delegate);
			}
		}
		SHORT_NAMES = shortNamesBuilder.build();
		BY_ID = byIdBuilder.build();
	}

	private Categories(
			String categoryURI,  String alias){
		this.categoryURI = categoryURI;
		this.alias = alias;
		if(!isDelegate(categoryURI)){
			this.delegated = new CustomCategory(toDelegateURI(categoryURI));
		} else {
			this.delegated = null;
		}
	}

	@Override
	public String getId(){
		return categoryURI;
	}

	@Override
	public String getShortName() {
		return alias;
	}

	@Override
	public boolean isDefault() {
		return true;
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
		Preconditions.checkArgument(categoryUri != null, "Category URI can not be null");
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
	 * Gets all short names available for categories
	 *
	 * @return an iterator over short category names
	 */
	public static Iterable<String> getCategoryShortNames(){
		return SHORT_NAMES.keySet();
	}

	/**
	 * Filters given iterable of categories by excluding custom categories
	 *
	 * @param categories an iterable over categories
	 * @return filtered iterable containing default categories
	 */
	public static Collection<Category> getDefaultCategories(final Iterable<Category> categories){
		return FluentIterable.from(categories).filter(new Predicate<Category>() {
			@Override
			public boolean apply(Category category) {
				return category.getCategoryId().isDefault();
			}
		}).toImmutableList();
	}

	/**
	 * Filters given iterable of categories by excluding default categories
	 *
	 * @param categories an iterable over categories
	 * @return filtered iterable containing custom categories
	 */
	public static Collection<Category> getCustomCategories(final Iterable<Category> categories){
		return FluentIterable.from(categories).filter(new Predicate<Category>() {
			@Override
			public boolean apply(Category category) {
				return !category.getCategoryId().isDefault();
			}
		}).toImmutableList();
	}


	/**
	 * A custom category implementation
	 */
	private static class CustomCategory implements CategoryId {
		private final String categoryURI;
		private final CategoryId delegated;

		private CustomCategory(
				String categoryURI)
		{
			this.categoryURI = categoryURI;
			this.delegated = Categories.isDelegate(categoryURI) ? null : new CustomCategory(toDelegateURI(categoryURI));
		}

		@Override
		public String getId(){
			return categoryURI;
		}

		@Override
		public String getShortName() {
			return categoryURI;
		}

		@Override
		public boolean isDefault() {
			return false;
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
			return c.getId().equals(categoryURI);
		}

		@Override
		public String toString() {
			return categoryURI;
		}
	}
}
