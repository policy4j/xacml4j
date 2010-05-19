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
		addRuleCombineAlgorithm(new RuleDenyUnlessPermitCombingingAlgorithm());
		
		addRuleCombineAlgorithm(RuleDenyOverridesCombiningAlgorithm.getLegacyInstance());
		addRuleCombineAlgorithm(RulePermitOverridesCombineAlgorithm.getLegacyInstance());
		
		addPolicyCombineAlgorithm(new PolicyDenyOverridesCombiningAlgorithm());
		addPolicyCombineAlgorithm(new PolicyPermitOverridesCombineAlgorithm());
		addPolicyCombineAlgorithm(new PolicyFirstApplicableCombiningAlgorithm());
		addPolicyCombineAlgorithm(new PolicyOnlyOneApplicableCombingingAlgorithm());
		addPolicyCombineAlgorithm(new PolicyPermitUnlessDenyCombiningAlgorithm());
		addPolicyCombineAlgorithm(new PolicyDenyUnlessPermitCombingingAlgorithm());
		
		addPolicyCombineAlgorithm(PolicyDenyOverridesCombiningAlgorithm.getLegacyInstance());
		addPolicyCombineAlgorithm(PolicyPermitOverridesCombineAlgorithm.getLegacyInstance());
	}
}
