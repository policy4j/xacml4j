package com.artagon.xacml.v3;

import com.artagon.xacml.util.Preconditions;

public class AttributesReference extends XacmlObject
{
	private String referenceId;
	
	public AttributesReference(String referenceId){
		Preconditions.checkNotNull(referenceId);
		this.referenceId = referenceId;
	}
	
	public String getReferenceId(){
		return referenceId;
	}
}
