package com.artagon.xacml.v30;

import com.google.common.base.Preconditions;

public class AttributesReference extends XacmlObject
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
}
