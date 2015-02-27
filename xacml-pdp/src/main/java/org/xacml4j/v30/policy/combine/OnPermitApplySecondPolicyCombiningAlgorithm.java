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
import org.xacml4j.v30.Status;
import org.xacml4j.v30.pdp.DecisionRuleEvaluationContext;
import org.xacml4j.v30.spi.combine.BaseDecisionCombiningAlgorithm;
import org.xacml4j.v30.spi.combine.XacmlPolicyDecisionCombiningAlgorithm;

import java.util.List;

import static org.xacml4j.v30.spi.combine.DecisionCombiningAlgorithms.evaluateIfMatch;


/**
 * XACML 3.0 does not allow Condition elements at the policy or policy set levels.
 * In some cases it may be useful to have a Condition at the policy or policy set level since a
 * Condition allows for more expressive matching than a Target, which can only match against constant values.
 * For instance, someone may want to write a policy which applies to the cases where the subject is
 * the owner of the resource. In this case the policy should apply if the subject-attributeId of the request
 * equals the owner category of the resource in the request. This matching cannot be done with
 * a <Target> since it is not a match expression against a constant value.
 * Such a policy would require a Condition at the Policy level
 *
 * @author Giedrius Trumpickas
 */
public class OnPermitApplySecondPolicyCombiningAlgorithm extends
	BaseDecisionCombiningAlgorithm
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:on-permit-apply-second";

	private final static Status PROCESSING_ERROR = Status.processingError().build();

	public OnPermitApplySecondPolicyCombiningAlgorithm() {
		super(ID);
	}

    @Override
    public Decision combine(DecisionRuleEvaluationContext context,
                            List<? extends DecisionRule> policies) {
        return doCombine(context, policies);
    }

    @XacmlPolicyDecisionCombiningAlgorithm(ID)
	public static Decision doCombine(DecisionRuleEvaluationContext context,
			List<? extends DecisionRule> policies) {
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
