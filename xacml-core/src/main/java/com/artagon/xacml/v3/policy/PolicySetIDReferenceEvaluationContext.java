package com.artagon.xacml.v3.policy;

import com.artagon.xacml.util.Preconditions;

final class PolicySetIDReferenceEvaluationContext extends DelegatingEvaluationContext
{
	private PolicySetIDReference policySetIDRef;
	private PolicySet referencedPolicySet;

	public PolicySetIDReferenceEvaluationContext(
			EvaluationContext context, PolicySetIDReference policySetIDRef, 
			PolicySet resolvedPolicySet) {
		super(context);
		Preconditions.checkNotNull(policySetIDRef);
		Preconditions.checkArgument(resolvedPolicySet != null && 
				resolvedPolicySet.getId().equals(policySetIDRef.getId()));
		this.policySetIDRef = policySetIDRef;
		this.referencedPolicySet = resolvedPolicySet;
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
