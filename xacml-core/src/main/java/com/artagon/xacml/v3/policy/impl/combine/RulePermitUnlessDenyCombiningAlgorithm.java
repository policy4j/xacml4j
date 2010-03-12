package com.artagon.xacml.v3.policy.impl.combine;

import com.artagon.xacml.v3.policy.Rule;

final class RulePermitUnlessDenyCombiningAlgorithm extends PermitUnlessDeny<Rule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-unless-deny";

	public RulePermitUnlessDenyCombiningAlgorithm() {
		super(ID);
	}	
}
