package com.artagon.xacml.v3.policy.combine;

import com.artagon.xacml.v3.ReferencableDecisionRule;

final class OrderedPermitOverridesPolicyCombineAlgorithm extends 
	PermitOverrides<ReferencableDecisionRule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:ordered-permit-overrides";
	
	public OrderedPermitOverridesPolicyCombineAlgorithm() {
		super(ID);
	}
}
