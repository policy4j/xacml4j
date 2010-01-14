package com.artagon.xacml.policy.combine;

import com.artagon.xacml.policy.Rule;

public class RuleFirstApplicableCombiningAlgorithm extends FirstApplicable<Rule> 
{
	public final static String ID = "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable";
	
	public RuleFirstApplicableCombiningAlgorithm(){
		super(ID);
	}
}
