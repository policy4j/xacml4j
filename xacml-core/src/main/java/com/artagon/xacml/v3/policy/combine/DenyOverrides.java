package com.artagon.xacml.v3.policy.combine;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.policy.DecisionRule;
import com.artagon.xacml.v3.policy.EvaluationContext;

class DenyOverrides <D extends DecisionRule> extends BaseDecisionCombiningAlgorithm<D>
{
	private final static Logger log = LoggerFactory.getLogger(DenyOverrides.class);
	
	protected DenyOverrides(String id){
		super(id);
	}
	
	@Override
	public Decision combine(List<D> decisions,
			EvaluationContext context) 
	{
		boolean atLeastOneIndeterminateD = false;
		boolean atLeastOneIndeterminateP = false;
		boolean atLeastOneIndeterminateDP = false;
		boolean atLeastOnePermit = false;
		for(D d : decisions)
		{
			Decision decision = evaluateIfApplicable(context, d);
			log.debug("Evaluating decicion=\"{}\", evaluation result=\"{}\"", d.getId(), decision);
			if(decision == Decision.DENY){
				log.debug("Not evauating decisions further, result is=\"{}\"", decision);
				return Decision.DENY;
			}
			if(decision == Decision.PERMIT){
				atLeastOnePermit = true;
				continue;
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
				(atLeastOneIndeterminateP || atLeastOnePermit)){
			return Decision.INDETERMINATE_DP;
		}
		if(atLeastOnePermit){
			return Decision.PERMIT;
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
