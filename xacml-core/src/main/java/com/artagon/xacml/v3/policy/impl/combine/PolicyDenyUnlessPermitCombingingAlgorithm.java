package com.artagon.xacml.v3.policy.impl.combine;

import com.artagon.xacml.v3.CompositeDecisionRule;

public class PolicyDenyUnlessPermitCombingingAlgorithm extends DenyUnlessPermit<CompositeDecisionRule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-unless-permit";
	
	public PolicyDenyUnlessPermitCombingingAlgorithm(){
		super(ID);
	}
}
