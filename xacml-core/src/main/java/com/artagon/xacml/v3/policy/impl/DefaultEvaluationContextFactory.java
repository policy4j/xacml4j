package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.policy.ContextHandler;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationContextFactory;
import com.artagon.xacml.v3.policy.Policy;
import com.artagon.xacml.v3.policy.PolicyReferenceResolver;
import com.artagon.xacml.v3.policy.PolicySet;
import com.artagon.xacml.v3.policy.spi.XPathProvider;


public class DefaultEvaluationContextFactory implements EvaluationContextFactory
{
	private PolicyReferenceResolver policyResolver;
	private XPathProvider xpathProvider;
	
	public DefaultEvaluationContextFactory(
			PolicyReferenceResolver policyResolver, 
			XPathProvider xpathProvider){
		Preconditions.checkNotNull(policyResolver);
		Preconditions.checkNotNull(xpathProvider);
		this.policyResolver = policyResolver;
		this.xpathProvider = xpathProvider;
	}

	@Override
	public EvaluationContext createContext(Policy policy, Request request) {
		ContextHandler handler = new DefaultContextHandler(xpathProvider, request);
		return new PolicyEvaluationContext(policy, handler , xpathProvider, policyResolver);
	}
	
	@Override
	public EvaluationContext createContext(PolicySet policySet, Request request) {
		ContextHandler handler = new DefaultContextHandler(xpathProvider, request);
		return new PolicySetEvaluationContext(policySet, handler, xpathProvider, policyResolver);
	}
}
