package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationContextFactory;
import com.artagon.xacml.v3.policy.AttributeResolver;
import com.artagon.xacml.v3.policy.Policy;
import com.artagon.xacml.v3.policy.DecisionRuleReferenceResolver;
import com.artagon.xacml.v3.policy.PolicySet;
import com.artagon.xacml.v3.policy.spi.XPathProvider;


public class DefaultEvaluationContextFactory implements EvaluationContextFactory
{
	private AttributeResolver attributeResolver;
	private DecisionRuleReferenceResolver policyResolver;
	
	public DefaultEvaluationContextFactory(
			AttributeResolver service, 
			DecisionRuleReferenceResolver policyResolver, 
			XPathProvider xpathProvider){
		Preconditions.checkNotNull(service);
		Preconditions.checkNotNull(policyResolver);
		Preconditions.checkNotNull(xpathProvider);
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
