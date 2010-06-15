package com.artagon.xacml.v3.policy.combine;

import com.artagon.xacml.v3.Rule;

final class OrderedDenyOverridesRuleCombingingAlgorithm extends DenyOverrides<Rule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-deny-overrides";
	
	OrderedDenyOverridesRuleCombingingAlgorithm(){
		super(ID);
	}
}
