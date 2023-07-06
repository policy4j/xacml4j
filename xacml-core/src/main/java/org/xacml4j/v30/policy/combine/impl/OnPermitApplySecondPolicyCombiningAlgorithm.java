package org.xacml4j.v30.policy.combine.impl;

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

import static org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithms.evaluateIfMatch;

import java.util.List;

import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.policy.combine.BaseDecisionCombiningAlgorithm;


/**
 * XACML 3.0 does not allow Condition elements at the policy or policy set levels.
 * In some cases it may be useful to have a Condition at the policy or policy set level since a
 * Condition allows for more expressive matching than a Target, which can only match against constant values.
 * For defaultProvider, someone may want to write a policy which applies to the cases where the subject is
 * the owner of the resource. In this case the policy should apply if the subject-id of the request
 * equals the owner attribute of the resource in the request. This matching cannot be done with
 * a &lt;Target&gt; since it is not a match expression against a constant value.
 * Such a policy would require a Condition at the Policy level
 *
 * @author Giedrius Trumpickas
 */
public class OnPermitApplySecondPolicyCombiningAlgorithm extends
                                                         BaseDecisionCombiningAlgorithm<CompositeDecisionRule>
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:on-permit-apply-second";

	private final static Status PROCESSING_ERROR = Status.processingError().build();

	public OnPermitApplySecondPolicyCombiningAlgorithm() {
		super(ID);
	}

	@Override
	public Decision combine(EvaluationContext context,
			List<CompositeDecisionRule> policies) {
		if(policies.size() != 2){
			context.setEvaluationStatus(PROCESSING_ERROR);
			return Decision.INDETERMINATE_DP;
		}
		Decision d0 =  evaluateIfMatch(context, policies.get(0));
		if(d0 == Decision.NOT_APPLICABLE){
			return Decision.NOT_APPLICABLE;
		}
		Decision d1 = evaluateIfMatch(context, policies.get(1));
		if(d0 == Decision.PERMIT){
			return d1;
		}
		if(d1 == Decision.PERMIT){
			context.setEvaluationStatus(PROCESSING_ERROR);
			return Decision.INDETERMINATE_P;
		}
		if(d1 == Decision.DENY){
			context.setEvaluationStatus(PROCESSING_ERROR);
			return Decision.INDETERMINATE_D;
		}
		if(d1 == Decision.INDETERMINATE_P){
			context.setEvaluationStatus(PROCESSING_ERROR);
			return Decision.INDETERMINATE_P;
		}
		if(d1 == Decision.INDETERMINATE_D){
			context.setEvaluationStatus(PROCESSING_ERROR);
			return Decision.INDETERMINATE_D;
		}
		if(d1 == Decision.INDETERMINATE_DP){
			context.setEvaluationStatus(PROCESSING_ERROR);
			return Decision.INDETERMINATE_DP;
		}
		return Decision.NOT_APPLICABLE;
	}

}
