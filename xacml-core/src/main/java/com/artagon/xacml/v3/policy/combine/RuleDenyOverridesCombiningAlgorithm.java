package com.artagon.xacml.v3.policy.combine;

import com.artagon.xacml.v3.policy.Rule;

final class RuleDenyOverridesCombiningAlgorithm extends DenyOverrides<Rule>
{
	public final static String ID = "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides";
	
	public RuleDenyOverridesCombiningAlgorithm(){
		super(ID);
	}
	
	private RuleDenyOverridesCombiningAlgorithm(String algorithmId){
		super(algorithmId);
	}
	
	public static RuleDenyOverridesCombiningAlgorithm getLegacyInstance(){
		return new RuleDenyOverridesCombiningAlgorithm("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:deny-overrides");
	}
}
