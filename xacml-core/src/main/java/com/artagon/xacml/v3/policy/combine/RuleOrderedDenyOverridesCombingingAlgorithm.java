package com.artagon.xacml.v3.policy.combine;

import com.artagon.xacml.v3.policy.Rule;

final class RuleOrderedDenyOverridesCombingingAlgorithm extends DenyOverrides<Rule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-deny-overrides";
	
	public RuleOrderedDenyOverridesCombingingAlgorithm(){
		super(ID);
	}
	
	private RuleOrderedDenyOverridesCombingingAlgorithm(String algorithmId){
		super(algorithmId);
	}
	
	public static RuleOrderedDenyOverridesCombingingAlgorithm getLegacyInstance(){
		return new RuleOrderedDenyOverridesCombingingAlgorithm("urn:oasis:names:tc:xacml:1.1:rule-combining-algorithm:ordered-deny-overrides");
	}
}
