package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.v3.policy.DecisionCombinerParameter;
import com.artagon.xacml.v3.policy.PolicyCombinerParameters;
import com.artagon.xacml.v3.policy.PolicyVisitor;

final class DefaultPolicyCombinerParameters extends BaseDecisionCombinerParameters 
	implements PolicyCombinerParameters
{
	private String ruleId; 
	
	public DefaultPolicyCombinerParameters(
			Iterable<DecisionCombinerParameter> parameters) {
		super(parameters);
	}

	@Override
	public String getPolicyId() {
		return ruleId;
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


