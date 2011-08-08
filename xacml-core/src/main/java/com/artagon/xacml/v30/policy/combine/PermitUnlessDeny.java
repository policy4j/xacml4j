package com.artagon.xacml.v30.policy.combine;

import static com.artagon.xacml.v30.spi.combine.DecisionCombingingAlgorithms.evaluateIfApplicable;

import java.util.List;

import com.artagon.xacml.v30.Decision;
import com.artagon.xacml.v30.DecisionRule;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.spi.combine.BaseDecisionCombiningAlgorithm;
import com.artagon.xacml.v30.spi.combine.XacmlPolicyDecisionCombingingAlgorithm;
import com.artagon.xacml.v30.spi.combine.XacmlRuleDecisionCombingingAlgorithm;

public class PermitUnlessDeny <DecisionType extends DecisionRule>  
	extends BaseDecisionCombiningAlgorithm<DecisionType>
{
	protected PermitUnlessDeny(String id){
		super(id);
	}
	
	@XacmlPolicyDecisionCombingingAlgorithm("urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-unless-deny")
	@XacmlRuleDecisionCombingingAlgorithm("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-unless-deny")
	@Override
	public Decision combine(EvaluationContext context, 
			List<DecisionType> decisions) 
	{
		for(DecisionType d : decisions){
			Decision decision = evaluateIfApplicable(context, d);
			if(decision == Decision.DENY){
				return decision;
			}
		}
		return Decision.PERMIT;
	}
}
