package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.AttributeResolver;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationContextFactory;
import com.artagon.xacml.v3.policy.Policy;
import com.artagon.xacml.v3.policy.PolicyResolver;
import com.artagon.xacml.v3.policy.PolicySet;
import com.artagon.xacml.v3.policy.XPathProvider;


public class DefaultEvaluationContextFactory implements EvaluationContextFactory
{
	private AttributeResolver attributeResolver;
	private PolicyResolver policyResolver;
	private XPathProvider xpathProvider;
	
	public DefaultEvaluationContextFactory(
			AttributeResolver service, 
			PolicyResolver policyResolver, 
			XPathProvider xpathProvider){
		Preconditions.checkNotNull(service);
		Preconditions.checkNotNull(policyResolver);
		Preconditions.checkNotNull(xpathProvider);
		this.attributeResolver = service;	
		this.policyResolver = policyResolver;
		this.xpathProvider = xpathProvider;
	}

	@Override
	public EvaluationContext createContext(Policy policy) {
		return new PolicyEvaluationContext(policy, attributeResolver, policyResolver, xpathProvider);
	}
	
	@Override
	public EvaluationContext createContext(PolicySet policySet) {
		return new PolicySetEvaluationContext(policySet, attributeResolver, policyResolver, xpathProvider);
	}
}
