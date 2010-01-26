package com.artagon.xacml.v30.policy.combine;

import com.artagon.xacml.v30.policy.Rule;

public class RuleFirstApplicableCombiningAlgorithm extends FirstApplicable<Rule> 
{
	public final static String ID = "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable";
	
	public RuleFirstApplicableCombiningAlgorithm(){
		super(ID);
	}
}
