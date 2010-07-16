package com.artagon.xacml.v3.policy.combine;

import com.artagon.xacml.v3.Rule;

final class DenyOverridesRuleCombiningAlgorithm extends DenyOverrides<Rule>
{
	public final static String ID = "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides";
	
	public DenyOverridesRuleCombiningAlgorithm(){
		super(ID);
	}
}
