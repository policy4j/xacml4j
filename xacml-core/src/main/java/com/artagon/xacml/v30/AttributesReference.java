package com.artagon.xacml.v30;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class AttributesReference
{
	private String referenceId;
	
	/**
	 * Constructs an attribute reference
	 * 
	 * @param referenceId a reference identifier
	 */
	public AttributesReference(String referenceId){
		Preconditions.checkNotNull(referenceId, 
				"Attribute reference id can not be null");
		this.referenceId = referenceId;
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
	
}
