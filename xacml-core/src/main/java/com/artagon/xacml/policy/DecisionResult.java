package com.artagon.xacml.policy;

public enum DecisionResult 
{
	PERMIT(false),
	DENY(false),
	NOT_APPLICABLE(false),
	INDETERMINATE(true),
	INDETERMINATE_D(true),
	INDETERMINATE_P(true),
	INDETERMINATE_DP(true);
	
	private boolean indeterminate;
	
	private DecisionResult(boolean indeterminate){
		this.indeterminate = indeterminate;
	}
	
	public boolean isIndeterminate(){
		return indeterminate;
	}
}
