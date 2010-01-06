package com.artagon.xacml.policy;

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
