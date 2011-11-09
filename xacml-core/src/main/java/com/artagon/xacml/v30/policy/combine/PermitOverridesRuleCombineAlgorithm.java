package com.artagon.xacml.v30.policy.combine;

import com.artagon.xacml.v30.pdp.Rule;

public final class PermitOverridesRuleCombineAlgorithm extends PermitOverrides<Rule> 
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides";
	
	public PermitOverridesRuleCombineAlgorithm() {
		super(ID);
	}
}
