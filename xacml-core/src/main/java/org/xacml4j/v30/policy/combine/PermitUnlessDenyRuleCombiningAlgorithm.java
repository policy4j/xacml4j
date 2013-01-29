package org.xacml4j.v30.policy.combine;

import org.xacml4j.v30.pdp.Rule;

public final class PermitUnlessDenyRuleCombiningAlgorithm extends PermitUnlessDeny<Rule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-unless-deny";

	public PermitUnlessDenyRuleCombiningAlgorithm() {
		super(ID);
	}	
}
