package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.v3.PolicyReferenceResolver;
import com.artagon.xacml.v3.PolicySet;
import com.artagon.xacml.v3.PolicySetDefaults;
import com.artagon.xacml.v3.XPathVersion;
import com.artagon.xacml.v3.policy.spi.XPathProvider;
import com.google.common.base.Preconditions;

final class PolicySetEvaluationContext extends BaseEvaluationContext
{
	private PolicySet policySet;
	private XPathVersion xpathVersion;
	
	public PolicySetEvaluationContext(PolicySet policySet, 
			ContextHandler attributeService, 
			XPathProvider xpathProvider,
			PolicyReferenceResolver policyResolver){
		super(attributeService, xpathProvider, policyResolver);
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

