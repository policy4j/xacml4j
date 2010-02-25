package com.artagon.xacml.v3.policy;

import com.artagon.xacml.util.Preconditions;

final class PolicySetIDReferenceEvaluationContext extends DelegatingEvaluationContext
{
	private PolicySetIDReference policySetIDRef;
	private PolicySet referencedPolicySet;

	public PolicySetIDReferenceEvaluationContext(
			EvaluationContext context, PolicySetIDReference policySetIDRef, 
			PolicySet policySet) {
		super(context);
		Preconditions.checkNotNull(policySet);
		Preconditions.checkNotNull(policySetIDRef);
		Preconditions.checkArgument(policySet.getId().equals(policySetIDRef.getId()));
		this.policySetIDRef = policySetIDRef;
		this.referencedPolicySet = policySet;
	}

	@Override
	public PolicySet getCurrentPolicySet() {
		return referencedPolicySet;
	}

	@Override
	public PolicySetIDReference getCurrentPolicySetIDReference() {
		return policySetIDRef;
	}
}
