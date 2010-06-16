package com.artagon.xacml.v3.policy.combine.legacy;

final class OrderedDenyOverridesRuleCombineAlgorihm extends DenyOverridesRuleCombineAlgorithm
{
	private final static String ID = "urn:oasis:names:tc:xacml:1.1:rule-combining-algorithm:ordered-deny-overrides";

	public OrderedDenyOverridesRuleCombineAlgorihm() {
		super(ID);
	}	
}
