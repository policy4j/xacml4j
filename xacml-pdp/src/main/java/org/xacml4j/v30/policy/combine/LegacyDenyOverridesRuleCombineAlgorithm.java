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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.Effect;
import org.xacml4j.v30.pdp.DecisionRuleEvaluationContext;
import org.xacml4j.v30.pdp.Rule;
import org.xacml4j.v30.spi.combine.BaseDecisionCombiningAlgorithm;
import org.xacml4j.v30.spi.combine.XacmlRuleDecisionCombiningAlgorithm;


public class LegacyDenyOverridesRuleCombineAlgorithm extends BaseDecisionCombiningAlgorithm
{
	private final static Logger log = LoggerFactory.getLogger(LegacyDenyOverridesRuleCombineAlgorithm.class);

	private final static String ID = "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:deny-overrides";

	public LegacyDenyOverridesRuleCombineAlgorithm() {
		super(ID);
	}

	LegacyDenyOverridesRuleCombineAlgorithm(String algorithmId) {
		super(algorithmId);
	}

	@Override
	public Decision combine(DecisionRuleEvaluationContext context, List<? extends DecisionRule> rules){
		return doCombine(context, (List<Rule>)rules);
	}

	@XacmlRuleDecisionCombiningAlgorithm("urn:oasis:names:tc:xacml:1.1:rule-combining-algorithm:ordered-deny-overrides")
	public static Decision doCombineOrdered(DecisionRuleEvaluationContext context, List<Rule> rules){
		return doCombine(context, rules);
	}

	@XacmlRuleDecisionCombiningAlgorithm("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:deny-overrides")
	public static Decision doCombine(DecisionRuleEvaluationContext context, List<Rule> rules)
	{
		boolean potentialDeny	= false;
		boolean atLeastOnePermit = false;
		boolean atLeastOneError = false;
		for(Rule r : rules)
		{
			Decision d = r.evaluate(r.createContext(context));
			if(log.isDebugEnabled()){
				log.debug("Decision rule id=\"{}\" evaluation result=\"{}\"", r.getId(), d);
			}
			if (d == Decision.DENY){
				return Decision.DENY;
			}
			if(d == Decision.PERMIT){
				atLeastOnePermit = true;
				continue;
			}
			if (d == Decision.NOT_APPLICABLE){
				continue;
			}
			if (d.isIndeterminate()){
				atLeastOneError = true;
				if (r.getEffect() == Effect.DENY){
					potentialDeny = true;
				}
				continue;
			}
		}
		if(potentialDeny){
			return Decision.INDETERMINATE;
		}
		if(atLeastOnePermit){
			return Decision.PERMIT;
		}
		if(atLeastOneError){
			return Decision.INDETERMINATE;
		}
		return Decision.NOT_APPLICABLE;
	}
}
