package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.DecisionResult;

public enum Effect 
{
	PERMIT(DecisionResult.PERMIT),
	DENY(DecisionResult.DENY);
	
	private DecisionResult result;
	
	private Effect(DecisionResult r){
		this.result = r;
	}
	
	public DecisionResult getResult(){
		return result;
	}
}
