package com.artagon.xacml.v30.policy.combine;

import static com.artagon.xacml.v30.spi.combine.DecisionCombingingAlgorithms.evaluateIfMatch;

import java.util.List;

import com.artagon.xacml.v30.Decision;
import com.artagon.xacml.v30.DecisionRule;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.spi.combine.BaseDecisionCombiningAlgorithm;
import com.artagon.xacml.v30.spi.combine.XacmlPolicyDecisionCombingingAlgorithm;
import com.artagon.xacml.v30.spi.combine.XacmlRuleDecisionCombingingAlgorithm;

public class FirstApplicable<D extends DecisionRule> extends BaseDecisionCombiningAlgorithm<D>
{
	protected FirstApplicable(String algorithmId) {
		super(algorithmId);
	}

	public final Decision combine(EvaluationContext context, List<D> decisions){
		return doCombine(context, decisions);
	}

	@XacmlPolicyDecisionCombingingAlgorithm("urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:first-applicable")
	@XacmlRuleDecisionCombingingAlgorithm("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable")
	public final static <D extends DecisionRule> Decision doCombine(EvaluationContext context, List<D> decisions)
	{
		for(D d : decisions){
			Decision decision = evaluateIfMatch(context, d);
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
