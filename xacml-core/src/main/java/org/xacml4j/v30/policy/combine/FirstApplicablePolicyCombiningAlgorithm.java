package org.xacml4j.v30.policy.combine;

import org.xacml4j.v30.CompositeDecisionRule;

public final class FirstApplicablePolicyCombiningAlgorithm extends FirstApplicable<CompositeDecisionRule>
{
	public final static String ID = "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:first-applicable";
	
	public FirstApplicablePolicyCombiningAlgorithm(){
		super(ID);
	}
}
