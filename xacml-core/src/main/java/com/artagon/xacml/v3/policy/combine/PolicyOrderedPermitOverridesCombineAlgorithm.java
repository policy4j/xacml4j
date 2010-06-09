package com.artagon.xacml.v3.policy.combine;

import com.artagon.xacml.v3.policy.CompositeDecisionRule;

final class PolicyOrderedPermitOverridesCombineAlgorithm extends 
	PermitOverrides<CompositeDecisionRule>
{
	public final static String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:ordered-permit-overrides";
	
	public PolicyOrderedPermitOverridesCombineAlgorithm() {
		super(ID);
	}
	
	private PolicyOrderedPermitOverridesCombineAlgorithm(String algorithmId){
		super(algorithmId);
	}
	
	public static PolicyOrderedPermitOverridesCombineAlgorithm getLegacyInstance(){
		return new PolicyOrderedPermitOverridesCombineAlgorithm("urn:oasis:names:tc:xacml:1.1:policy-combining-algorithm:ordered-permit-overrides");
	}
}
