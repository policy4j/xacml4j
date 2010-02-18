package com.artagon.xacml.v3.policy;

import com.artagon.xacml.util.Preconditions;

class PolicyDelegatingEvaluationContext extends DelegatingEvaluationContext
{	
	private VariableEvaluationCache cache;
	private Policy policy;
	
	/**
	 * Creates policy evaluation context with a given parent context
	 * 
	 * @param context a parent evaluation context
	 * @param policy a policy to be evaluated
	 */
	PolicyDelegatingEvaluationContext(EvaluationContext context, Policy policy)
	{
		super(context);
		Preconditions.checkNotNull(policy);
		Preconditions.checkNotNull(context.getCurrentPolicySet());
		this.policy = policy;
	}

	@Override
	public Policy getCurrentPolicy() {
		return policy;
	}
	
	public VariableEvaluationCache getVariableEvaluationCache(){
		return cache;
	}
}
