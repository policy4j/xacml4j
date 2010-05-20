package com.artagon.xacml.v3;

public interface PolicySetIDReference extends CompositeDecisionRuleIDReference
{
	boolean isReferenceTo(PolicySet policySet);
}
