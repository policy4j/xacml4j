package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.v3.CombinerParameter;
import com.artagon.xacml.v3.CombinerParameters;
import com.artagon.xacml.v3.PolicyVisitor;

final class DefaultCombinerParameters extends BaseDecisionCombinerParameters 
	implements CombinerParameters
{
	public DefaultCombinerParameters(
			Iterable<CombinerParameter> parameters) {
		super(parameters);
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
