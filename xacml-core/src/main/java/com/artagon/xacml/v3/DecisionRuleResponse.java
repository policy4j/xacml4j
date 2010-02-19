package com.artagon.xacml.v3;

import java.util.Collection;

import com.artagon.xacml.v3.policy.AttributeAssignment;

public interface DecisionRuleResponse 
{
	String getId();
	Collection<AttributeAssignment> getAttributes();
}
