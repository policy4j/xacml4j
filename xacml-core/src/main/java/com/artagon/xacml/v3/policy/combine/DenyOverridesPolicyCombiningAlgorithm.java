package com.artagon.xacml.v3.policy.combine;

import com.artagon.xacml.v3.ReferencableDecisionRule;

final class DenyOverridesPolicyCombiningAlgorithm extends DenyOverrides<ReferencableDecisionRule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-overrides";
	
	public DenyOverridesPolicyCombiningAlgorithm(){
		super(ID);
	}	
}
