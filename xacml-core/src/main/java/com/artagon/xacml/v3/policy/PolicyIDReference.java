package com.artagon.xacml.v3.policy;

public interface PolicyIDReference extends CompositeDecisionRuleIDReference
{
	boolean isReferenceTo(Policy policy);
}
