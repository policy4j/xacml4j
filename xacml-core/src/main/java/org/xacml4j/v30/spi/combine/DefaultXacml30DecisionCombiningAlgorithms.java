package org.xacml4j.v30.spi.combine;

/*
 * #%L
 * Artagon XACML 3.0 Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Artagon
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.xacml4j.v30.policy.combine.DenyOverridesPolicyCombiningAlgorithm;
import org.xacml4j.v30.policy.combine.DenyOverridesPolicyOrderedCombiningAlgorithm;
import org.xacml4j.v30.policy.combine.DenyOverridesRuleCombiningAlgorithm;
import org.xacml4j.v30.policy.combine.DenyOverridesRuleOrderedCombiningAlgorithm;
import org.xacml4j.v30.policy.combine.DenyUnlessPermitPolicyCombiningAlgorithm;
import org.xacml4j.v30.policy.combine.DenyUnlessPermitRuleCombiningAlgorithm;
import org.xacml4j.v30.policy.combine.FirstApplicablePolicyCombiningAlgorithm;
import org.xacml4j.v30.policy.combine.FirstApplicableRuleCombiningAlgorithm;
import org.xacml4j.v30.policy.combine.LegacyOrderedDenyOverridesRuleCombineAlgorithm;
import org.xacml4j.v30.policy.combine.LegacyOrderedPermitOverridesPolicyCombineAlgorithm;
import org.xacml4j.v30.policy.combine.LegacyDenyOverridesPolicyCombineAlgorithm;
import org.xacml4j.v30.policy.combine.LegacyDenyOverridesRuleCombineAlgorithm;
import org.xacml4j.v30.policy.combine.LegacyOrderedDenyOverridesPolicyCombineAlgorithm;
import org.xacml4j.v30.policy.combine.LegacyOrderedPermitOverridesRuleCombineAlgorithm;
import org.xacml4j.v30.policy.combine.LegacyPermitOverridesPolicyCombineAlgorithm;
import org.xacml4j.v30.policy.combine.LegacyPermitOverridesRuleCombineAlgorithm;
import org.xacml4j.v30.policy.combine.OnlyOneApplicablePolicyCombiningAlgorithm;
import org.xacml4j.v30.policy.combine.PermitOverridesPolicyCombineAlgorithm;
import org.xacml4j.v30.policy.combine.PermitOverridesPolicyOrderedCombineAlgorithm;
import org.xacml4j.v30.policy.combine.PermitOverridesRuleCombineAlgorithm;
import org.xacml4j.v30.policy.combine.PermitOverridesRuleOrderedCombiningAlgorithm;
import org.xacml4j.v30.policy.combine.PermitUnlessDenyPolicyCombiningAlgorithm;
import org.xacml4j.v30.policy.combine.PermitUnlessDenyRuleCombiningAlgorithm;

class DefaultXacml30DecisionCombiningAlgorithms
	extends DecisionCombiningAlgorithmProviderImpl
{
	public DefaultXacml30DecisionCombiningAlgorithms()
	{
		super();
		addRuleCombineAlgorithm(new FirstApplicableRuleCombiningAlgorithm());

		addRuleCombineAlgorithm(new PermitOverridesRuleCombineAlgorithm());
		addRuleCombineAlgorithm(new PermitOverridesRuleOrderedCombiningAlgorithm());

		addRuleCombineAlgorithm(new DenyOverridesRuleCombiningAlgorithm());
		addRuleCombineAlgorithm(new DenyOverridesRuleOrderedCombiningAlgorithm());

		addRuleCombineAlgorithm(new PermitUnlessDenyRuleCombiningAlgorithm());
		addRuleCombineAlgorithm(new DenyUnlessPermitRuleCombiningAlgorithm());

		addCompositeRuleCombineAlgorithm(new DenyOverridesPolicyCombiningAlgorithm());
		addCompositeRuleCombineAlgorithm(new DenyOverridesPolicyOrderedCombiningAlgorithm());

		addCompositeRuleCombineAlgorithm(new PermitOverridesPolicyCombineAlgorithm());
		addCompositeRuleCombineAlgorithm(new PermitOverridesPolicyOrderedCombineAlgorithm());

		addCompositeRuleCombineAlgorithm(new FirstApplicablePolicyCombiningAlgorithm());
		addCompositeRuleCombineAlgorithm(new OnlyOneApplicablePolicyCombiningAlgorithm());
		addCompositeRuleCombineAlgorithm(new PermitUnlessDenyPolicyCombiningAlgorithm());
		addCompositeRuleCombineAlgorithm(new DenyUnlessPermitPolicyCombiningAlgorithm());

		// Legacy algorithms

		// rule combining algorithms
		addRuleCombineAlgorithm(new LegacyDenyOverridesRuleCombineAlgorithm());
		addRuleCombineAlgorithm(new LegacyOrderedDenyOverridesRuleCombineAlgorithm());
		addRuleCombineAlgorithm(new LegacyPermitOverridesRuleCombineAlgorithm());
		addRuleCombineAlgorithm(new LegacyOrderedPermitOverridesRuleCombineAlgorithm());

		// policy combining algorithms
		addCompositeRuleCombineAlgorithm(new LegacyDenyOverridesPolicyCombineAlgorithm());
		addCompositeRuleCombineAlgorithm(new LegacyOrderedDenyOverridesPolicyCombineAlgorithm());
		addCompositeRuleCombineAlgorithm(new LegacyPermitOverridesPolicyCombineAlgorithm());
		addCompositeRuleCombineAlgorithm(new LegacyOrderedPermitOverridesPolicyCombineAlgorithm());
	}
}
