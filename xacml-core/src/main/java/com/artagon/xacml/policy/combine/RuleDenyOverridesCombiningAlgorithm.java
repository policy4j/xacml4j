package com.artagon.xacml.policy.combine;

import com.artagon.xacml.policy.Rule;

public class RuleDenyOverridesCombiningAlgorithm extends DenyOverrides<Rule>
{
	public RuleDenyOverridesCombiningAlgorithm(){
		super("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides");
	}
}
