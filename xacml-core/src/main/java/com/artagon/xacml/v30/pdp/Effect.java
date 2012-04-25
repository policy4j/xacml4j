package com.artagon.xacml.v30.pdp;

public enum Effect 
{
	PERMIT(Decision.PERMIT),
	DENY(Decision.DENY);
	
	private Decision result;
	
	private Effect(Decision r){
		this.result = r;
	}
	
	/**
	 * Converts this effect 
	 * to an access decision
	 * 
	 * @return {@link Decision}
	 */
	public Decision toDecision(){
		return result;
	}
}
