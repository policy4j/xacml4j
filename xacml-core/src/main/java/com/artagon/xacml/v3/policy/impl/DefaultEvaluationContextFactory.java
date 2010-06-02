package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationContextFactory;
import com.artagon.xacml.v3.Policy;
import com.artagon.xacml.v3.PolicyReferenceResolver;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.policy.spi.XPathProvider;
import com.google.common.base.Preconditions;


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
