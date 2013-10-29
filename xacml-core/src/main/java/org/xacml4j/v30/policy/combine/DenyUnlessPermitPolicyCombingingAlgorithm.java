package org.xacml4j.v30.policy.combine;

import org.xacml4j.v30.CompositeDecisionRule;

public final class DenyUnlessPermitPolicyCombingingAlgorithm extends DenyUnlessPermit<CompositeDecisionRule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-unless-permit";

	public DenyUnlessPermitPolicyCombingingAlgorithm(){
		super(ID);
	}
}
