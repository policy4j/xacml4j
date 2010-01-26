package com.artagon.xacml.v30.policy.combine;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v30.DecisionResult;
import com.artagon.xacml.v30.policy.Decision;
import com.artagon.xacml.v30.policy.DecisionCombiningAlgorithm;
import com.artagon.xacml.v30.policy.EvaluationContext;
import com.artagon.xacml.v30.policy.PolicyVisitor;

abstract class BaseDecisionCombiningAlgorithm <D extends Decision>  
	implements DecisionCombiningAlgorithm <D>
{
	private String algorithmId;
	
	protected BaseDecisionCombiningAlgorithm(String algorithmId){
		Preconditions.checkNotNull(algorithmId);
		this.algorithmId = algorithmId;
	}
	
	/**
	 * Gets decision algorithm identifier
	 * 
	 * @return decision algorithm identifier
	 */
	public final String getId(){
		return algorithmId;
	}

	@Override
	public final void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}	
	
	/**
	 * Evaluates decision by checking applicability first
	 * 
	 * @param context an evaluation context
	 * @param decision an decision
	 * @return
	 */
	protected DecisionResult evaluateIfApplicable(EvaluationContext context, D decision)
	{
		EvaluationContext decisionContext = decision.createContext(context);
		return decision.evaluateIfApplicable(decisionContext);
	}
}
