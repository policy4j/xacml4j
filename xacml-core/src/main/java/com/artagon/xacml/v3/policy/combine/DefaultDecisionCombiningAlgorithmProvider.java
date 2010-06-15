package com.artagon.xacml.v3.policy.combine;

import com.artagon.xacml.v3.policy.combine.legacy.LegacyDenyOverridesPolicyCombineAlgorithm;
import com.artagon.xacml.v3.policy.combine.legacy.LegacyDenyOverridesRuleCombineAlgorithm;
import com.artagon.xacml.v3.policy.combine.legacy.LegacyOrderedDenyOverridesPolicyCombineAlgorithm;
import com.artagon.xacml.v3.policy.combine.legacy.LegacyOrderedDenyOverridesRuleCombineAlgorihm;
import com.artagon.xacml.v3.policy.combine.legacy.LegacyOrderedPermitOverridesRuleCombineAlgorithm;
import com.artagon.xacml.v3.policy.combine.legacy.LegacyPermitOverridesRuleCombineAlgorithm;
import com.artagon.xacml.v3.policy.spi.combine.BaseDecisionCombingingAlgoritmProvider;

public class DefaultDecisionCombiningAlgorithmProvider extends BaseDecisionCombingingAlgoritmProvider 
{
	public DefaultDecisionCombiningAlgorithmProvider() 
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
		addCompositeRuleCombineAlgorithm(new PolicyPermitUnlessDenyCombiningAlgorithm());
		addCompositeRuleCombineAlgorithm(new DenyUnlessPermitPolicyCombingingAlgorithm());	
		
		
		addRuleCombineAlgorithm(new LegacyPermitOverridesRuleCombineAlgorithm());
		addRuleCombineAlgorithm(new LegacyOrderedPermitOverridesRuleCombineAlgorithm());
		
		addRuleCombineAlgorithm(new LegacyDenyOverridesRuleCombineAlgorithm());
		addRuleCombineAlgorithm(new LegacyOrderedDenyOverridesRuleCombineAlgorihm());
		
		addCompositeRuleCombineAlgorithm(new LegacyDenyOverridesPolicyCombineAlgorithm());
		addCompositeRuleCombineAlgorithm(new LegacyOrderedDenyOverridesPolicyCombineAlgorithm());
	}
}
