package com.artagon.xacml.v3.policy.combine;

final class LegacyOrderedPermitOverridesRuleCombineAlgorithm extends LegacyPermitOverridesRuleCombineAlgorithm 
{
	private final static String ID = "urn:oasis:names:tc:xacml:1.1:rule-combining-algorithm:ordered-permit-overrides";

	public LegacyOrderedPermitOverridesRuleCombineAlgorithm() {
		super(ID);
	}
}
