package com.artagon.xacml.v3.policy.combine;

import com.artagon.xacml.v3.Rule;

final class DenyUnlessPermitRuleCombingingAlgorithm extends DenyUnlessPermit<Rule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit";
	
	public DenyUnlessPermitRuleCombingingAlgorithm(){
		super(ID);
	}
}
