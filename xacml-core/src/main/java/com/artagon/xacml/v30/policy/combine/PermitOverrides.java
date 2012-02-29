package com.artagon.xacml.v30.policy.combine;


import static com.artagon.xacml.v30.spi.combine.DecisionCombingingAlgorithms.evaluateIfMatch;

import java.util.List;

import com.artagon.xacml.v30.pdp.Decision;
import com.artagon.xacml.v30.pdp.DecisionRule;
import com.artagon.xacml.v30.pdp.EvaluationContext;
import com.artagon.xacml.v30.spi.combine.BaseDecisionCombiningAlgorithm;
import com.artagon.xacml.v30.spi.combine.XacmlPolicyDecisionCombingingAlgorithm;
import com.artagon.xacml.v30.spi.combine.XacmlRuleDecisionCombingingAlgorithm;

public class PermitOverrides <D extends DecisionRule> extends BaseDecisionCombiningAlgorithm<D>
{
	protected PermitOverrides(String id){
		super(id);
	}
	
	@XacmlPolicyDecisionCombingingAlgorithm("urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-overrides")
	@XacmlRuleDecisionCombingingAlgorithm("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides")
	@Override
	public Decision combine(EvaluationContext context, List<D> decisions) 
	{
		boolean atLeastOneIndeterminate = false;
		boolean atLeastOneIndeterminateD = false;
		boolean atLeastOneIndeterminateP = false;
		boolean atLeastOneIndeterminateDP = false;
		boolean atLeastOneDeny = false;
		for(D d : decisions)
		{
			Decision decision = evaluateIfMatch(context, d);
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
			if(decision == Decision.INDETERMINATE){
				atLeastOneIndeterminate  = true;
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
		if(atLeastOneIndeterminate){
			return Decision.INDETERMINATE;
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
