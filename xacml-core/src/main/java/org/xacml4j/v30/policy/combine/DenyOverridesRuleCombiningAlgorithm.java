package org.xacml4j.v30.policy.combine;

import org.xacml4j.v30.pdp.Rule;

public final class DenyOverridesRuleCombiningAlgorithm extends DenyOverrides<Rule>
{
	public final static String ID = "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides";
	
	public DenyOverridesRuleCombiningAlgorithm(){
		super(ID);
	}
}
