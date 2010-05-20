package com.artagon.xacml.v3.policy.impl;

import java.util.Collection;

import com.artagon.xacml.v3.AttributeAssignment;
import com.artagon.xacml.v3.Obligation;

final class DefaultObligation extends BaseDecisionRuleResponse implements Obligation
{
	public DefaultObligation(String id, Collection<AttributeAssignment> attributes) {
		super(id, attributes);
	}	
}
