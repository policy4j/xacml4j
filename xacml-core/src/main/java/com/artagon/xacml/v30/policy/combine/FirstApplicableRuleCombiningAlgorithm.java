package com.artagon.xacml.v30.policy.combine;

import com.artagon.xacml.v30.Rule;

final class FirstApplicableRuleCombiningAlgorithm extends FirstApplicable<Rule> 
{
	private final static String ID = "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable";
	
	public FirstApplicableRuleCombiningAlgorithm(){
		super(ID);
	}
}
