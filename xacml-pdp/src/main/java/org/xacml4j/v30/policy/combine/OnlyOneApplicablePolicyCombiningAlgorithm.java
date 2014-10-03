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

import org.xacml4j.v30.*;
import org.xacml4j.v30.pdp.DecisionRuleEvaluationContext;
import org.xacml4j.v30.spi.combine.BaseDecisionCombiningAlgorithm;
import org.xacml4j.v30.spi.combine.XacmlPolicyDecisionCombiningAlgorithm;


public final class OnlyOneApplicablePolicyCombiningAlgorithm extends
	BaseDecisionCombiningAlgorithm
{
	public final static String ID = "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:only-one-applicable";

	public OnlyOneApplicablePolicyCombiningAlgorithm() {
		super(ID);
	}


    public Decision combine(DecisionRuleEvaluationContext context, List<? extends DecisionRule> decisions){
        return doCombine(context, (List<? extends CompositeDecisionRule>) decisions);
    }

	@XacmlPolicyDecisionCombiningAlgorithm("urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:only-one-applicable")
	public static Decision doCombine(DecisionRuleEvaluationContext context, List<? extends CompositeDecisionRule> decisions)
	{
		boolean atLeastOne = false;
		CompositeDecisionRule found = null;
		DecisionRuleEvaluationContext foundEvalContext = null;
		for(CompositeDecisionRule d : decisions)
		{
			final DecisionRuleEvaluationContext policyContext = (DecisionRuleEvaluationContext)d.createContext(context);
			final MatchResult r = d.isMatch(policyContext);
			if(r == MatchResult.INDETERMINATE){
				return Decision.INDETERMINATE;
			}
			if(r == MatchResult.MATCH){
				if(atLeastOne){
					return Decision.INDETERMINATE;
				}
				atLeastOne = true;
				found = d;
				foundEvalContext = policyContext;
			}
		}
		return atLeastOne?found.evaluate(foundEvalContext):Decision.NOT_APPLICABLE;
	}
}
