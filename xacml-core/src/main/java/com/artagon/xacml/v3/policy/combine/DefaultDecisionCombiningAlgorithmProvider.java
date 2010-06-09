package com.artagon.xacml.v3.policy.combine;

import com.artagon.xacml.v3.policy.spi.combine.BaseDecisionCombingingAlgoritmProvider;

public class DefaultDecisionCombiningAlgorithmProvider extends BaseDecisionCombingingAlgoritmProvider 
{
	public DefaultDecisionCombiningAlgorithmProvider() 
	{
		super();
		addRuleCombineAlgorithm(new RuleFirstApplicableCombiningAlgorithm());
		
		addRuleCombineAlgorithm(new RulePermitOverridesCombineAlgorithm());
		addRuleCombineAlgorithm(new RuleOrderedPermitOverridesCombingingAlgorithm());
		// legacy
		addRuleCombineAlgorithm(RulePermitOverridesCombineAlgorithm.getLegacyInstance());
		addRuleCombineAlgorithm(RuleOrderedPermitOverridesCombingingAlgorithm.getLegacyInstance());
		
		addRuleCombineAlgorithm(new RuleDenyOverridesCombiningAlgorithm());
		addRuleCombineAlgorithm(new RuleOrderedDenyOverridesCombingingAlgorithm());
		// legacy
		addRuleCombineAlgorithm(RuleDenyOverridesCombiningAlgorithm.getLegacyInstance());
		addRuleCombineAlgorithm(RuleOrderedDenyOverridesCombingingAlgorithm.getLegacyInstance());
		
		addRuleCombineAlgorithm(new RulePermitUnlessDenyCombiningAlgorithm());
		addRuleCombineAlgorithm(new RuleDenyUnlessPermitCombingingAlgorithm());
		
		addCompositeRuleCombineAlgorithm(new PolicyDenyOverridesCombiningAlgorithm());
		addCompositeRuleCombineAlgorithm(new PolicyOrderedDenyOverridesCombiningAlgorithm());
		// legacy
		addCompositeRuleCombineAlgorithm(PolicyDenyOverridesCombiningAlgorithm.getLegacyInstance());
		addCompositeRuleCombineAlgorithm(PolicyOrderedDenyOverridesCombiningAlgorithm.getLegacyInstance());
		
		addCompositeRuleCombineAlgorithm(new PolicyPermitOverridesCombineAlgorithm());
		addCompositeRuleCombineAlgorithm(new PolicyOrderedPermitOverridesCombineAlgorithm());
		// legacy
		addCompositeRuleCombineAlgorithm(PolicyPermitOverridesCombineAlgorithm.getLegacyInstance());
		addCompositeRuleCombineAlgorithm(PolicyOrderedPermitOverridesCombineAlgorithm.getLegacyInstance());
		
		addCompositeRuleCombineAlgorithm(new PolicyFirstApplicableCombiningAlgorithm());
		addCompositeRuleCombineAlgorithm(new PolicyOnlyOneApplicableCombingingAlgorithm());
		addCompositeRuleCombineAlgorithm(new PolicyPermitUnlessDenyCombiningAlgorithm());
		addCompositeRuleCombineAlgorithm(new PolicyDenyUnlessPermitCombingingAlgorithm());	
	}
}
