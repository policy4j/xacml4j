package com.artagon.xacml.v3.policy.combine.legacy;

final class OrderedDenyOverridesPolicyCombineAlgorithm extends DenyOverridesPolicyCombineAlgorithm
{
	private final static String ID = "urn:oasis:names:tc:xacml:1.1:policy-combining-algorithm:ordered-deny-overrides";
	
	public OrderedDenyOverridesPolicyCombineAlgorithm() {
		super(ID);
	}
}
