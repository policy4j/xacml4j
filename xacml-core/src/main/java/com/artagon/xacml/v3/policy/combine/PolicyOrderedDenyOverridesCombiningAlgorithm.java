package com.artagon.xacml.v3.policy.combine;

import com.artagon.xacml.v3.CompositeDecisionRule;

final class PolicyOrderedDenyOverridesCombiningAlgorithm extends DenyOverrides<CompositeDecisionRule>
{
	public final static String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:ordered-deny-overrides";
	
	public PolicyOrderedDenyOverridesCombiningAlgorithm(){
		super(ID);
	}
	
	private PolicyOrderedDenyOverridesCombiningAlgorithm(String algorithmId){
		super(algorithmId);
	}
	
	public static PolicyOrderedDenyOverridesCombiningAlgorithm getLegacyInstance(){
		return new PolicyOrderedDenyOverridesCombiningAlgorithm("urn:oasis:names:tc:xacml:1.1:policy-combining-algorithm:ordered-deny-overrides");
	}	
}


