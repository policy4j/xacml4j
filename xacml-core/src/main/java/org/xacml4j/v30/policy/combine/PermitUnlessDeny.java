package org.xacml4j.v30.policy.combine;

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

import static org.xacml4j.v30.spi.combine.DecisionCombingingAlgorithms.evaluateIfMatch;

import java.util.List;

import org.xacml4j.v30.Decision;
import org.xacml4j.v30.DecisionRule;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.spi.combine.BaseDecisionCombiningAlgorithm;
import org.xacml4j.v30.spi.combine.XacmlPolicyDecisionCombingingAlgorithm;
import org.xacml4j.v30.spi.combine.XacmlRuleDecisionCombingingAlgorithm;


public class PermitUnlessDeny <DecisionType extends DecisionRule>
	extends BaseDecisionCombiningAlgorithm<DecisionType>
{
	protected PermitUnlessDeny(String id){
		super(id);
	}

	@XacmlPolicyDecisionCombingingAlgorithm("urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-unless-deny")
	@XacmlRuleDecisionCombingingAlgorithm("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-unless-deny")
	@Override
	public Decision combine(EvaluationContext context,
			List<DecisionType> decisions)
	{
		for(DecisionType d : decisions){
			Decision decision = evaluateIfMatch(context, d);
			if(decision == Decision.DENY){
				return decision;
			}
		}
		return Decision.PERMIT;
	}
}
