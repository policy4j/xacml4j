package com.artagon.xacml.v3.policy.combine;

import java.util.List;

import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.policy.DecisionRule;
import com.artagon.xacml.v3.policy.spi.combine.BaseDecisionCombiningAlgorithm;

class PermitUnlessDeny <DecisionType extends DecisionRule>  extends BaseDecisionCombiningAlgorithm<DecisionType>
{
	protected PermitUnlessDeny(String id){
		super(id);
	}
	
	@Override
	public Decision combine(List<DecisionType> decisions,
			EvaluationContext context) 
	{
		for(DecisionType d : decisions){
			Decision decision = evaluate(context, d);
			if(decision == Decision.DENY){
				return decision;
			}
		}
		return Decision.PERMIT;
	}
}
