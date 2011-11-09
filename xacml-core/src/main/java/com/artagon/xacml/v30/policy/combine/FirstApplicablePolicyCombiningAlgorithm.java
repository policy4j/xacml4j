package com.artagon.xacml.v30.policy.combine;

import com.artagon.xacml.v30.pdp.CompositeDecisionRule;

public final class FirstApplicablePolicyCombiningAlgorithm extends FirstApplicable<CompositeDecisionRule>
{
	public final static String ID = "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:first-applicable";
	
	public FirstApplicablePolicyCombiningAlgorithm(){
		super(ID);
	}
}
