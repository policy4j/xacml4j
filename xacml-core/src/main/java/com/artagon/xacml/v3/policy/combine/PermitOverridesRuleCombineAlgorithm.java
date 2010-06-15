package com.artagon.xacml.v3.policy.combine;

import com.artagon.xacml.v3.Rule;

final class PermitOverridesRuleCombineAlgorithm extends PermitOverrides<Rule> 
{
	public final static String ID = "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides";
	
	public PermitOverridesRuleCombineAlgorithm() {
		super(ID);
	}
	
	private PermitOverridesRuleCombineAlgorithm(String algorithmId){
		super(algorithmId);
	}
	
	public static PermitOverridesRuleCombineAlgorithm getLegacyInstance(){
		return new PermitOverridesRuleCombineAlgorithm("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:permit-overrides");
	}
}
