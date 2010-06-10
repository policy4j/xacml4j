
package com.artagon.xacml.v3.policy.combine;

import com.artagon.xacml.v3.Rule;

final class RuleOrderedPermitOverridesCombingingAlgorithm extends PermitOverrides<Rule> 
{
	public final static String ID = "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-permit-overrides";
	
	public RuleOrderedPermitOverridesCombingingAlgorithm() {
		super(ID);
	}
	
	protected RuleOrderedPermitOverridesCombingingAlgorithm(String algorithmId){
		super(algorithmId);
	}
	
	public static RuleOrderedPermitOverridesCombingingAlgorithm getLegacyInstance(){
		return new RuleOrderedPermitOverridesCombingingAlgorithm("urn:oasis:names:tc:xacml:1.1:rule-combining-algorithm:ordered-permit-overrides");
	}
}
