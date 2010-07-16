package com.artagon.xacml.v3.policy.combine;

import com.artagon.xacml.v3.CompositeDecisionRule;

public final class PermitUnlessDenyPolicyCombingingAlgorithm extends 
	PermitUnlessDeny<CompositeDecisionRule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-unless-deny";

	public PermitUnlessDenyPolicyCombingingAlgorithm() {
		super(ID);
	}	
}


