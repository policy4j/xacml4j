package com.artagon.xacml.v30.policy.combine;

import java.util.List;

import com.artagon.xacml.v30.DecisionResult;
import com.artagon.xacml.v30.policy.Decision;
import com.artagon.xacml.v30.policy.EvaluationContext;

class PermitUnlessDeny <DecisionType extends Decision>  extends BaseDecisionCombiningAlgorithm<DecisionType>
{
	protected PermitUnlessDeny(String id){
		super(id);
	}
	
	@Override
	public DecisionResult combine(List<DecisionType> decisions,
			EvaluationContext context) 
	{
		for(DecisionType d : decisions){
			DecisionResult decision = d.evaluate(context);
			if(decision == DecisionResult.DENY){
				return decision;
			}
		}
		return DecisionResult.PERMIT;
	}
}
