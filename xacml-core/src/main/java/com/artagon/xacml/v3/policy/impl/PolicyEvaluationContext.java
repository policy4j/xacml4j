package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.AttributeResolver;
import com.artagon.xacml.v3.policy.Policy;
import com.artagon.xacml.v3.policy.DecisionRuleReferenceResolver;
import com.artagon.xacml.v3.policy.PolicyDefaults;
import com.artagon.xacml.v3.policy.XPathVersion;

final class PolicyEvaluationContext extends BaseEvaluationContext
{	
	private Policy policy;
	private XPathVersion xpathVersion;
	
	/**
	 * Creates policy evaluation context without parent evaluation context
	 * @param policy
	 * @param service
	 */
	PolicyEvaluationContext(Policy policy, 
			AttributeResolver service, 
			DecisionRuleReferenceResolver policyResolver)
	{
		super(service, policyResolver);
		Preconditions.checkNotNull(policy);
		this.policy = policy;
		this.xpathVersion = XPathVersion.XPATH1;
	}
	
	@Override
	public Policy getCurrentPolicy() {
		return policy;
	}

	@Override
	public XPathVersion getXPathVersion() {
		PolicyDefaults defaults = policy.getDefaults();
		return (defaults == null)?xpathVersion:((
				defaults.getXPathVersion() == null)?
						xpathVersion:defaults.getXPathVersion());
	}

}
