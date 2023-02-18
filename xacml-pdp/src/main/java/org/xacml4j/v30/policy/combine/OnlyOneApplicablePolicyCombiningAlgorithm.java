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

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.spi.combine.BaseDecisionCombiningAlgorithm;


public final class OnlyOneApplicablePolicyCombiningAlgorithm extends
	BaseDecisionCombiningAlgorithm<CompositeDecisionRule>
{
	public final static String ID = "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:only-one-applicable";

	public OnlyOneApplicablePolicyCombiningAlgorithm() {
		super(ID);
	}

	@XacmlPolicyDecisionCombiningAlgorithm("urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:only-one-applicable")
	@Override
	public Decision combine(EvaluationContext context, List<CompositeDecisionRule> decisions)
	{
		boolean atLeastOne = false;
		CompositeDecisionRule found = null;
		EvaluationContext foundEvalContext = null;
		for(CompositeDecisionRule d : decisions)
		{
			final EvaluationContext policyContext = d.createContext(context);
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
