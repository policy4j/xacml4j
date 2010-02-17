package com.artagon.xacml.v3.policy;

import com.artagon.xacml.util.Preconditions;

class PolicySetEvaluationContext extends BaseEvaluationContext
{
	private PolicySet policySet;
	
	public PolicySetEvaluationContext(PolicySet policySet, 
			AttributeResolver attributeService){
		super(attributeService);
		Preconditions.checkNotNull(policySet);
		this.policySet = policySet;
	}

	@Override
	public PolicySet getCurrentPolicySet() {
		return policySet;
	}
}
