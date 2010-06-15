package com.artagon.xacml.v3.policy.combine;

import com.artagon.xacml.v3.CompositeDecisionRule;

final class PermitOverridesPolicyCombineAlgorithm extends 
	PermitOverrides<CompositeDecisionRule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-overrides";
	
	public PermitOverridesPolicyCombineAlgorithm() {
		super(ID);
	}
	
	private PermitOverridesPolicyCombineAlgorithm(String algorithmId){
		super(algorithmId);
	}
	
	public static PermitOverridesPolicyCombineAlgorithm getLegacyInstance(){
		return new PermitOverridesPolicyCombineAlgorithm("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:ordered-permit-overrides");
	}
}
