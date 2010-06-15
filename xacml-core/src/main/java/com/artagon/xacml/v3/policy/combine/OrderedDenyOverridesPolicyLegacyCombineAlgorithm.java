package com.artagon.xacml.v3.policy.combine;

class OrderedDenyOverridesPolicyLegacyCombineAlgorithm extends DenyOverridesPolicyLegacyCombineAlgorithm
{
	private final static String ID = "urn:oasis:names:tc:xacml:1.1:policy-combining-algorithm:ordered-deny-overrides";
	
	OrderedDenyOverridesPolicyLegacyCombineAlgorithm() {
		super(ID);
	}
}
