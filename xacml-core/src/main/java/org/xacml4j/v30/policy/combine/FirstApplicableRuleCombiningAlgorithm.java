package org.xacml4j.v30.policy.combine;

import org.xacml4j.v30.pdp.Rule;

public final class FirstApplicableRuleCombiningAlgorithm extends FirstApplicable<Rule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable";

	public FirstApplicableRuleCombiningAlgorithm(){
		super(ID);
	}
}
