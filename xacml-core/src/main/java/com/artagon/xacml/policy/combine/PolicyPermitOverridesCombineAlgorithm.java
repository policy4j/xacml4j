package com.artagon.xacml.policy.combine;

import com.artagon.xacml.policy.Rule;

public class PolicyPermitOverridesCombineAlgorithm extends PermitOverrides<Rule>
{
	public PolicyPermitOverridesCombineAlgorithm(String id) {
		super("urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-overrides");
	}
}
