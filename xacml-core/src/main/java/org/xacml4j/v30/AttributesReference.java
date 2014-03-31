package org.xacml4j.v30;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class AttributesReference
{
	private String referenceId;

	private AttributesReference(Builder b){
		Preconditions.checkNotNull(b.id);
		this.referenceId = b.id;
	}

	public static Builder builder(){
		return new Builder();
	}

	/**
	 * Gets an attribute reference
	 * identifier
	 *
	 * @return reference identifier
	 */
	public String getReferenceId(){
		return referenceId;
	}

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("referenceId", referenceId)
		.toString();
	}

	@Override
	public int hashCode(){
		return referenceId.hashCode();
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof AttributesReference)){
			return false;
		}
		AttributesReference r = (AttributesReference)o;
		return referenceId.equals(r.referenceId);
	}

	public static class Builder
	{
		private String id;

		public Builder id(String id){
			Preconditions.checkArgument(!Strings.isNullOrEmpty(id),
					"Attribute id can't be null or empty");
			this.id = id;
			return this;
		}

		public Builder from(Category a){
			return id(a.getId());
		}

		public AttributesReference build(){
			Preconditions.checkState(!Strings.isNullOrEmpty(id),
					"Attribute id can't be null or empty");
			return new AttributesReference(this);
		}
	}

}
