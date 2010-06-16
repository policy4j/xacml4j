package com.artagon.xacml.v3.policy.combine.legacy;

final class OrderedPermitOverridesRuleCombineAlgorithm extends PermitOverridesRuleCombineAlgorithm 
{
	private final static String ID = "urn:oasis:names:tc:xacml:1.1:rule-combining-algorithm:ordered-permit-overrides";

	public OrderedPermitOverridesRuleCombineAlgorithm() {
		super(ID);
	}
}
