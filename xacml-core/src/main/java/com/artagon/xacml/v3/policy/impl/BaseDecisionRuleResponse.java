package com.artagon.xacml.v3.policy.impl;

import java.util.Collection;
import java.util.LinkedList;

import com.artagon.xacml.v3.AttributeAssignment;
import com.artagon.xacml.v3.DecisionRuleResponse;
import com.artagon.xacml.v3.PolicySyntaxException;
import com.artagon.xacml.v3.XacmlObject;

abstract class BaseDecisionRuleResponse extends XacmlObject implements DecisionRuleResponse
{
	private String id;
	private Collection<AttributeAssignment> attributes;
	
	protected BaseDecisionRuleResponse(
			String id, Collection<AttributeAssignment> attributes) 
		throws PolicySyntaxException
	{
		checkNotNull(id, "Decision rule respone id can not be null");
		checkNotNull(attributes, 
				"Decision rule attribute assignments can not be null");
		this.id = id;
		this.attributes = new LinkedList<AttributeAssignment>(attributes);
	}
	
	@Override
	public final String getId(){
		return id;
	}
	
	@Override
	public final Collection<AttributeAssignment> getAttributes(){
		return attributes;
	}
}
