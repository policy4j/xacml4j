package com.artagon.xacml.v3;

import java.util.Collection;
import java.util.LinkedList;

import com.google.common.base.Preconditions;

abstract class BaseDecisionRuleResponse extends XacmlObject
{
	private String id;
	private Collection<AttributeAssignment> attributes;
	
	protected BaseDecisionRuleResponse(
			String id, Collection<AttributeAssignment> attributes) 
	{
		Preconditions.checkNotNull(id, "Decision rule response id can not be null");
		Preconditions.checkNotNull(attributes, 
				"Decision rule attribute assignments can not be null");
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
