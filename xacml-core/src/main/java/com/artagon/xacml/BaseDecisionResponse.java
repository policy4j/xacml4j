package com.artagon.xacml;

import java.util.Collection;
import java.util.LinkedList;

import com.artagon.xacml.policy.AttributeAssignment;
import com.artagon.xacml.util.Preconditions;

abstract class BaseDecisionResponse
{
	private String id;
	private Collection<AttributeAssignment> attributes;
	
	protected BaseDecisionResponse(String id, Collection<AttributeAssignment> attributes){
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(attributes);
		this.id = id;
		this.attributes = new LinkedList<AttributeAssignment>(attributes);
	}
	
	public final String getId(){
		return id;
	}
	
	public final Collection<AttributeAssignment> getAttributes(){
		return attributes;
	}
}
