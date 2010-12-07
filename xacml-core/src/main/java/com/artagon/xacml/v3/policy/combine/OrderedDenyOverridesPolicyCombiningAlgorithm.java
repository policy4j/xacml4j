package com.artagon.xacml.v3.policy.combine;

import com.artagon.xacml.v3.policy.CompositeDecisionRule;

final class OrderedDenyOverridesPolicyCombiningAlgorithm extends DenyOverrides<CompositeDecisionRule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:ordered-deny-overrides";
	
	public OrderedDenyOverridesPolicyCombiningAlgorithm(){
		super(ID);
	}	
}


