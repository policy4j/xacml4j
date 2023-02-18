package org.xacml4j.v30;

import org.xacml4j.util.StringUtils;
import org.xacml4j.v30.types.XacmlTypes;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

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


/**
 * XACML attribute category identifier
 *
 * @author Giedrius Trumpickas
 */
public final class CategoryId extends SemanticalIdentifier
{
	public static final CategoryId ACTION  = new CategoryId("urn:oasis:names:tc:xacml:3.0:attribute-category:action", "action");
	public static final CategoryId ENVIRONMENT = new CategoryId("urn:oasis:names:tc:xacml:3.0:attribute-category:environment", "environment");
	public static final CategoryId RESOURCE  = new CategoryId("urn:oasis:names:tc:xacml:3.0:attribute-category:resource", "resource");
	public static final CategoryId OBLIGATION = new CategoryId("urn:oasis:names:tc:xacml:3.0:attribute-category:obligation", "obligation");
	public static final CategoryId STATUS_DETAIL = new CategoryId("urn:oasis:names:tc:xacml:3.0:attribute-category:status-detail", "status-detail");
	public static final CategoryId SUBJECT_ACCESS = new CategoryId("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject", "access-subject");
	public static final CategoryId SUBJECT_CODEBASE = new CategoryId("urn:oasis:names:tc:xacml:1.0:subject-category:codebase", "codebase");
	public static final CategoryId SUBJECT_INTERMEDIARY = new CategoryId("urn:oasis:names:tc:xacml:1.0:subject-category:intermediary-subject", "intermediary-subject");
	public static final CategoryId SUBJECT_RECIPIENT = new CategoryId("urn:oasis:names:tc:xacml:1.0:subject-category:recipient-subject", "recipient-subject");
	public static final CategoryId SUBJECT_REQUESTING_MACHINE = new CategoryId("urn:oasis:names:tc:xacml:1.0:subject-category:requesting-machine", "requesting-machine");
	public static final CategoryId SUBJECT_ROLE_ENABLEMENT_AUTHORITY = new CategoryId("urn:oasis:names:tc:xacml:2.0:subject-category:role-enablement-authority", "role-enablement-authority");
	public static final CategoryId DELEGATE  = new CategoryId("urn:oasis:names:tc:xacml:3.0:attribute-category:delegate", "delegate");
	public static final CategoryId DELEGATE_INFO = new CategoryId("urn:oasis:names:tc:xacml:3.0:attribute-category:delegate-info", "delegate-info");


	private final static Map<String, CategoryId> BY_ID = getById(CategoryId.class);
	private final static Map<String, CategoryId> BY_ABBREVIATED_ID = getByAbbrId(CategoryId.class);

	private static final String DELEGATED_CATEGORY_PREFIX= "urn:oasis:names:tc:xacml:3.0:attribute-category:delegated:";
	private static final String DELEGATED_ABBREVIATED_CATEGORY_PREFIX= "delegated:";


	private Optional<CategoryId> delegated;


	private CategoryId(String categoryId, String abbreviatedId){
		super(normalizeId(categoryId), normalizeId(abbreviatedId));
		this.delegated = toDelegateCategory(categoryId, abbreviatedId);
	}

	private static String normalizeId(String id){
		if(StringUtils.isNullOrEmpty(id)){
			return null;
		}
		return id.trim();
	}

	private CategoryId(String categoryId){
		this(categoryId, categoryId);
	}

	/**
	 * Tests if this category is delegated
	 *
	 * @return {@code true} if this
	 * category is delegated
	 */
	public boolean isDelegated() {
		return !delegated.isPresent();
	}


	/**
	 * Converts this category to
	 * XACML delegated category if category
	 * is already delegate {@link Optional#empty()}
	 * is returned
	 *
	 * @return {@link Optional<CategoryId>}
	 */
	public Optional<CategoryId> toDelegatedCategory() {
		return delegated;
	}

	/**
	 * Parses given value to the {@link CategoryId}
	 * Supported value types: {@link Value},{@link String}
	 * {@link URI} {@link CategoryId} and {@link Optional<CategoryId>}
	 *
	 * @param v a value, supported value types {@link String} {@link URI} {@link Value}
	 * @return {@link CategoryId}
	 */
	public static Optional<CategoryId> parse(Object v)
	{
		if(v instanceof Optional){
			Optional<Object> o =  (Optional)v;
			if(o.orElse(null) instanceof CategoryId){
				return (Optional<CategoryId>)v;
			}
		}
		if(v instanceof CategoryId){
			return Optional.of((CategoryId)v);
		}
		if(v instanceof String){
			return getById((String)v);
		}
		if(v instanceof URI){
			return getById(v.toString());
		}
		if(v instanceof Value){
			Value a = (Value)v;
			if(a.getType().equals(XacmlTypes.ANYURI) ||
					a.getType().equals(XacmlTypes.STRING)){
				return getById(a.value().toString());
			}
		}
		throw SyntaxException
				.invalidCategoryId(v);
	}

	private static Optional<CategoryId> getById(String v){
		return !StringUtils.isNullOrEmpty(v)?
				Optional.ofNullable(BY_ID.get(v))
						.or(()->Optional
								.ofNullable(BY_ABBREVIATED_ID.get(v)))
						.or(()->Optional.of(new CategoryId(v)))
				:Optional.empty();
	}


	public static CategoryId of(Value v)
			throws SyntaxException {
		return parse(v).orElseThrow(
				()-> SyntaxException.invalidCategoryId(v));
	}

	public static CategoryId of(String v)
			throws SyntaxException {
		return parse(v).orElseThrow(
				()-> SyntaxException.invalidCategoryId(v));
	}

	public static CategoryId of(URI v)
			throws SyntaxException {
		return parse(v).orElseThrow(
				()-> SyntaxException.invalidCategoryId(v));
	}

	/**
	 * Tests if a given category URI represents
	 * a delegated category
	 *
	 * @param categoryURI a category URI
	 * @return {@code true} if a given category
	 * URI represents a delegated category
	 */
	public static boolean isDelegate(String categoryURI){
		if(StringUtils.isNullOrEmpty(categoryURI)){
			return false;
		}
		return StringUtils.startsWithIgnoreCase(categoryURI, DELEGATED_CATEGORY_PREFIX) ||
				StringUtils.startsWithIgnoreCase(categoryURI, DELEGATED_ABBREVIATED_CATEGORY_PREFIX);
	}

	public static Optional<CategoryId> toDelegateCategory(
			String categoryId, String abbreviatedId){
		if(isDelegate(categoryId) || isDelegate(abbreviatedId)){
			return Optional.empty();
		}
		return Optional.of(
				new CategoryId(DELEGATED_CATEGORY_PREFIX + categoryId,
						DELEGATED_ABBREVIATED_CATEGORY_PREFIX + abbreviatedId));
	}
}
