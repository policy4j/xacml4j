package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.v3.policy.DecisionCombinerParameter;
import com.artagon.xacml.v3.policy.PolicyVisitor;
import com.artagon.xacml.v3.policy.RuleCombinerParameters;

final class DefaultRuleCombinerParameters extends BaseDecisionCombinerParameters 
	implements RuleCombinerParameters
{
	private String ruleId; 
	
	public DefaultRuleCombinerParameters(
			Iterable<DecisionCombinerParameter> parameters) {
		super(parameters);
	}

	@Override
	public String getRuleId() {
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
