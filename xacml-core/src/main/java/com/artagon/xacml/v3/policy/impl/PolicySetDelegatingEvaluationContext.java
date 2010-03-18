package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.policy.PolicySet;
import com.artagon.xacml.v3.policy.PolicySetDefaults;
import com.artagon.xacml.v3.policy.XPathVersion;

final class PolicySetDelegatingEvaluationContext extends DelegatingEvaluationContext 
{
	private PolicySet policySet;
	
	/**
	 * Constructs delegating evaluation context
	 * which delegates all invocations to the enclosing
	 * policy set or root context to evaluate given 
	 * policy set
	 * 
	 * @param parentContext a parent context
	 * @param policySet a policy set to be evaluated
	 */
	public PolicySetDelegatingEvaluationContext(
			EvaluationContext parentContext, 
			PolicySet policySet){
		super(parentContext);
		Preconditions.checkArgument(parentContext.getCurrentPolicySet() != null);
		Preconditions.checkArgument(parentContext.getCurrentPolicySet() != policySet);
		Preconditions.checkArgument(parentContext.getCurrentPolicy() == null);
		Preconditions.checkNotNull(policySet);
		this.policySet = policySet;
	}

	@Override
	public PolicySet getCurrentPolicySet() {
		return policySet;
	}

	@Override
	public XPathVersion getXPathVersion() {
		PolicySetDefaults defaults = policySet.getDefaults();
		if(defaults != null && 
				defaults.getXPathVersion() != null){
			return defaults.getXPathVersion();
		}
		return super.getXPathVersion();
	}
}
