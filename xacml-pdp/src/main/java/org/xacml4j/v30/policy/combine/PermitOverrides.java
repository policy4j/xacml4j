package org.xacml4j.v30.policy.combine;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
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


import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.pdp.DecisionRuleEvaluationContext;
import org.xacml4j.v30.spi.combine.BaseDecisionCombiningAlgorithm;
import org.xacml4j.v30.spi.combine.XacmlPolicyDecisionCombiningAlgorithm;
import org.xacml4j.v30.spi.combine.XacmlRuleDecisionCombiningAlgorithm;

import java.util.List;

import static org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithms.evaluateIfMatch;


public class PermitOverrides extends BaseDecisionCombiningAlgorithm
{
	protected PermitOverrides(String id){
		super(id);
	}

	@XacmlPolicyDecisionCombiningAlgorithm("urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-overrides")
	@XacmlRuleDecisionCombiningAlgorithm("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides")
	@Override
	public Decision combine(DecisionRuleEvaluationContext context, List<? extends DecisionRule> decisions)
	{
		boolean atLeastOneIndeterminate = false;
		boolean atLeastOneIndeterminateD = false;
		boolean atLeastOneIndeterminateP = false;
		boolean atLeastOneIndeterminateDP = false;
		boolean atLeastOneDeny = false;
		for(DecisionRule d : decisions)
		{
			Decision decision = evaluateIfMatch(context, d);
			if(decision == Decision.DENY){
				atLeastOneDeny = true;
				continue;
			}
			if(decision == Decision.PERMIT){
				return Decision.PERMIT;
			}
			if(decision == Decision.NOT_APPLICABLE){
				continue;
			}
			if(decision == Decision.INDETERMINATE_D){
				atLeastOneIndeterminateD = true;
				continue;
			}
			if(decision == Decision.INDETERMINATE_P){
				atLeastOneIndeterminateP = true;
				continue;
			}
			if(decision == Decision.INDETERMINATE_DP ||
					decision ==  Decision.INDETERMINATE){
				atLeastOneIndeterminateDP = true;
				continue;
			}

		}
		if(atLeastOneIndeterminate){
			return Decision.INDETERMINATE;
		}
		if(atLeastOneIndeterminateDP){
			return Decision.INDETERMINATE_DP;
		}
		if(atLeastOneIndeterminateD &&
				(atLeastOneIndeterminateP || atLeastOneDeny)){
			return Decision.INDETERMINATE_DP;
		}
		if(atLeastOneDeny){
			return Decision.DENY;
		}
		if(atLeastOneIndeterminateP){
			return Decision.INDETERMINATE_P;
		}
		if(atLeastOneIndeterminateD){
			return Decision.INDETERMINATE_D;
		}
		return Decision.NOT_APPLICABLE;
	}
}
