package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.policy.PolicySetIDReference;

final class PolicySetIDReferenceEvaluationContext extends DelegatingEvaluationContext
{
	private PolicySetIDReference policySetIDRef;

	public PolicySetIDReferenceEvaluationContext(
			EvaluationContext context, PolicySetIDReference policySetIDRef) {
		super(context);
		Preconditions.checkNotNull(policySetIDRef);
		Preconditions.checkNotNull(context.getCurrentPolicySet());
		this.policySetIDRef = policySetIDRef;
	}
	
	@Override
	public PolicySetIDReference getCurrentPolicySetIDReference() {
		return policySetIDRef;
	}
}
