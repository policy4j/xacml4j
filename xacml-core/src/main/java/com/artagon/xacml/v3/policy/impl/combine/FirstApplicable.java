package com.artagon.xacml.v3.policy.impl.combine;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.DecisionRule;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.policy.spi.combine.BaseDecisionCombiningAlgorithm;

class FirstApplicable<D extends DecisionRule> extends BaseDecisionCombiningAlgorithm<D>
{
	private final static Logger log = LoggerFactory.getLogger(FirstApplicable.class);
	
	protected FirstApplicable(String algorithmId) {
		super(algorithmId);
	}

	@Override
	public Decision combine(List<D> decisions,
			EvaluationContext context) 
	{
		log.debug("Combining decisions via algorithm={}", getId());
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
