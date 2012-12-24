package org.xacml4j.v30.policy.combine;

public final class LegacyOrderedDenyOverridesPolicyCombineAlgorithm 
	extends LegacyDenyOverridesPolicyCombineAlgorithm
{
	private final static String ID = "urn:oasis:names:tc:xacml:1.1:policy-combining-algorithm:ordered-deny-overrides";
	
	public LegacyOrderedDenyOverridesPolicyCombineAlgorithm() {
		super(ID);
	}
}
