package org.xacml4j.v30.policy.combine;

public final class LegacyOrderedDenyOverridesRuleCombineAlgorihm 
	extends LegacyDenyOverridesRuleCombineAlgorithm
{
	private final static String ID = "urn:oasis:names:tc:xacml:1.1:rule-combining-algorithm:ordered-deny-overrides";

	public LegacyOrderedDenyOverridesRuleCombineAlgorihm() {
		super(ID);
	}	
}
