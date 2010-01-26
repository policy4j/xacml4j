package com.artagon.xacml.policy.combine;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.DecisionResult;
import com.artagon.xacml.policy.Decision;
import com.artagon.xacml.policy.EvaluationContext;

class DenyOverrides <D extends Decision> extends BaseDecisionCombiningAlgorithm<D>
{
	private final static Logger log = LoggerFactory.getLogger(DenyOverrides.class);
	
	protected DenyOverrides(String id){
		super(id);
	}
	
	@Override
	public DecisionResult combine(List<D> decisions,
			EvaluationContext context) 
	{
		boolean atLeastOneIndeterminateD = false;
		boolean atLeastOneIndeterminateP = false;
		boolean atLeastOneIndeterminateDP = false;
		boolean atLeastOnePermit = false;
		for(D d : decisions)
		{
			DecisionResult decision = evaluateIfApplicable(context, d);
			log.debug("Evaluating decicion=\"{}\", evaluation result=\"{}\"", d.getId(), decision);
			if(decision == DecisionResult.DENY){
				log.debug("Not evauating decisions further, result is=\"{}\"", decision);
				return DecisionResult.DENY;
			}
			if(decision == DecisionResult.PERMIT){
				atLeastOnePermit = true;
				continue;
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
				(atLeastOneIndeterminateP || atLeastOnePermit)){
			return DecisionResult.INDETERMINATE_DP;
		}
		if(atLeastOnePermit){
			return DecisionResult.PERMIT;
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
