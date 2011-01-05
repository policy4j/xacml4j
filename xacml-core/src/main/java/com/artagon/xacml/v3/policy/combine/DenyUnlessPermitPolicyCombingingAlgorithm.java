package com.artagon.xacml.v3.policy.combine;

import com.artagon.xacml.v3.CompositeDecisionRule;

final class DenyUnlessPermitPolicyCombingingAlgorithm extends DenyUnlessPermit<CompositeDecisionRule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-unless-permit";
	
	public DenyUnlessPermitPolicyCombingingAlgorithm(){
		super(ID);
	}
}
