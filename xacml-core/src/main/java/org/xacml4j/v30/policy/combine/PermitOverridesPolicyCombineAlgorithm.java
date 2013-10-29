package org.xacml4j.v30.policy.combine;

import org.xacml4j.v30.CompositeDecisionRule;

public final class PermitOverridesPolicyCombineAlgorithm extends
	PermitOverrides<CompositeDecisionRule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-overrides";

	public PermitOverridesPolicyCombineAlgorithm() {
		super(ID);
	}
}
