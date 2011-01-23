package com.artagon.xacml.v30.policy.combine;

import com.artagon.xacml.v30.CompositeDecisionRule;

final class DenyOverridesPolicyCombiningAlgorithm extends DenyOverrides<CompositeDecisionRule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-overrides";
	
	public DenyOverridesPolicyCombiningAlgorithm(){
		super(ID);
	}	
}
