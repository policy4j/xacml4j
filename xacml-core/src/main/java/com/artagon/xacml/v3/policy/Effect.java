package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.Decision;

public enum Effect 
{
	PERMIT(Decision.PERMIT),
	DENY(Decision.DENY);
	
	private Decision result;
	
	private Effect(Decision r){
		this.result = r;
	}
	
	public Decision getResult(){
		return result;
	}
}
