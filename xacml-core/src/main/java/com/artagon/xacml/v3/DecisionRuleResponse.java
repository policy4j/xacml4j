package com.artagon.xacml.v3;

import java.util.Collection;


public interface DecisionRuleResponse 
{
	String getId();
	Collection<AttributeAssignment> getAttributes();
}
