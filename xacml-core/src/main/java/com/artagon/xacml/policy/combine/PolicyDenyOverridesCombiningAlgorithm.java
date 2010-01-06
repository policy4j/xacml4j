package com.artagon.xacml.policy.combine;

import com.artagon.xacml.policy.Policy;

public class PolicyDenyOverridesCombiningAlgorithm extends DenyOverrides<Policy>
{
	public PolicyDenyOverridesCombiningAlgorithm(){
		super("urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-overrides");
	}
}
