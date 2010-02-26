package com.artagon.xacml.v3.policy.impl;

import java.util.Collection;

import com.artagon.xacml.v3.Obligation;
import com.artagon.xacml.v3.policy.AttributeAssignment;

class DefaultObligation extends BaseDecisionRuleResponse implements Obligation
{
	public DefaultObligation(String id, Collection<AttributeAssignment> attributes) {
		super(id, attributes);
	}	
}
