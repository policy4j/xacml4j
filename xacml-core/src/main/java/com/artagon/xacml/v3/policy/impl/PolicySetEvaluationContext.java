package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.AttributeResolver;
import com.artagon.xacml.v3.policy.DecisionRuleReferenceResolver;
import com.artagon.xacml.v3.policy.PolicySet;

final class PolicySetEvaluationContext extends BaseEvaluationContext
{
	private PolicySet policySet;
	
	public PolicySetEvaluationContext(PolicySet policySet, 
			AttributeResolver attributeService, 
			DecisionRuleReferenceResolver policyResolver){
		super(attributeService, policyResolver);
		Preconditions.checkNotNull(policySet);
		this.policySet = policySet;
	}

	@Override
	public PolicySet getCurrentPolicySet() {
		return policySet;
	}
}
