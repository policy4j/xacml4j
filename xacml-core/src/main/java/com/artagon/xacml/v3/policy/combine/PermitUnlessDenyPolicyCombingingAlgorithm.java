package com.artagon.xacml.v3.policy.combine;

import com.artagon.xacml.v3.ReferencableDecisionRule;

final class PermitUnlessDenyPolicyCombingingAlgorithm extends 
	PermitUnlessDeny<ReferencableDecisionRule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-unless-deny";

	public PermitUnlessDenyPolicyCombingingAlgorithm() {
		super(ID);
	}	
}


