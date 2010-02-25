package com.artagon.xacml.v3.policy;

import com.artagon.xacml.util.Preconditions;

final class PolicyIDReferenceEvaluationContext extends
		DelegatingEvaluationContext 
{
	private PolicyIDReference policyRef;
	private Policy policy;

	/**
	 * Creates policy evaluation context with a given parent context
	 * 
	 * @param context
	 *            a parent evaluation context
	 * @param policy
	 *            a policy to be evaluated
	 */
	PolicyIDReferenceEvaluationContext(EvaluationContext context, 
			PolicyIDReference ref) 
			throws EvaluationException 
	{
		super(context);
		Preconditions.checkNotNull(context.getCurrentPolicySet());
		Preconditions.checkArgument(context.getCurrentPolicy() == null);
		this.policyRef = ref;
		this.policy = context.resolve(policyRef);
	}

	@Override
	public Policy getCurrentPolicy() {
		return policy;
	}

	@Override
	public PolicyIDReference getCurrentPolicyIDReference() {
		return policyRef;
	}
}
