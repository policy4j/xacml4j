package com.artagon.xacml.v3.policy.impl.combine;

import com.artagon.xacml.v3.Rule;

public class RuleDenyUnlessPermitCombingingAlgorithm extends DenyUnlessPermit<Rule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit";
	
	public RuleDenyUnlessPermitCombingingAlgorithm(){
		super(ID);
	}
}
