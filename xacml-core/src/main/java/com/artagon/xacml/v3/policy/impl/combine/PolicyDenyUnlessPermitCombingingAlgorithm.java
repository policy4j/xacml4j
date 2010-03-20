package com.artagon.xacml.v3.policy.impl.combine;

import com.artagon.xacml.v3.policy.Policy;

public class PolicyDenyUnlessPermitCombingingAlgorithm extends DenyUnlessPermit<Policy>
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-unless-permit";
	
	public PolicyDenyUnlessPermitCombingingAlgorithm(){
		super(ID);
	}
}
