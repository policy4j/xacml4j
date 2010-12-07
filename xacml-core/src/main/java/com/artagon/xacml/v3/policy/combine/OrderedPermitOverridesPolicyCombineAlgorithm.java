package com.artagon.xacml.v3.policy.combine;

import com.artagon.xacml.v3.CompositeDecisionRule;

final class OrderedPermitOverridesPolicyCombineAlgorithm extends 
	PermitOverrides<CompositeDecisionRule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:ordered-permit-overrides";
	
	public OrderedPermitOverridesPolicyCombineAlgorithm() {
		super(ID);
	}
}
