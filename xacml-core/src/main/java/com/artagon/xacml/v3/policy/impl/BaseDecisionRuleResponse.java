package com.artagon.xacml.v3.policy.impl;

import java.util.Collection;
import java.util.LinkedList;

import com.artagon.xacml.v3.DecisionRuleResponse;
import com.artagon.xacml.v3.XacmlObject;
import com.artagon.xacml.v3.policy.AttributeAssignment;
import com.google.common.base.Preconditions;

abstract class BaseDecisionRuleResponse extends XacmlObject implements DecisionRuleResponse
{
	private String id;
	private Collection<AttributeAssignment> attributes;
	
	protected BaseDecisionRuleResponse(String id, Collection<AttributeAssignment> attributes){
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(attributes);
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
