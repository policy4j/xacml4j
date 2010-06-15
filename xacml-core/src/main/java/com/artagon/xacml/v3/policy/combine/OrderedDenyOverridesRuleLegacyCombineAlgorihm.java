package com.artagon.xacml.v3.policy.combine;

class OrderedDenyOverridesRuleLegacyCombineAlgorihm extends DenyOverridesRuleLegacyCombineAlgorithm
{
	private final static String ID = "urn:oasis:names:tc:xacml:1.1:rule-combining-algorithm:ordered-deny-overrides";

	OrderedDenyOverridesRuleLegacyCombineAlgorihm() {
		super(ID);
	}	
}
