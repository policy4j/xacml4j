package com.artagon.xacml.v3;

public enum Decision 
{
	PERMIT(false, "Permit"),
	DENY(false, "Deny"),
	NOT_APPLICABLE(false, "NotApplicable"),
	INDETERMINATE(true, "Indeterminate"),
	INDETERMINATE_D(true, "Indeterminate"),
	INDETERMINATE_P(true, "Indeterminate"),
	INDETERMINATE_DP(true, "Indeterminate");
	
	private String value;
	private boolean indeterminate;
	
	private Decision(boolean indeterminate, 
			String value){
		this.indeterminate = indeterminate;
		this.value = value;
	}
	
	public boolean isIndeterminate(){
		return indeterminate;
	}
	
	@Override
	public String toString(){
		return value;
	}
}
