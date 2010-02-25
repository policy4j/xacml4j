package com.artagon.xacml.v3.policy;

import com.artagon.xacml.util.Preconditions;

final class PolicyEvaluationContext extends BaseEvaluationContext
{	
	private Policy policy;

	/**
	 * Creates policy evaluation context without parent evaluation context
	 * @param policy
	 * @param service
	 */
	PolicyEvaluationContext(Policy policy, AttributeResolver service, 
			PolicyResolver policyResolver)
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
