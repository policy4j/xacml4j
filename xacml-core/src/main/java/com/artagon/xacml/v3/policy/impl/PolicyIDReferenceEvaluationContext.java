package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.PolicyIDReference;
import com.artagon.xacml.v3.PolicySetIDReference;
import com.google.common.base.Preconditions;

/**
 * An {@link EvaluationContext} implementation
 * to evaluate {@link PolicySetIDReference} decisions
 * 
 * @author Giedrius Trumpickas
 */
final class PolicyIDReferenceEvaluationContext extends
		DelegatingEvaluationContext 
{
	private PolicyIDReference policyRef;
	
	/**
	 * Creates policy evaluation context with a given parent context
	 * 
	 * @param context a parent evaluation context
	 * @param policyIDRef a policy reference
	 * @exception IllegalArgumentException if enclosing context
	 * {@link EvaluationContext#getCurrentPolicySet()} returns
	 * <code>null</code> or given policy ID reference is
	 * <code>null</code>
	 */
	public PolicyIDReferenceEvaluationContext(EvaluationContext context, 
			PolicyIDReference policyIDRef) 
	{
		super(context);
		Preconditions.checkNotNull(policyIDRef);
		Preconditions.checkNotNull(context.getCurrentPolicySet());
		Preconditions.checkArgument(context.getCurrentPolicy() == null);
		this.policyRef = policyIDRef;
	}
	
	@Override
	public PolicyIDReference getCurrentPolicyIDReference() {
		return policyRef;
	}
}
