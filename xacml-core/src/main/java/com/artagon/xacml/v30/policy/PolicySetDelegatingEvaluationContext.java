package com.artagon.xacml.v30.policy;

import com.artagon.xacml.util.Preconditions;

class PolicySetDelegatingEvaluationContext extends DelegatingEvaluationContext 
{
	private PolicySet policySet;
	
	/**
	 * Constructs delegating evaluation context
	 * which delegates all invocations to the enclosing
	 * policy set or root context to evaluate given 
	 * policy set
	 * 
	 * @param context a parent context
	 * @param policySet a policy set to be evaluated
	 */
	public PolicySetDelegatingEvaluationContext(
			EvaluationContext context, 
			PolicySet policySet){
		super(context);
		Preconditions.checkArgument(context.getCurrentPolicySet() != null);
		Preconditions.checkArgument(context.getCurrentPolicySet() != policySet);
		Preconditions.checkArgument(context.getCurrentPolicy() == null);
		Preconditions.checkNotNull(policySet);
		this.policySet = policySet;
	}

	@Override
	public PolicySet getCurrentPolicySet() {
		return policySet;
	}
}
