package com.artagon.xacml.v3.policy;

import com.artagon.xacml.util.Preconditions;

public final class RuleCombinerParameter extends DecisionCombiningAlgorithmParameter
{
	private String ruleId;
	
	public RuleCombinerParameter(String ruleId, 
			String name, AttributeValue value){
		super(name, value);
		Preconditions.checkNotNull(ruleId);
		this.ruleId = ruleId;
	}
	
	public String getRuleId(){
		return ruleId;
	}
}
