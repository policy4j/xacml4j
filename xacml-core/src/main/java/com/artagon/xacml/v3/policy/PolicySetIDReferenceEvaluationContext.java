package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.EvaluationContext;
import com.google.common.base.Preconditions;

public class PolicySetIDReferenceEvaluationContext extends DelegatingEvaluationContext
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
