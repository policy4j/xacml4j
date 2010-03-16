package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.AttributeReferenceResolver;
import com.artagon.xacml.v3.policy.DecisionRuleReferenceResolver;
import com.artagon.xacml.v3.policy.PolicySet;
import com.artagon.xacml.v3.policy.spi.XPathProvider;

final class PolicySetEvaluationContext extends BaseEvaluationContext
{
	private PolicySet policySet;
	
	public PolicySetEvaluationContext(PolicySet policySet, 
			AttributeReferenceResolver attributeService, 
			DecisionRuleReferenceResolver policyResolver, XPathProvider xpathProvider){
		super(attributeService, policyResolver, xpathProvider);
		Preconditions.checkNotNull(policySet);
		this.policySet = policySet;
	}

	@Override
	public PolicySet getCurrentPolicySet() {
		return policySet;
	}
}
