package com.artagon.xacml.v3.policy.combine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.util.Preconditions;
import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.XacmlObject;
import com.artagon.xacml.v3.policy.DecisionCombiningAlgorithm;
import com.artagon.xacml.v3.policy.DecisionRule;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.PolicyVisitor;

public abstract class BaseDecisionCombiningAlgorithm <D extends DecisionRule> extends XacmlObject
	implements DecisionCombiningAlgorithm <D>
{
	private final static Logger log = LoggerFactory.getLogger(BaseDecisionCombiningAlgorithm.class);
	
	private String algorithmId;
	
	/**
	 * Creates decision combining algorithm with a
	 * given algorithm identifier
	 * @param algorithmId an algorithm identifier
	 */
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
	protected final Decision evaluateIfApplicable(EvaluationContext context, D decision) 
	{
		EvaluationContext decisionContext = decision.createContext(context);
		return decision.evaluateIfApplicable(decisionContext);
	}
}
