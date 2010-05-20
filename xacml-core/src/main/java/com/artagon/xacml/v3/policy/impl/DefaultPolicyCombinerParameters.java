package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.v3.policy.CombinerParameter;
import com.artagon.xacml.v3.policy.PolicyCombinerParameters;
import com.artagon.xacml.v3.policy.PolicyVisitor;

final class DefaultPolicyCombinerParameters extends BaseDecisionCombinerParameters 
	implements PolicyCombinerParameters
{
	private String policyId; 
	
	public DefaultPolicyCombinerParameters(
			Iterable<CombinerParameter> parameters) {
		super(parameters);
	}

	@Override
	public String getPolicyId() {
		return policyId;
	}

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		for(CombinerParameter p : parameters.values()){
			p.accept(v);
		}
		v.visitLeave(this);
	}
}


