package com.artagon.xacml.v30.policy.combine;

import java.util.List;

import static com.artagon.xacml.v30.spi.combine.DecisionCombingingAlgorithms.evaluateIfMatch;

import com.artagon.xacml.v30.StatusCode;
import com.artagon.xacml.v30.pdp.CompositeDecisionRule;
import com.artagon.xacml.v30.pdp.Decision;
import com.artagon.xacml.v30.pdp.EvaluationContext;
import com.artagon.xacml.v30.spi.combine.BaseDecisionCombiningAlgorithm;
import com.google.common.base.Preconditions;

/**
 * XACML 3.0 does not allow Condition elements at the policy or policy set levels. 
 * In some cases it may be useful to have a Condition at the policy or policy set level since a 
 * Condition allows for more expressive matching than a Target, which can only match against constant values.
 * For instance, someone may want to write a policy which applies to the cases where the subject is 
 * the owner of the resource. In this case the policy should apply if the subject-id of the request 
 * equals the owner attribute of the resource in the request. This matching cannot be done with 
 * a <Target> since it is not a match expression against a constant value. 
 * Such a policy would require a Condition at the Policy level
 * 
 * @author Giedrius Trumpickas
 */
public class OnPermitApplySecondPolicyCombiningAlgorithm extends 
	BaseDecisionCombiningAlgorithm<CompositeDecisionRule> 
{
	private final static String ID = "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:on-permit-apply-second";
	
	public OnPermitApplySecondPolicyCombiningAlgorithm() {
		super(ID);
	}

	@Override
	public Decision combine(EvaluationContext context,
			List<CompositeDecisionRule> policies) {
		if(policies.size() != 2){
			context.setEvaluationStatus(StatusCode.createProcessingError());
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
			context.setEvaluationStatus(StatusCode.createProcessingError());
			return Decision.INDETERMINATE_P;
		}
		if(d1 == Decision.DENY){
			context.setEvaluationStatus(StatusCode.createProcessingError());
			return Decision.INDETERMINATE_D;
		}
		if(d1 == Decision.INDETERMINATE_P){
			context.setEvaluationStatus(StatusCode.createProcessingError());
			return Decision.INDETERMINATE_P;
		}
		if(d1 == Decision.INDETERMINATE_D){
			context.setEvaluationStatus(StatusCode.createProcessingError());
			return Decision.INDETERMINATE_D;
		}
		if(d1 == Decision.INDETERMINATE_DP){
			context.setEvaluationStatus(StatusCode.createProcessingError());
			return Decision.INDETERMINATE_DP;
		}
		return Decision.NOT_APPLICABLE;
	}
	
}
