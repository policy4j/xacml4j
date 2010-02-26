package com.artagon.xacml.v3.policy.impl.combine;

import com.artagon.xacml.v3.policy.Rule;

public class PolicyPermitOverridesCombineAlgorithm extends PermitOverrides<Rule>
{
	public final static String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-overrides";
	
	public PolicyPermitOverridesCombineAlgorithm() {
		super(ID);
	}
}
