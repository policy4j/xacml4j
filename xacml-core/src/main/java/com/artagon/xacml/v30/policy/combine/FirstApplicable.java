package com.artagon.xacml.v30.policy.combine;

import java.util.List;

import com.artagon.xacml.v30.Decision;
import com.artagon.xacml.v30.DecisionRule;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.spi.combine.BaseDecisionCombiningAlgorithm;

class FirstApplicable<D extends DecisionRule> extends BaseDecisionCombiningAlgorithm<D>
{
	protected FirstApplicable(String algorithmId) {
		super(algorithmId);
	}

	@Override
	public final Decision combine(List<D> decisions,
			EvaluationContext context) 
	{
		for(D d : decisions){
			Decision decision = evaluateIfApplicable(context, d);
			if(decision == Decision.DENY){
				return decision;
			}
			if(decision == Decision.PERMIT){
				return decision;
			}
			if(decision == Decision.NOT_APPLICABLE){
				continue;
			}
			if(decision.isIndeterminate()){
				return decision;
			}
		}
		return Decision.NOT_APPLICABLE;
	}
}
