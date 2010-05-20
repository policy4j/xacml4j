package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.v3.CombinerParameter;
import com.artagon.xacml.v3.PolicySetCombinerParameters;
import com.artagon.xacml.v3.PolicyVisitor;

final class DefaultPolicySetCombinerParameters extends BaseDecisionCombinerParameters 
	implements PolicySetCombinerParameters
{
	private String policySetId; 
	
	public DefaultPolicySetCombinerParameters(
			Iterable<CombinerParameter> parameters) {
		super(parameters);
	}

	@Override
	public String getPolicySetId() {
		return policySetId;
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
