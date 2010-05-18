package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.PolicySetIDReference;
import com.google.common.base.Preconditions;

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
