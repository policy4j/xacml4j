package com.artagon.xacml.v3.policy.combine;


import java.util.List;

import com.artagon.xacml.v3.DecisionResult;
import com.artagon.xacml.v3.policy.DecisionRule;
import com.artagon.xacml.v3.policy.EvaluationContext;

class PermitOverrides <D extends DecisionRule> extends BaseDecisionCombiningAlgorithm<D>
{
	protected PermitOverrides(String id){
		super(id);
	}
	
	@Override
	public DecisionResult combine(List<D> decisions,
			EvaluationContext context) 
	{
		boolean atLeastOneIndeterminateD = false;
		boolean atLeastOneIndeterminateP = false;
		boolean atLeastOneIndeterminateDP = false;
		boolean atLeastOneDeny = false;
		for(D d : decisions)
		{
			DecisionResult decision = evaluateIfApplicable(context, d);
			if(decision == DecisionResult.DENY){
				atLeastOneDeny = true;
				continue;
			}
			if(decision == DecisionResult.PERMIT){
				return DecisionResult.PERMIT;
			}
			if(decision == DecisionResult.NOT_APPLICABLE){
				continue;
			}
			if(decision == DecisionResult.INDETERMINATE_D){
				atLeastOneIndeterminateD = true;
				continue;
			}
			if(decision == DecisionResult.INDETERMINATE_P){
				atLeastOneIndeterminateP = true;
				continue;
			}
			if(decision == DecisionResult.INDETERMINATE_DP){
				atLeastOneIndeterminateDP = true;
				continue;
			}
			
		}
		if(atLeastOneIndeterminateDP){
			return DecisionResult.INDETERMINATE_DP;
		}
		if(atLeastOneIndeterminateD && 
				(atLeastOneIndeterminateP || atLeastOneDeny)){
			return DecisionResult.INDETERMINATE_DP;
		}
		if(atLeastOneDeny){
			return DecisionResult.DENY;
		}
		if(atLeastOneIndeterminateP){
			return DecisionResult.INDETERMINATE_P;
		}
		if(atLeastOneIndeterminateD){
			return DecisionResult.INDETERMINATE_D;
		}
		return DecisionResult.NOT_APPLICABLE;
	}
}
