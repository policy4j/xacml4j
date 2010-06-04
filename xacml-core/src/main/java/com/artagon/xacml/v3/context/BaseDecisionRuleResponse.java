package com.artagon.xacml.v3.context;

import java.util.Collection;
import java.util.LinkedList;

import com.artagon.xacml.v3.AttributeAssignment;
import com.artagon.xacml.v3.XacmlObject;


abstract class BaseDecisionRuleResponse extends XacmlObject
{
	private String id;
	private Collection<AttributeAssignment> attributes;
	
	protected BaseDecisionRuleResponse(
			String id, Collection<AttributeAssignment> attributes) 
	{
		checkNotNull(id, "Decision rule response id can not be null");
		checkNotNull(attributes, 
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
