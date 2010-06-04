package com.artagon.xacml.v3.policy.combine;

import java.util.List;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.context.Decision;
import com.artagon.xacml.v3.policy.DecisionRule;
import com.artagon.xacml.v3.policy.spi.combine.BaseDecisionCombiningAlgorithm;

/**
 * The deny overrides combining algorithm is intended for those cases where a deny 
 * decision should have priority over a permit decision.<p>
 * This algorithm has the following behavior:<p>
 * <ol>
 * <li>If any decision is "Deny", the result is "Deny"</li>
 * <li>Otherwise, if any decision is "Indeterminate{DP}", the result is "Indeterminate{DP}"</li>
 * <li>Otherwise, if any decision is "Indeterminate{D}" and another decision is 
 * ÒIndeterminate{P} or Permit, the result is "Indeterminate{DP}"</li>
 * <li>Otherwise, if any decision is "Indeterminate{D}", the result is "Indeterminate{D}"</li>
 * <li>Otherwise, if any decision is "Permit", the result is "Permit"</li>
 * <li>Otherwise, if any decision is "Indeterminate{P}", the result is "Indeterminate{P}"<li>
 * <li>Otherwise, the result is "NotApplicable"<li>
 * <ol>
 * 
 * @author Giedrius Trumpickas
 *
 * @param <D> a {@link DecisionRule} implementation type
 */
class DenyOverrides <D extends DecisionRule> extends BaseDecisionCombiningAlgorithm<D>
{
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
			if(decision == Decision.DENY){
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
