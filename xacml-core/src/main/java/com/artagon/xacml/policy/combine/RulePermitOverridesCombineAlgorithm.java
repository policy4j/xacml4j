package com.artagon.xacml.policy.combine;

import com.artagon.xacml.policy.CompositeDecision;

public class RulePermitOverridesCombineAlgorithm extends PermitOverrides<CompositeDecision> 
{

	public RulePermitOverridesCombineAlgorithm(String id) {
		super("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides");
	}

}
