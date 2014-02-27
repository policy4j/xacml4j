package org.xacml4j.v30;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class Attributes 
{
	private final String id;
	private final AttributeCategory categoryId;
	private final AttributesReference ref;
	private Entity entity;

	private Attributes(Builder b) {
		Preconditions.checkNotNull(b.category);
		Preconditions.checkNotNull(b.entity);
		this.id = b.id;
		this.categoryId = b.category;
		this.entity = b.entity;
		this.ref = (b.id == null)
				? null
				: AttributesReference.builder().id(b.id).build();
	}

	/**
	 * Constructs {@link Attributes.Builder} for given
	 * attribute category
	 * 
	 * @param category attribute category
	 * @return {@link Attributes.Builder} instance
	 */
	public static Builder builder(AttributeCategory category){
		return new Builder(category);
	}

	public static Builder builder(){
		return new Builder();
	}

	/**
	 * An unique identifier of the attribute in
	 * the request context
	 *
	 * @return unique identifier of the
	 * attribute in the request context
	 */
	public String getId(){
		return id;
	}

	public AttributesReference getReference(){
		return ref;
	}

	public Entity getEntity(){
		return entity;
	}

	/**
	 * Gets an attribute category
	 *
	 * @return attribute category
	 */
	public AttributeCategory getCategory(){
		return categoryId;
	}

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("category", categoryId)
		.add("id", id)
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
		if(o == null){
			return false;
		}
		if(!(o instanceof Attributes)){
			return false;
		}
		Attributes a = (Attributes)o;
		return Objects.equal(categoryId, a.categoryId) &&
		Objects.equal(id, a.id) && entity.equals(a.entity);
	}

	public static class Builder
	{
		private String id;
		private AttributeCategory category;
		private Entity entity;

		private Builder(AttributeCategory category){
			Preconditions.checkNotNull(category);
			this.category = category;
		}

		private Builder(){
		}

		public Builder entity(Entity entity){
			this.entity = entity;
			return this;
		}

		public Builder copyOf(Attributes a){
			return copyOf(a, Predicates.<Attribute>alwaysTrue());
		}
		
		public Builder copyOf(Attributes a,
				Predicate<Attribute> f){
			Preconditions.checkNotNull(a);
			id(a.getId());
			category(a.getCategory());
			entity(Entity.builder().copyOf(a.entity, f).build());
			return this;
		}

		public Builder id(String id){
			this.id = id;
			return this;
		}

		public Builder category(AttributeCategory category){
			Preconditions.checkNotNull(category);
			this.category = category;
			return this;
		}
		
		public Attributes build(){
			return new Attributes(this);
		}
	}
}
