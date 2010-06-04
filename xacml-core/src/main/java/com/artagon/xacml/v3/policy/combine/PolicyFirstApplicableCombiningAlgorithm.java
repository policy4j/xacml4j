package com.artagon.xacml.v3.policy.combine;

import com.artagon.xacml.v3.policy.CompositeDecisionRule;

final class PolicyFirstApplicableCombiningAlgorithm extends FirstApplicable<CompositeDecisionRule>
{
	public final static String ID = "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:first-applicable";
	
	public PolicyFirstApplicableCombiningAlgorithm(){
		super(ID);
	}
}
