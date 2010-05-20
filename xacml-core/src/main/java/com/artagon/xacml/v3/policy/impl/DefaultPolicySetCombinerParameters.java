package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.v3.policy.DecisionCombinerParameter;
import com.artagon.xacml.v3.policy.PolicySetCombinerParameters;
import com.artagon.xacml.v3.policy.PolicyVisitor;

final class DefaultPolicySetCombinerParameters extends BaseDecisionCombinerParameters 
	implements PolicySetCombinerParameters
{
	private String policySetId; 
	
	public DefaultPolicySetCombinerParameters(
			Iterable<DecisionCombinerParameter> parameters) {
		super(parameters);
	}

	@Override
	public String getPolicySetId() {
		return policySetId;
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		for(DecisionCombinerParameter p : parameters.values()){
			p.accept(v);
		}
		v.visitLeave(this);
	}
}
