package com.artagon.xacml.v3;

import com.artagon.xacml.util.Preconditions;

public class AttributesReference extends XacmlObject
{
	private String referenceId;
	
	/**
	 * Constructs an attribute reference
	 * 
	 * @param referenceId a reference identifier
	 */
	public AttributesReference(String referenceId){
		Preconditions.checkNotNull(referenceId);
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
}
