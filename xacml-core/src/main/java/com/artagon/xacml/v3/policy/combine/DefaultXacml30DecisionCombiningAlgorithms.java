package com.artagon.xacml.v3.policy.combine;

import com.artagon.xacml.v3.spi.combine.BaseDecisionCombingingAlgorithmProvider;

public class DefaultXacml30DecisionCombiningAlgorithms 
	extends BaseDecisionCombingingAlgorithmProvider 
{
	public DefaultXacml30DecisionCombiningAlgorithms() 
	{
		super();
		addRuleCombineAlgorithm(new FirstApplicableRuleCombiningAlgorithm());
		
		addRuleCombineAlgorithm(new PermitOverridesRuleCombineAlgorithm());
		addRuleCombineAlgorithm(new OrderedPermitOverridesRuleCombingingAlgorithm());
		
		addRuleCombineAlgorithm(new DenyOverridesRuleCombiningAlgorithm());
		addRuleCombineAlgorithm(new OrderedDenyOverridesRuleCombingingAlgorithm());
		
		addRuleCombineAlgorithm(new PermitUnlessDenyRuleCombiningAlgorithm());
		addRuleCombineAlgorithm(new DenyUnlessPermitRuleCombingingAlgorithm());
		
		addCompositeRuleCombineAlgorithm(new DenyOverridesPolicyCombiningAlgorithm());
		addCompositeRuleCombineAlgorithm(new OrderedDenyOverridesPolicyCombiningAlgorithm());
			
		addCompositeRuleCombineAlgorithm(new PermitOverridesPolicyCombineAlgorithm());
		addCompositeRuleCombineAlgorithm(new OrderedPermitOverridesPolicyCombineAlgorithm());
	
		addCompositeRuleCombineAlgorithm(new FirstApplicablePolicyCombiningAlgorithm());
		addCompositeRuleCombineAlgorithm(new OnlyOneApplicablePolicyCombingingAlgorithm());
		addCompositeRuleCombineAlgorithm(new PermitUnlessDenyPolicyCombingingAlgorithm());
		addCompositeRuleCombineAlgorithm(new DenyUnlessPermitPolicyCombingingAlgorithm());	
		
		// Legacy algorithms
		
		// rule combining algorithms
		addRuleCombineAlgorithm(new LegacyDenyOverridesRuleCombineAlgorithm());
		addRuleCombineAlgorithm(new LegacyOrderedDenyOverridesRuleCombineAlgorihm());
		addRuleCombineAlgorithm(new LegacyPermitOverridesRuleCombineAlgorithm());
		addRuleCombineAlgorithm(new LegacyOrderedPermitOverridesRuleCombineAlgorithm());
		
		// policy combining algorithms
		addCompositeRuleCombineAlgorithm(new LegacyDenyOverridesPolicyCombineAlgorithm());
		addCompositeRuleCombineAlgorithm(new LegacyOrderedDenyOverridesPolicyCombineAlgorithm());
		addCompositeRuleCombineAlgorithm(new LegacyPermitOverridesPolicyCombineAlgorithm());
		addCompositeRuleCombineAlgorithm(new LeagacyOrderedPermitOverridesPolicyCombineAlgorithm());
	}
}
