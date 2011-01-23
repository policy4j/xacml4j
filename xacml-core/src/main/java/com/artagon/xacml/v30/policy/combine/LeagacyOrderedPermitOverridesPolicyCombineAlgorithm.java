package com.artagon.xacml.v30.policy.combine;


final class LeagacyOrderedPermitOverridesPolicyCombineAlgorithm extends LegacyPermitOverridesPolicyCombineAlgorithm
{
	private final static String ID = "urn:oasis:names:tc:xacml:1.1:policy-combining-algorithm:ordered-permit-overrides";
	
	public LeagacyOrderedPermitOverridesPolicyCombineAlgorithm(){
		super(ID);
	}
}
