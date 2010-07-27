package com.artagon.xacml.v3.policy.combine.legacy;


import com.artagon.xacml.v3.spi.combine.DefaultDecisionCombingingAlgoritmProvider;

public class LegacyDecisionCombiningAlgorithms extends DefaultDecisionCombingingAlgoritmProvider 
{
	public LegacyDecisionCombiningAlgorithms() 
	{
		// rule combining algorithms
		addRuleCombineAlgorithm(new DenyOverridesRuleCombineAlgorithm());
		addRuleCombineAlgorithm(new OrderedDenyOverridesRuleCombineAlgorihm());
		addRuleCombineAlgorithm(new PermitOverridesRuleCombineAlgorithm());
		addRuleCombineAlgorithm(new OrderedPermitOverridesRuleCombineAlgorithm());
		
		// policy combining algorithms
		addCompositeRuleCombineAlgorithm(new DenyOverridesPolicyCombineAlgorithm());
		addCompositeRuleCombineAlgorithm(new OrderedDenyOverridesPolicyCombineAlgorithm());
		addCompositeRuleCombineAlgorithm(new PermitOverridesPolicyCombineAlgorithm());
		addCompositeRuleCombineAlgorithm(new OrderedPermitOverridesPolicyCombineAlgorithm());
	}
}
