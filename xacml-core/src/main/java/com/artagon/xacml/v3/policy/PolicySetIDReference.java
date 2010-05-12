package com.artagon.xacml.v3.policy;

public interface PolicySetIDReference extends CompositeDecisionRuleIDReference
{
	boolean isReferenceTo(PolicySet policySet);
}
