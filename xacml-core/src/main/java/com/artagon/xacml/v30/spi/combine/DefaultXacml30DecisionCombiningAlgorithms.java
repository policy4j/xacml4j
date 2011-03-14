package com.artagon.xacml.v30.spi.combine;

import com.artagon.xacml.v30.policy.combine.DenyOverridesPolicyCombiningAlgorithm;
import com.artagon.xacml.v30.policy.combine.DenyOverridesRuleCombiningAlgorithm;
import com.artagon.xacml.v30.policy.combine.DenyUnlessPermitPolicyCombingingAlgorithm;
import com.artagon.xacml.v30.policy.combine.DenyUnlessPermitRuleCombingingAlgorithm;
import com.artagon.xacml.v30.policy.combine.FirstApplicablePolicyCombiningAlgorithm;
import com.artagon.xacml.v30.policy.combine.FirstApplicableRuleCombiningAlgorithm;
import com.artagon.xacml.v30.policy.combine.LeagacyOrderedPermitOverridesPolicyCombineAlgorithm;
import com.artagon.xacml.v30.policy.combine.LegacyDenyOverridesPolicyCombineAlgorithm;
import com.artagon.xacml.v30.policy.combine.LegacyDenyOverridesRuleCombineAlgorithm;
import com.artagon.xacml.v30.policy.combine.LegacyOrderedDenyOverridesPolicyCombineAlgorithm;
import com.artagon.xacml.v30.policy.combine.LegacyOrderedDenyOverridesRuleCombineAlgorihm;
import com.artagon.xacml.v30.policy.combine.LegacyOrderedPermitOverridesRuleCombineAlgorithm;
import com.artagon.xacml.v30.policy.combine.LegacyPermitOverridesPolicyCombineAlgorithm;
import com.artagon.xacml.v30.policy.combine.LegacyPermitOverridesRuleCombineAlgorithm;
import com.artagon.xacml.v30.policy.combine.OnlyOneApplicablePolicyCombingingAlgorithm;
import com.artagon.xacml.v30.policy.combine.OrderedDenyOverridesPolicyCombiningAlgorithm;
import com.artagon.xacml.v30.policy.combine.OrderedDenyOverridesRuleCombingingAlgorithm;
import com.artagon.xacml.v30.policy.combine.OrderedPermitOverridesPolicyCombineAlgorithm;
import com.artagon.xacml.v30.policy.combine.OrderedPermitOverridesRuleCombingingAlgorithm;
import com.artagon.xacml.v30.policy.combine.PermitOverridesPolicyCombineAlgorithm;
import com.artagon.xacml.v30.policy.combine.PermitOverridesRuleCombineAlgorithm;
import com.artagon.xacml.v30.policy.combine.PermitUnlessDenyPolicyCombingingAlgorithm;
import com.artagon.xacml.v30.policy.combine.PermitUnlessDenyRuleCombiningAlgorithm;

class DefaultXacml30DecisionCombiningAlgorithms 
	extends DecisionCombingingAlgorithmProviderImpl 
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
