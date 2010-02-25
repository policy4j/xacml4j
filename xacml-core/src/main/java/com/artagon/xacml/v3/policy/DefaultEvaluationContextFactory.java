package com.artagon.xacml.v3.policy;

import com.artagon.xacml.util.Preconditions;


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
