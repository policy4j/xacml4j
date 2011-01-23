package com.artagon.xacml.v30.policy.combine;

import com.artagon.xacml.v30.CompositeDecisionRule;

final class PermitUnlessDenyPolicyCombingingAlgorithm extends 
	PermitUnlessDeny<CompositeDecisionRule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-unless-deny";

	public PermitUnlessDenyPolicyCombingingAlgorithm() {
		super(ID);
	}	
}


