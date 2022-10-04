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

import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.spi.combine.BaseDecisionCombiningAlgorithm;
import org.xacml4j.v30.spi.combine.XacmlPolicyDecisionCombiningAlgorithm;
import org.xacml4j.v30.spi.combine.XacmlRuleDecisionCombiningAlgorithm;


/**
 * The deny overrides combining algorithm is intended for those cases where a deny
 * decision should have priority over a permit decision.<p>
 * This algorithm has the following behavior:<p>
 * <ol>
 * <li>If any decision is "Deny", the result is "Deny"</li>
 * <li>Otherwise, if any decision is "Indeterminate{DP}", the result is "Indeterminate{DP}"</li>
 * <li>Otherwise, if any decision is "Indeterminate{D}" and another decision is
 * "Indeterminate{P} or Permit, the result is "Indeterminate{DP}"</li>
 * <li>Otherwise, if any decision is "Indeterminate{D}", the result is "Indeterminate{D}"</li>
 * <li>Otherwise, if any decision is "Permit", the result is "Permit"</li>
 * <li>Otherwise, if any decision is "Indeterminate{P}", the result is "Indeterminate{P}"<li>
 * <li>Otherwise, the result is "NotApplicable"<li>
 * <ol>
 *
 * @author Giedrius Trumpickas
 *
 * @param <D> a {@link DecisionRule} implementation type
 */
public class DenyOverrides <D extends DecisionRule> extends BaseDecisionCombiningAlgorithm<D>
{
	protected DenyOverrides(String id){
		super(id);
	}

	@Override
	public final Decision combine(EvaluationContext context, List<D> decisions){
		return doCombine(context, decisions);
	}

	@XacmlPolicyDecisionCombiningAlgorithm("urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-overrides")
	@XacmlRuleDecisionCombiningAlgorithm("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides")
	public static <D extends DecisionRule>  Decision doCombine(EvaluationContext context, List<D> decisions)
	{
		boolean atLeastOneIndeterminateD = false;
		boolean atLeastOneIndeterminateP = false;
		boolean atLeastOneIndeterminateDP = false;
		boolean atLeastOnePermit = false;
		for(D d : decisions)
		{
			Decision decision = d.evaluate(d.createContext(context));
			if(decision == Decision.DENY){
				return Decision.DENY;
			}
			if(decision == Decision.PERMIT){
				atLeastOnePermit = true;
				continue;
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
					decision == Decision.INDETERMINATE){
				atLeastOneIndeterminateDP = true;
				continue;
			}
		}
		if(atLeastOneIndeterminateDP){
			return Decision.INDETERMINATE_DP;
		}
		if(atLeastOneIndeterminateD &&
				(atLeastOneIndeterminateP || atLeastOnePermit)){
			return Decision.INDETERMINATE_DP;
		}
		if(atLeastOnePermit){
			return Decision.PERMIT;
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
