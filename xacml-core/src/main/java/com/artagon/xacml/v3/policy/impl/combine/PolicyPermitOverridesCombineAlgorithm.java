package com.artagon.xacml.v3.policy.impl.combine;

import com.artagon.xacml.v3.policy.CompositeDecisionRule;

final class PolicyPermitOverridesCombineAlgorithm extends PermitOverrides<CompositeDecisionRule>
{
	public final static String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-overrides";
	
	public PolicyPermitOverridesCombineAlgorithm() {
		super(ID);
	}
}
