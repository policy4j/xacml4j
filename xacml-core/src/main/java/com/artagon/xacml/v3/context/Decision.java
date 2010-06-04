package com.artagon.xacml.v3.context;

public enum Decision 
{
	PERMIT(false),
	DENY(false),
	NOT_APPLICABLE(false),
	INDETERMINATE(true),
	INDETERMINATE_D(true),
	INDETERMINATE_P(true),
	INDETERMINATE_DP(true);
	
	private boolean indeterminate;
	
	private Decision(boolean indeterminate){
		this.indeterminate = indeterminate;
	}
	
	public boolean isIndeterminate(){
		return indeterminate;
	}
}
