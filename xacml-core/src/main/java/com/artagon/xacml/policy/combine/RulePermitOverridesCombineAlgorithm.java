package com.artagon.xacml.policy.combine;

import com.artagon.xacml.policy.CompositeDecision;

public class RulePermitOverridesCombineAlgorithm extends PermitOverrides<CompositeDecision> 
{
	public final static String ID = "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides";
	
	public RulePermitOverridesCombineAlgorithm() {
		super(ID);
	}

}
