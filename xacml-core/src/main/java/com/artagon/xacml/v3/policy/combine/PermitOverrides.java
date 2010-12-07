package com.artagon.xacml.v3.policy.combine;


import java.util.List;

import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.DecisionRule;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.spi.combine.BaseDecisionCombiningAlgorithm;

class PermitOverrides <D extends DecisionRule> extends BaseDecisionCombiningAlgorithm<D>
{
	protected PermitOverrides(String id){
		super(id);
	}
	
	@Override
	public Decision combine(List<D> decisions,
			EvaluationContext context) 
	{
		boolean atLeastOneIndeterminateD = false;
		boolean atLeastOneIndeterminateP = false;
		boolean atLeastOneIndeterminateDP = false;
		boolean atLeastOneDeny = false;
		for(D d : decisions)
		{
			Decision decision = evaluateIfApplicable(context, d);
			if(decision == Decision.DENY){
				atLeastOneDeny = true;
				continue;
			}
			if(decision == Decision.PERMIT){
				return Decision.PERMIT;
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
			if(decision == Decision.INDETERMINATE_DP){
				atLeastOneIndeterminateDP = true;
				continue;
			}
			
		}
		if(atLeastOneIndeterminateDP){
			return Decision.INDETERMINATE_DP;
		}
		if(atLeastOneIndeterminateD && 
				(atLeastOneIndeterminateP || atLeastOneDeny)){
			return Decision.INDETERMINATE_DP;
		}
		if(atLeastOneDeny){
			return Decision.DENY;
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
