package com.artagon.xacml.policy.combine;

import com.artagon.xacml.policy.Policy;

public class PolicyFirstApplicableCombiningAlgorithm extends FirstApplicable<Policy>
{
	public final static String ID = "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:first-applicable";
	
	public PolicyFirstApplicableCombiningAlgorithm(){
		super(ID);
	}
}
