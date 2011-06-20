package com.artagon.xacml.v30;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;


public class PolicySetCombinerParameters extends 
	BaseDecisionCombinerParameters 
{
	private String policySetId; 
	
	public PolicySetCombinerParameters(
			String policySetId,
			Iterable<CombinerParameter> parameters) {
		super(parameters);
		Preconditions.checkNotNull(policySetId);
		this.policySetId = policySetId;
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
	
	@Override
	public int hashCode(){
		return Objects.hashCode(
				policySetId, parameters);
	}
	
	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof PolicySetCombinerParameters)){
			return false;
		}
		PolicySetCombinerParameters p = (PolicySetCombinerParameters)o;
		return p.policySetId.equals(p.policySetId) && 
			parameters.equals(p.parameters);
	}
	
	@Override
	public String toString(){
		return Objects.toStringHelper(this)
		.add("policySetId", policySetId)
		.add("parameters", parameters)
		.toString();
	}
}
