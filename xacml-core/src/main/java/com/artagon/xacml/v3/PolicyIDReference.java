package com.artagon.xacml.v3;

public interface PolicyIDReference extends CompositeDecisionRuleIDReference
{
	boolean isReferenceTo(Policy policy);
}
