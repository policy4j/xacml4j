package org.xacml4j.v30;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public final class AttributeDesignatorKey
	extends AttributeReferenceKey
{
	private String attributeId;
	private String issuer;
	private int hashCode;

	public AttributeDesignatorKey(Builder b){
		super(b);
		Preconditions.checkArgument(!Strings.isNullOrEmpty(b.attributeId));
		this.attributeId = b.attributeId;
		this.issuer = b.issuer;
		this.hashCode = Objects.hashCode(
				category, attributeId, dataType, issuer);
	}

	public static Builder builder(){
		return new Builder();
	}

	public AttributeDesignatorKey withIssuer(String issuer){
		return builder()
				.from(this)
				.issuer(issuer)
				.build();
	}

	public String getAttributeId(){
		return attributeId;
	}


	public String getIssuer(){
		return issuer;
	}

	@Override
	public BagOfAttributeExp resolve(EvaluationContext context)
			throws EvaluationException
	{
		return context.resolve(this);
	}

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("Category", getCategory())
		.add("AttributeId", attributeId)
		.add("DataType", getDataType())
		.add("Issuer", issuer).toString();
	}

	@Override
	public int hashCode(){
		return hashCode;
	}

	@Override
	public boolean equals(Object o)
	{
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof AttributeDesignatorKey)){
			return false;
		}
		AttributeDesignatorKey s = (AttributeDesignatorKey)o;
		return category.equals(s.category) &&
		dataType.equals(s.dataType) && attributeId.equals(s.attributeId) &&
		Objects.equal(issuer, s.issuer);
	}

	public static class Builder extends AttributeReferenceBuilder<Builder>
	{
		private String issuer;
		private String attributeId;

		public Builder attributeId(String id){
			Preconditions.checkArgument(!Strings.isNullOrEmpty(id));
			this.attributeId = id;
			return this;
		}

		public Builder from(AttributeDesignatorKey ref){
			category(ref.getCategory());
			dataType(ref.getDataType());
			attributeId(ref.getAttributeId());
			issuer(ref.getIssuer());
			return this;
		}

		public Builder issuer(String issuer){
			this.issuer = Strings.emptyToNull(issuer);
			return this;
		}

		@Override
		protected Builder getThis() {
			return this;
		}

		public AttributeDesignatorKey build(){
			return new AttributeDesignatorKey(this);
		}

	}
}
