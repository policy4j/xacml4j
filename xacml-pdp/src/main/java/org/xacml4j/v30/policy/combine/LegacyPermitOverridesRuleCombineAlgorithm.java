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

import static org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithms.evaluateIfMatch;

import java.util.List;

import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.policy.Rule;
import org.xacml4j.v30.spi.combine.BaseDecisionCombiningAlgorithm;
import org.xacml4j.v30.spi.combine.XacmlRuleDecisionCombiningAlgorithm;


public class LegacyPermitOverridesRuleCombineAlgorithm extends BaseDecisionCombiningAlgorithm<Rule>
{
	private final static String ID ="urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:permit-overrides";


	protected LegacyPermitOverridesRuleCombineAlgorithm(String algorithmId) {
		super(algorithmId);
	}

	public LegacyPermitOverridesRuleCombineAlgorithm() {
		super(ID);
	}


	@XacmlRuleDecisionCombiningAlgorithm("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:permit-overrides")
	@Override
	public Decision combine(EvaluationContext context, List<Rule> rules)
	{
		boolean atLeastOneError = false;
		boolean potentialPermit = false;
		boolean atLeastOneDeny = false;
		for(Rule r : rules)
		{
			Decision d = evaluateIfMatch(context, r);
			if(d == Decision.DENY){
				atLeastOneDeny = true;
				continue;
			}
			if(d == Decision.PERMIT){
				return Decision.PERMIT;
			}
			if(d == Decision.NOT_APPLICABLE){
				continue;
			}
			if(d.isIndeterminate()){
				atLeastOneError = true;
				if(r.getEffect() == Effect.PERMIT){
					potentialPermit = true;
				}
			}
		}
		if(potentialPermit){
			return Decision.INDETERMINATE;
		}
		if(atLeastOneDeny){
			return Decision.DENY;
		}
		if(atLeastOneError){
			return Decision.INDETERMINATE;
		}
		return Decision.NOT_APPLICABLE;
	}
}
