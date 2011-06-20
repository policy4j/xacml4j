package com.artagon.xacml.v30;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class PolicyCombinerParameters 
	extends BaseDecisionCombinerParameters implements PolicyElement
{
	private String policyId; 
	
	public PolicyCombinerParameters(
			String policyId,
			Iterable<CombinerParameter> parameters) {
		super(parameters);
		Preconditions.checkNotNull(policyId);
		this.policyId = policyId;
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
	
	@Override
	public int hashCode(){
		return Objects.hashCode(policyId, parameters);
	}
	
	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof PolicyCombinerParameters)){
			return false;
		}
		PolicyCombinerParameters p = (PolicyCombinerParameters)o;
		return p.policyId.equals(p.policyId) && 
			parameters.equals(p.parameters);
	}
	
	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("policyId", policyId)
		.add("parameters", parameters)
		.toString();
	}
}


