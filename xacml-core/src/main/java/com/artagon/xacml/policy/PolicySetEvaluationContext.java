package com.artagon.xacml.policy;

import com.artagon.xacml.util.Preconditions;

class PolicySetEvaluationContext extends BaseEvaluationContext
{
	private PolicySet policySet;
	
	public PolicySetEvaluationContext(PolicySet policySet, 
			AttributeResolutionService attributeService){
		super(attributeService);
		Preconditions.checkNotNull(policySet);
		this.policySet = policySet;
	}

	@Override
	public PolicySet getCurrentPolicySet() {
		return policySet;
	}
}
