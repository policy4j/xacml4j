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

import com.google.common.base.*;


/**
 * Represents a set of XACML category
 * of a given category
 *
 * @author Giedrius Trumpickas
 */
public class Category
{
	private final String id;
	private final CategoryId categoryId;
	private final CategoryReference ref;
	private final Entity entity;

	/**
	 * Creates {@link Category} from given {@link Category.Builder}
	 *
	 * @param b a category builder
	 */
	private Category(Builder b) {
		Preconditions.checkNotNull(b.category,
                "Category identifier must be specified");
		this.id = b.id;
		this.categoryId = b.category;
		this.entity = b.b.build();
		this.ref = (b.id == null)
				? null
				: CategoryReference.builder().id(b.id).build();
	}

	/**
	 * Constructs {@link Category.Builder} for given
	 * category category
	 *
	 * @param category category category
	 * @return {@link Category.Builder} instance
	 */
	public static Builder builder(CategoryId category){
		return new Builder(category);
	}

    public static Builder SubjectAccess(){
        return new Builder(Categories.SUBJECT_ACCESS);
    }
    public static Builder SubjectCodebase(){
        return new Builder(Categories.SUBJECT_CODEBASE);
    }
    public static Builder SubjectIntermediary(){
        return new Builder(Categories.SUBJECT_INTERMEDIARY);
    }
    public static Builder SubjectRecepient(){
        return new Builder(Categories.SUBJECT_RECIPIENT);
    }
    public static Builder SubjectRequestingMachine(){
        return new Builder(Categories.SUBJECT_REQUESTING_MACHINE);
    }
    public static Builder Action(){
        return new Builder(Categories.ACTION);
    }
    public static Builder Resource(){
        return new Builder(Categories.RESOURCE);
    }
    public static Builder Enviroment(){
        return new Builder(Categories.ENVIRONMENT);
    }


	public static Builder builder(){
		return new Builder();
	}

    public static Builder Custom(String categoryId){
        return new Builder().category(Categories.parse(categoryId));
    }

	/**
	 * An unique identifier of the category in
	 * the request context
	 *
	 * @return unique identifier of the
	 * category in the request context
	 */
	public String getReferenceId(){
		return id;
	}


    public boolean isReferencable(){
        return id != null;
    }
    /**
     * Test if this category is a default/well-known category
     *
     * @return {@link true} if this category is a well-known category
     */
    public boolean isDefault(){
        return categoryId.isDefault();
    }

	public CategoryReference getReference(){
		return ref;
	}

	public Entity getEntity(){
		return entity;
	}

	/**
	 * Gets an category category
	 *
	 * @return category category
	 */
	public CategoryId getCategoryId(){
		return categoryId;
	}

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("category", categoryId)
		.add("attributeId", id)
		.add("entity", entity)
		.toString();
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(categoryId, id, entity);
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
		return Objects.equal(categoryId, a.categoryId) &&
		Objects.equal(id, a.id) && entity.equals(a.entity);
	}

	public static class Builder
	{
		private String id;
		private CategoryId category;
		private Entity.Builder b = Entity.builder();
        private String defaultIssuer;

        private Builder(){
        }

		private Builder(CategoryId category){
			Preconditions.checkNotNull(category);
			this.category = category;
		}

        /**
         * A default issuer is used by this builder
         * to set an issuer on the category created
         * via fluent category creation methods
         *
         * @param issuer an default issuer
         * @return this builder references
         */
        public Builder defaultIssuer(String issuer){
            this.defaultIssuer = defaultIssuer;
            return this;
        }

        public Builder noDefaultIssuer(){
            this.defaultIssuer = null;
            return this;
        }

        public Builder entity(Entity entity){
			b.copyOf(entity);
			return this;
		}

		public Builder copyOf(Category a){
			return copyOf(a, Predicates.<Attribute>alwaysTrue());
		}

		public Builder copyOf(Category a,
				Predicate<Attribute> f){
			Preconditions.checkNotNull(a);
			id(a.getReferenceId());
			category(a.getCategoryId());
			b.copyOf(a.entity, f);
			return this;
		}

        public Builder copyOf(Category a,
                              Function<Attribute, Attribute> f){
            Preconditions.checkNotNull(a);
            id(a.getReferenceId());
            category(a.getCategoryId());
            b.copyOf(a.entity, f);
            return this;
        }

		public Builder id(String id){
			this.id = id;
			return this;
		}

		public Builder category(CategoryId category){
			Preconditions.checkNotNull(category);
			this.category = category;
			return this;
		}

		public Category build(){
			return new Category(this);
		}
	}
}
