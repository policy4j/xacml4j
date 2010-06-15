package com.artagon.xacml.v3.policy.combine;

import com.artagon.xacml.v3.policy.spi.combine.BaseDecisionCombingingAlgoritmProvider;

public class DefaultDecisionCombiningAlgorithmProvider extends BaseDecisionCombingingAlgoritmProvider 
{
	public DefaultDecisionCombiningAlgorithmProvider() 
	{
		super();
		addRuleCombineAlgorithm(new FirstApplicableRuleCombiningAlgorithm());
		
		addRuleCombineAlgorithm(new PermitOverridesRuleCombineAlgorithm());
		addRuleCombineAlgorithm(new OrderedPermitOverridesRuleCombingingAlgorithm());
		// legacy
		addRuleCombineAlgorithm(PermitOverridesRuleCombineAlgorithm.getLegacyInstance());
		addRuleCombineAlgorithm(OrderedPermitOverridesRuleCombingingAlgorithm.getLegacyInstance());
		
		addRuleCombineAlgorithm(new DenyOverridesRuleCombiningAlgorithm());
		addRuleCombineAlgorithm(new OrderedDenyOverridesRuleCombingingAlgorithm());
		
		// legacy
		addRuleCombineAlgorithm(new DenyOverridesRuleLegacyCombineAlgorithm());
		addRuleCombineAlgorithm(new OrderedDenyOverridesRuleLegacyCombineAlgorihm());
		
		addRuleCombineAlgorithm(new PermitUnlessDenyRuleCombiningAlgorithm());
		addRuleCombineAlgorithm(new DenyUnlessPermitRuleCombingingAlgorithm());
		
		addCompositeRuleCombineAlgorithm(new DenyOverridesPolicyCombiningAlgorithm());
		addCompositeRuleCombineAlgorithm(new OrderedDenyOverridesPolicyCombiningAlgorithm());
		
		// legacy
		addCompositeRuleCombineAlgorithm(new DenyOverridesPolicyLegacyCombineAlgorithm());
		addCompositeRuleCombineAlgorithm(new OrderedDenyOverridesPolicyLegacyCombineAlgorithm());
		
		addCompositeRuleCombineAlgorithm(new PermitOverridesPolicyCombineAlgorithm());
		addCompositeRuleCombineAlgorithm(new OrderedPermitOverridesPolicyCombineAlgorithm());
		
		// legacy
		
		addCompositeRuleCombineAlgorithm(new FirstApplicablePolicyCombiningAlgorithm());
		addCompositeRuleCombineAlgorithm(new OnlyOneApplicablePolicyCombingingAlgorithm());
		addCompositeRuleCombineAlgorithm(new PolicyPermitUnlessDenyCombiningAlgorithm());
		addCompositeRuleCombineAlgorithm(new DenyUnlessPermitPolicyCombingingAlgorithm());	
	}
}
