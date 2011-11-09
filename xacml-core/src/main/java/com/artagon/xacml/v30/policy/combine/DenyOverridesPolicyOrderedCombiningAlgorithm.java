package com.artagon.xacml.v30.policy.combine;

import com.artagon.xacml.v30.pdp.CompositeDecisionRule;

public final class DenyOverridesPolicyOrderedCombiningAlgorithm extends DenyOverrides<CompositeDecisionRule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:ordered-deny-overrides";
	
	public DenyOverridesPolicyOrderedCombiningAlgorithm(){
		super(ID);
	}	
}


