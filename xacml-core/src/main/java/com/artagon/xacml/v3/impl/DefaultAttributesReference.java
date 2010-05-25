package com.artagon.xacml.v3.impl;

import com.artagon.xacml.v3.AttributesReference;
import com.artagon.xacml.v3.XacmlObject;
import com.google.common.base.Preconditions;

final class DefaultAttributesReference extends XacmlObject implements AttributesReference
{
	private String referenceId;
	
	/**
	 * Constructs an attribute reference
	 * 
	 * @param referenceId a reference identifier
	 */
	public DefaultAttributesReference(String referenceId){
		Preconditions.checkNotNull(referenceId);
		this.referenceId = referenceId;
	}
	
	@Override
	public String getReferenceId(){
		return referenceId;
	}
}
