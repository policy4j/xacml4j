package com.artagon.xacml.v3;

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
	public EvaluationContext createContext(CompositeDecisionRule policy, Request request) {
		ContextHandler handler = new DefaultContextHandler(xpathProvider, request);
		try{
			
			return new PolicySetEvaluationContext(
					(PolicySet)policy, handler , xpathProvider, policyResolver);
		}catch(ClassCastException e){
			return new PolicyEvaluationContext(
					(Policy)policy, 
					handler , 
					xpathProvider, 
					policyResolver);
		}
	}	
}
