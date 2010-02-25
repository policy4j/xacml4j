package com.artagon.xacml.v3.policy;

import com.artagon.xacml.util.Preconditions;

final class PolicyIDReferenceEvaluationContext extends
		DelegatingEvaluationContext 
{
	private PolicyIDReference policyRef;
	private Policy referencedPolicy;

	/**
	 * Creates policy evaluation context with a given parent context
	 * 
	 * @param context
	 *            a parent evaluation context
	 * @param policy
	 *            a policy to be evaluated
	 */
	public PolicyIDReferenceEvaluationContext(EvaluationContext context, 
			PolicyIDReference policyIDRef, Policy referencedPolicy) 
	{
		super(context);
		Preconditions.checkNotNull(policyIDRef);
		Preconditions.checkNotNull(context.getCurrentPolicySet());
		Preconditions.checkArgument(context.getCurrentPolicy() == null);
		Preconditions.checkArgument(referencedPolicy == null ||
				policyIDRef.getId().equals(referencedPolicy.getId()));
		this.policyRef = policyIDRef;
		this.referencedPolicy = referencedPolicy;
	}

	@Override
	public Policy getCurrentPolicy() {
		return referencedPolicy;
	}

	@Override
	public PolicyIDReference getCurrentPolicyIDReference() {
		return policyRef;
	}
}
