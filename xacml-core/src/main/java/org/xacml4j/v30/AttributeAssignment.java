package org.xacml4j.v30;

import org.xacml4j.v30.pdp.AttributeAssignmentExpression;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;


public class AttributeAssignment
{
	private final AttributeExp attribute;
	private final CategoryId category;
	private final String attributeId;
	private final String issuer;

	/**
	 * Creates attribute assignment with a
	 * given attribute identifier
	 *
	 * @param b attribute assignment builder
	 */
	private AttributeAssignment(Builder b)
	{
		Preconditions.checkNotNull(b.attributeId, "Attribute id can't be null");
		Preconditions.checkNotNull(b.value, "Attribute value can't be null");
		this.attributeId = b.attributeId;
		this.category = b.category;
		this.issuer = b.issuer;
		this.attribute = b.value;
	}

	public static Builder builder(){
		return new Builder();
	}

	/**
	 * Gets attribute identifier
	 *
	 * @return attribute identifier
	 */
	public String getAttributeId(){
		return attributeId;
	}

	/**
	 * Gets attribute value
	 *
	 * @return attribute value
	 */
	public AttributeExp getAttribute(){
		return attribute;
	}

	/**
	 * Gets attribute category
	 *
	 * @return attribute category
	 */
	public CategoryId getCategory(){
		return category;
	}


	/**
	 * Gets attribute issuer identifier
	 *
	 * @return attribute issuer
	 */
	public String getIssuer(){
		return issuer;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(
				attributeId,
				category,
				attribute,
				issuer);
	}

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("attributeId", attributeId)
		.add("category", category)
		.add("value", attribute)
		.add("issuer", issuer).toString();
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof AttributeAssignment)){
			return false;
		}
		AttributeAssignment a = (AttributeAssignment)o;
		return attributeId.equals(a.attributeId) &&
			attribute.equals(a.attribute) &&
			Objects.equal(category, a.category) &&
			Objects.equal(issuer, a.issuer);
	}

	public static class Builder
	{
		private String attributeId;
		private CategoryId category;
		private String issuer;
		private AttributeExp value;

		public Builder id(String attributeId){
			Preconditions.checkNotNull(attributeId);
			this.attributeId = attributeId;
			return this;
		}

		/**
		 * Copies all state from a given {@link AttributeAssignmentExpression}
		 * except attribute value expression
		 *
		 * @param attrAssigExp attribute assignment expression
		 * @return {@link Builder}
		 */
		public Builder from(AttributeAssignmentExpression attrAssigExp)
		{
			this.attributeId = attrAssigExp.getAttributeId();
			this.category = attrAssigExp.getCategory();
			this.issuer = attrAssigExp.getIssuer();
			return this;
		}

		public Builder category(CategoryId category){
			this.category = category;
			return this;
		}

		public Builder category(String category){
			this.category = Categories.parse(category);
			return this;
		}

		public Builder issuer(String issuer){
			this.issuer =  issuer;
			return this;
		}

		public Builder value(AttributeExp v){
			Preconditions.checkNotNull(v);
			this.value = v;
			return this;
		}

		public AttributeAssignment build(){
			return new AttributeAssignment(this);
		}
	}
}
