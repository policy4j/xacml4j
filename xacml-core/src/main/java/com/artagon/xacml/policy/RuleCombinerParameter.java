package com.artagon.xacml.policy;

import com.artagon.xacml.util.Preconditions;

public final class RuleCombinerParameter extends DecisionCombinerParameter
{
	private String ruleId;
	
	public RuleCombinerParameter(String ruleId, 
			String name, Attribute value){
		super(name, value);
		Preconditions.checkNotNull(ruleId);
		this.ruleId = ruleId;
	}
	
	public String getRuleId(){
		return ruleId;
	}
}
