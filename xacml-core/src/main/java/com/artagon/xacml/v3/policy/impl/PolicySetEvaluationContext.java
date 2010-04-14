package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.policy.AttributeResolver;
import com.artagon.xacml.v3.policy.PolicyReferenceResolver;
import com.artagon.xacml.v3.policy.PolicySet;
import com.artagon.xacml.v3.policy.PolicySetDefaults;
import com.artagon.xacml.v3.policy.XPathVersion;

final class PolicySetEvaluationContext extends BaseEvaluationContext
{
	private PolicySet policySet;
	private XPathVersion xpathVersion;
	
	public PolicySetEvaluationContext(PolicySet policySet, 
			AttributeResolver attributeService, 
			PolicyReferenceResolver policyResolver){
		super(attributeService, policyResolver);
		Preconditions.checkNotNull(policySet);
		this.policySet = policySet;
		this.xpathVersion = XPathVersion.XPATH1;
	}

	@Override
	public PolicySet getCurrentPolicySet() {
		return policySet;
	}

	@Override
	public XPathVersion getXPathVersion() {
		PolicySetDefaults defaults = policySet.getDefaults();
		return (defaults == null)?xpathVersion:((
				defaults.getXPathVersion() == null)?
						xpathVersion:defaults.getXPathVersion());
	}
}

