package com.artagon.xacml.v3.policy.combine;

import java.util.List;

import com.artagon.xacml.v3.DecisionResult;
import com.artagon.xacml.v3.policy.DecisionRule;
import com.artagon.xacml.v3.policy.EvaluationContext;

class PermitUnlessDeny <DecisionType extends DecisionRule>  extends BaseDecisionCombiningAlgorithm<DecisionType>
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
