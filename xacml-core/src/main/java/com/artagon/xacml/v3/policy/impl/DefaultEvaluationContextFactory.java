package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.AttributeResolver;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationContextFactory;
import com.artagon.xacml.v3.policy.Policy;
import com.artagon.xacml.v3.policy.PolicyResolver;
import com.artagon.xacml.v3.policy.PolicySet;


public class DefaultEvaluationContextFactory implements EvaluationContextFactory
{
	private AttributeResolver attributeResolver;
	private PolicyResolver policyResolver;
	
	public DefaultEvaluationContextFactory(AttributeResolver service, 
			PolicyResolver policyResolver){
		Preconditions.checkNotNull(service);
		Preconditions.checkNotNull(policyResolver);
		this.attributeResolver = service;	
		this.policyResolver = policyResolver;
	}

	@Override
	public EvaluationContext createContext(Policy policy) {
		return new PolicyEvaluationContext(policy, attributeResolver, policyResolver);
	}
	
	@Override
	public EvaluationContext createContext(PolicySet policySet) {
		return new PolicySetEvaluationContext(policySet, attributeResolver, policyResolver);
	}
}
