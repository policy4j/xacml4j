package com.artagon.xacml.v3.policy.impl.combine;

import com.artagon.xacml.v3.policy.spi.combine.BaseDecisionCombingingAlgoritmProvider;

public class DefaultDecisionCombiningAlgorithmProvider extends BaseDecisionCombingingAlgoritmProvider 
{

	public DefaultDecisionCombiningAlgorithmProvider() 
	{
		super();
		addRuleCombineAlgorithm(new RuleFirstApplicableCombiningAlgorithm());
		addRuleCombineAlgorithm(new RulePermitOverridesCombineAlgorithm());
		addRuleCombineAlgorithm(new RuleDenyOverridesCombiningAlgorithm());
		addRuleCombineAlgorithm(new RulePermitUnlessDenyCombiningAlgorithm());
		addCompositeRuleCombineAlgorithm(new PolicyDenyOverridesCombiningAlgorithm());
		addCompositeRuleCombineAlgorithm(new PolicyPermitOverridesCombineAlgorithm());
		addCompositeRuleCombineAlgorithm(new PolicyFirstApplicableCombiningAlgorithm());
		addCompositeRuleCombineAlgorithm(new PolicyOnlyOneApplicableCombingingAlgorithm());
		addCompositeRuleCombineAlgorithm(new PolicyPermitUnlessDenyCombiningAlgorithm());
	}
}
