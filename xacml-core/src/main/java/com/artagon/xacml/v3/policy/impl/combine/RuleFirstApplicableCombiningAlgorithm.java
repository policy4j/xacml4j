package com.artagon.xacml.v3.policy.impl.combine;

import com.artagon.xacml.v3.policy.Rule;

public class RuleFirstApplicableCombiningAlgorithm extends FirstApplicable<Rule> 
{
	public final static String ID = "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable";
	
	public RuleFirstApplicableCombiningAlgorithm(){
		super(ID);
	}
}
