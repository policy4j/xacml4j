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

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;


/**
 * Represents XACML attributes category
 * represented via {@link CategoryId}
 *
 * @author Giedrius Trumpickas
 */
public final class Category implements Serializable
{
	private final static Logger LOG = LoggerFactory.getLogger(Category.class);

	private final String refId;
	private final CategoryReference cachedRef;
	private final CategoryId categoryId;
	private final Entity entity;

	/**
	 * Creates {@link Category} from given {@link Category.Builder}
	 *
	 * @param b a category builder
	 */
	private Category(Builder b) {
		this.refId = b.id;
		this.categoryId = java.util.Objects.requireNonNull(b.category,
				"Category identifier must be specified");
		this.entity = java.util.Objects.requireNonNull(b.entity,
				"Category entity must be specified");
		this.cachedRef = (b.id == null)
				? null
				: CategoryReference.builder().id(b.id).build();
	}

	/**
	 * Tries to resolve given {@link org.xacml4j.v30.AttributeDesignatorKey} against this category
	 *
	 * @param key an attribute designator
	 * @return {@link java.util.Optional} resolved
	 */
	public java.util.Optional<BagOfValues> resolve(AttributeDesignatorKey key){
		if(!key.getCategory().equals(getCategoryId())){
			return java.util.Optional.empty();
		}
		return entity.resolve(key);
	}

	/**
	 * Tries to resolve given {@link org.xacml4j.v30.AttributeSelectorKey} against this category
	 *
	 * @param key an attribute designator
	 * @return {@link java.util.Optional} resolved
	 */
	public java.util.Optional<BagOfValues> resolve(AttributeSelectorKey key){
		if(!key.getCategory().equals(getCategoryId())){
			return java.util.Optional.empty();
		}
		return entity.resolve(key);
	}


	/**
	 * Constructs {@link Category.Builder} for given
	 * attribute category
	 *
	 * @param category attribute category
	 * @return {@link Category.Builder} defaultProvider
	 */
	public static Builder builder(CategoryId category){
		return new Builder(category);
	}

	public static Builder builder(){
		return new Builder();
	}

	public static Builder subjectAccess(){
		return new Builder(CategoryId.SUBJECT_ACCESS);
	}
	public static Builder subjectRecipient(){
		return new Builder(CategoryId.SUBJECT_RECIPIENT);
	}

	public static Builder subjectCodebase(){
		return new Builder(CategoryId.SUBJECT_CODEBASE);
	}
	public static Builder subjectIntermediary(){
		return new Builder(CategoryId.SUBJECT_INTERMEDIARY);
	}
	public static Builder subjectRequestingMachine(){
		return new Builder(CategoryId.SUBJECT_REQUESTING_MACHINE);
	}

	public static Builder subjectRoleEnablementAuthority(){
		return new Builder(CategoryId.SUBJECT_ROLE_ENABLEMENT_AUTHORITY);
	}

	public static Builder resource(){
		return new Builder(CategoryId.RESOURCE);
	}
	public static Builder action(){
		return new Builder(CategoryId.ACTION);
	}
	public static Builder enviroment(){
		return new Builder(CategoryId.ENVIRONMENT);
	}


	/**
	 * An unique identifier of the attribute category
	 *
	 * @return unique identifier of the attribute
	 * category in the request context
	 */
	public Optional<String> getReferenceId(){
		return Optional.ofNullable(refId);
	}

	public Optional<CategoryReference> getReference(){
		return Optional.ofNullable(cachedRef);
	}

	/**
	 * Gets category entity
	 *
	 * @return {@link Entity}
	 */
	public Entity getEntity(){
		return entity;
	}

	/**
	 * Gets an attribute category
	 *
	 * @return attribute category
	 */
	public CategoryId getCategoryId(){
		return categoryId;
	}

	@Override
	public String toString(){
		return MoreObjects
				.toStringHelper(this)
				.add("category", categoryId.getAbbreviatedId())
				.add("id", refId)
				.add("entity", entity)
				.add("reference", cachedRef)
				.toString();
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(categoryId, refId, entity);
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof Category)){
			return false;
		}
		Category a = (Category)o;
		return java.util.Objects.equals(categoryId, a.categoryId) &&
				java.util.Objects.equals(refId, a.refId) &&
				entity.equals(a.entity) &&
				java.util.Objects.equals(cachedRef, a.cachedRef);
	}

	public final static class Builder
	{
		private String id;
		private CategoryId category;
		private Entity entity;

		private Builder(CategoryId category){
			Preconditions.checkNotNull(category);
			this.category = category;
		}

		private Builder(){
		}

		public Builder entity(Entity entity){
			this.entity = entity;
			return this;
		}

		public Builder copyOf(Category a){
			return copyOf(a, (attribute)->true);
		}

		public Builder copyOf(Category a,
				Predicate<Attribute> f){
			Preconditions.checkNotNull(a);
			id(a.getReferenceId().orElse(null));
			category(a.getCategoryId());
			entity(Entity.builder().copyOf(a.entity, f).build());
			return this;
		}

		public Builder id(String id){
			this.id = id;
			return this;
		}

		public Builder category(CategoryId category){
			this.category = java.util.Objects.requireNonNull(category, "category");
			return this;
		}

		public Category build(){
			return new Category(this);
		}
	}
}
