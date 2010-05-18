package com.artagon.xacml.v3.policy.impl;

import java.util.Collection;

import com.artagon.xacml.v3.Advice;
import com.artagon.xacml.v3.policy.AttributeAssignment;

final class DefaultAdvice extends BaseDecisionRuleResponse implements Advice
{
	public DefaultAdvice(String adviceId, Collection<AttributeAssignment> attributes) {
		super(adviceId, attributes);
	}
}
