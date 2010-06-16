package com.artagon.xacml.v3.policy.combine.legacy;


final class OrderedPermitOverridesPolicyCombineAlgorithm extends PermitOverridesPolicyCombineAlgorithm
{
	private final static String ID = "urn:oasis:names:tc:xacml:1.1:policy-combining-algorithm:ordered-permit-overrides";
	
	public OrderedPermitOverridesPolicyCombineAlgorithm(){
		super(ID);
	}
}
