package com.artagon.xacml.policy.combine;

import com.artagon.xacml.policy.Policy;

public class PolicyDenyOverridesCombiningAlgorithm extends DenyOverrides<Policy>
{
	public final static String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-overrides";
	
	public PolicyDenyOverridesCombiningAlgorithm(){
		super(ID);
	}
}
