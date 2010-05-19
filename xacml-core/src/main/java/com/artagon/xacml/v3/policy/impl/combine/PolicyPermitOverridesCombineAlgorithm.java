package com.artagon.xacml.v3.policy.impl.combine;

import com.artagon.xacml.v3.policy.CompositeDecisionRule;

final class PolicyPermitOverridesCombineAlgorithm extends 
	PermitOverrides<CompositeDecisionRule>
{
	public final static String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-overrides";
	
	public PolicyPermitOverridesCombineAlgorithm() {
		super(ID);
	}
	
	private PolicyPermitOverridesCombineAlgorithm(String algorithmId){
		super(algorithmId);
	}
	
	public static PolicyPermitOverridesCombineAlgorithm getLegacyInstance(){
		return new PolicyPermitOverridesCombineAlgorithm("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:ordered-permit-overrides");
	}
}
