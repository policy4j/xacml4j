package com.artagon.xacml.v3;


public class PolicyCombinerParameters extends BaseDecisionCombinerParameters 
	implements PolicyElement
{
	private String policyId; 
	
	public PolicyCombinerParameters(
			Iterable<CombinerParameter> parameters) {
		super(parameters);
	}

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


