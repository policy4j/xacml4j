package com.artagon.xacml.v3.policy.combine;

import com.artagon.xacml.v3.Rule;

final class FirstApplicableRuleCombiningAlgorithm extends FirstApplicable<Rule> 
{
	private final static String ID = "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable";
	
	FirstApplicableRuleCombiningAlgorithm(){
		super(ID);
	}
}
