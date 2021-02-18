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
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.spi.combine.BaseDecisionCombiningAlgorithm;
import org.xacml4j.v30.spi.combine.XacmlPolicyDecisionCombiningAlgorithm;
import org.xacml4j.v30.spi.combine.XacmlRuleDecisionCombiningAlgorithm;

import java.util.List;

import static org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithms.evaluateIfMatch;


public class DenyUnlessPermit<D extends DecisionRule> extends BaseDecisionCombiningAlgorithm<D>
{
	protected DenyUnlessPermit(String algorithmId){
		super(algorithmId);
	}

	@Override
	public final Decision combine(EvaluationContext context, List<D> decisions)
	{
		return doCombine(context, decisions);
	}

	@XacmlPolicyDecisionCombiningAlgorithm("urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-unless-permit")
	@XacmlRuleDecisionCombiningAlgorithm("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit")
	public static <D extends DecisionRule> Decision doCombine(EvaluationContext context, List<D> decisions)
	{
		for(D d : decisions){
			Decision decision = evaluateIfMatch(context, d);
			if(decision == Decision.PERMIT){
				return decision;
			}
		}
		return Decision.DENY;
	}

}
