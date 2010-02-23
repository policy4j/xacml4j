package com.artagon.xacml.v3.policy;

import java.util.Collection;

import com.artagon.xacml.v3.Obligation;

class DefaultObligation extends BaseDecisionRuleResponse implements Obligation
{
	public DefaultObligation(String id, Collection<AttributeAssignment> attributes) {
		super(id, attributes);
	}	
}
