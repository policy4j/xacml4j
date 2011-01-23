
package com.artagon.xacml.v30.policy.combine;

import com.artagon.xacml.v30.Rule;

final class OrderedPermitOverridesRuleCombingingAlgorithm extends PermitOverrides<Rule> 
{
	public final static String ID = "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-permit-overrides";
	
	public OrderedPermitOverridesRuleCombingingAlgorithm() {
		super(ID);
	}
}
