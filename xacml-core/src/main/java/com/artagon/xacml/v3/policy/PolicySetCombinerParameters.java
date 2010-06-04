package com.artagon.xacml.v3.policy;


public class PolicySetCombinerParameters extends BaseDecisionCombinerParameters 
{
	private String policySetId; 
	
	public PolicySetCombinerParameters(
			Iterable<CombinerParameter> parameters) {
		super(parameters);
	}

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
