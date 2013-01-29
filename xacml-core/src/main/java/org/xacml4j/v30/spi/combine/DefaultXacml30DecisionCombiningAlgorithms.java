package org.xacml4j.v30.spi.combine;

import org.xacml4j.v30.policy.combine.DenyOverridesPolicyCombiningAlgorithm;
import org.xacml4j.v30.policy.combine.DenyOverridesPolicyOrderedCombiningAlgorithm;
import org.xacml4j.v30.policy.combine.DenyOverridesRuleCombiningAlgorithm;
import org.xacml4j.v30.policy.combine.DenyOverridesRuleOrderedCombingingAlgorithm;
import org.xacml4j.v30.policy.combine.DenyUnlessPermitPolicyCombingingAlgorithm;
import org.xacml4j.v30.policy.combine.DenyUnlessPermitRuleCombingingAlgorithm;
import org.xacml4j.v30.policy.combine.FirstApplicablePolicyCombiningAlgorithm;
import org.xacml4j.v30.policy.combine.FirstApplicableRuleCombiningAlgorithm;
import org.xacml4j.v30.policy.combine.LeagacyOrderedPermitOverridesPolicyCombineAlgorithm;
import org.xacml4j.v30.policy.combine.LegacyDenyOverridesPolicyCombineAlgorithm;
import org.xacml4j.v30.policy.combine.LegacyDenyOverridesRuleCombineAlgorithm;
import org.xacml4j.v30.policy.combine.LegacyOrderedDenyOverridesPolicyCombineAlgorithm;
import org.xacml4j.v30.policy.combine.LegacyOrderedDenyOverridesRuleCombineAlgorihm;
import org.xacml4j.v30.policy.combine.LegacyOrderedPermitOverridesRuleCombineAlgorithm;
import org.xacml4j.v30.policy.combine.LegacyPermitOverridesPolicyCombineAlgorithm;
import org.xacml4j.v30.policy.combine.LegacyPermitOverridesRuleCombineAlgorithm;
import org.xacml4j.v30.policy.combine.OnlyOneApplicablePolicyCombingingAlgorithm;
import org.xacml4j.v30.policy.combine.PermitOverridesPolicyCombineAlgorithm;
import org.xacml4j.v30.policy.combine.PermitOverridesPolicyOrderedCombineAlgorithm;
import org.xacml4j.v30.policy.combine.PermitOverridesRuleCombineAlgorithm;
import org.xacml4j.v30.policy.combine.PermitOverridesRuleOrderedCombingingAlgorithm;
import org.xacml4j.v30.policy.combine.PermitUnlessDenyPolicyCombingingAlgorithm;
import org.xacml4j.v30.policy.combine.PermitUnlessDenyRuleCombiningAlgorithm;

class DefaultXacml30DecisionCombiningAlgorithms 
	extends DecisionCombingingAlgorithmProviderImpl 
{
	public DefaultXacml30DecisionCombiningAlgorithms() 
	{
		super();
		addRuleCombineAlgorithm(new FirstApplicableRuleCombiningAlgorithm());
		
		addRuleCombineAlgorithm(new PermitOverridesRuleCombineAlgorithm());
		addRuleCombineAlgorithm(new PermitOverridesRuleOrderedCombingingAlgorithm());
		
		addRuleCombineAlgorithm(new DenyOverridesRuleCombiningAlgorithm());
		addRuleCombineAlgorithm(new DenyOverridesRuleOrderedCombingingAlgorithm());
		
		addRuleCombineAlgorithm(new PermitUnlessDenyRuleCombiningAlgorithm());
		addRuleCombineAlgorithm(new DenyUnlessPermitRuleCombingingAlgorithm());
		
		addCompositeRuleCombineAlgorithm(new DenyOverridesPolicyCombiningAlgorithm());
		addCompositeRuleCombineAlgorithm(new DenyOverridesPolicyOrderedCombiningAlgorithm());
			
		addCompositeRuleCombineAlgorithm(new PermitOverridesPolicyCombineAlgorithm());
		addCompositeRuleCombineAlgorithm(new PermitOverridesPolicyOrderedCombineAlgorithm());
	
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
