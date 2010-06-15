package com.artagon.xacml.v3.policy.combine;

import com.artagon.xacml.v3.CompositeDecisionRule;

final class DenyOverridesPolicyCombiningAlgorithm extends DenyOverrides<CompositeDecisionRule>
{
	public final static String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-overrides";
	
	DenyOverridesPolicyCombiningAlgorithm(){
		super(ID);
	}
	
	private DenyOverridesPolicyCombiningAlgorithm(String algorithmId){
		super(algorithmId);
	}	
}
