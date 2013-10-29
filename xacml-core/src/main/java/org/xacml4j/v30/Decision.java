package org.xacml4j.v30;

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
