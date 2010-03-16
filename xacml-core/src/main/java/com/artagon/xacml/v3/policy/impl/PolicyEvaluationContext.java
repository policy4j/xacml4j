package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.AttributeReferenceResolver;
import com.artagon.xacml.v3.policy.Policy;
import com.artagon.xacml.v3.policy.DecisionRuleReferenceResolver;
import com.artagon.xacml.v3.policy.spi.XPathProvider;

final class PolicyEvaluationContext extends BaseEvaluationContext
{	
	private Policy policy;

	/**
	 * Creates policy evaluation context without parent evaluation context
	 * @param policy
	 * @param service
	 */
	PolicyEvaluationContext(Policy policy, 
			AttributeReferenceResolver service, 
			DecisionRuleReferenceResolver policyResolver, 
			XPathProvider xpathProvider)
	{
		super(service, policyResolver, xpathProvider);
		Preconditions.checkNotNull(policy);
		this.policy = policy;
	}
	
	@Override
	public Policy getCurrentPolicy() {
		return policy;
	}
}
