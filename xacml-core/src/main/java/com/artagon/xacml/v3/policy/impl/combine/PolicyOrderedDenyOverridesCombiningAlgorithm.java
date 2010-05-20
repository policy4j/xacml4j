package com.artagon.xacml.v3.policy.impl.combine;

import com.artagon.xacml.v3.Policy;

final class PolicyOrderedDenyOverridesCombiningAlgorithm extends DenyOverrides<Policy>
{
	public final static String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:ordered-deny-overrides";
	
	public PolicyOrderedDenyOverridesCombiningAlgorithm(){
		super(ID);
	}
	
	private PolicyOrderedDenyOverridesCombiningAlgorithm(String algorithmId){
		super(algorithmId);
	}
	
	public static PolicyOrderedDenyOverridesCombiningAlgorithm getLegacyInstance(){
		return new PolicyOrderedDenyOverridesCombiningAlgorithm("urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:ordered-deny-overrides");
	}	
}


