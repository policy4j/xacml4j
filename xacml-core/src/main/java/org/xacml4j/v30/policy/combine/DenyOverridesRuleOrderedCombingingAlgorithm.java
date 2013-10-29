package org.xacml4j.v30.policy.combine;

import org.xacml4j.v30.pdp.Rule;

public final class DenyOverridesRuleOrderedCombingingAlgorithm extends DenyOverrides<Rule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-deny-overrides";

	public DenyOverridesRuleOrderedCombingingAlgorithm(){
		super(ID);
	}
}
