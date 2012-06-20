package com.artagon.xacml.v30.policy.combine;

import com.artagon.xacml.v30.CompositeDecisionRule;

public final class PermitOverridesPolicyOrderedCombineAlgorithm extends 
	PermitOverrides<CompositeDecisionRule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:ordered-permit-overrides";
	
	public PermitOverridesPolicyOrderedCombineAlgorithm() {
		super(ID);
	}
}
