package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.AttributeResolver;
import com.artagon.xacml.v3.policy.Policy;
import com.artagon.xacml.v3.policy.DecisionRuleReferenceResolver;

final class PolicyEvaluationContext extends BaseEvaluationContext
{	
	private Policy policy;

	/**
	 * Creates policy evaluation context without parent evaluation context
	 * @param policy
	 * @param service
	 */
	PolicyEvaluationContext(Policy policy, 
			AttributeResolver service, 
			DecisionRuleReferenceResolver policyResolver)
	{
		super(service, policyResolver);
		Preconditions.checkNotNull(policy);
		this.policy = policy;
	}
	
	@Override
	public Policy getCurrentPolicy() {
		return policy;
	}
}
