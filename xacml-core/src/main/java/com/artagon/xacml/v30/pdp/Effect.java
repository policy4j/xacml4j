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
	 * Gets an applicable decision for this
	 * effect
	 * 
	 * @return an applicable decion
	 */
	public Decision getApplicableDecision(){
		return result;
	}
}
