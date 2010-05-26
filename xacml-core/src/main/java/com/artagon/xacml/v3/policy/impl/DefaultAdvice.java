package com.artagon.xacml.v3.policy.impl;

import java.util.Collection;

import com.artagon.xacml.v3.Advice;
import com.artagon.xacml.v3.AttributeAssignment;
import com.artagon.xacml.v3.PolicySyntaxException;

final class DefaultAdvice extends BaseDecisionRuleResponse implements Advice
{
	public DefaultAdvice(String adviceId, Collection<AttributeAssignment> attributes) 
		throws PolicySyntaxException 
	{
		super(adviceId, attributes);
	}
}
