package com.artagon.xacml.v3.policy.impl.combine;

import com.artagon.xacml.v3.policy.Rule;

public class RuleDenyOverridesCombiningAlgorithm extends DenyOverrides<Rule>
{
	public final static String ID = "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides";
	
	public RuleDenyOverridesCombiningAlgorithm(){
		super(ID);
	}
}
