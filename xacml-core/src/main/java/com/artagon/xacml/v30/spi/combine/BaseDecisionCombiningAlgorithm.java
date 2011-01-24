package com.artagon.xacml.v30.spi.combine;

import com.artagon.xacml.v30.Decision;
import com.artagon.xacml.v30.DecisionCombiningAlgorithm;
import com.artagon.xacml.v30.DecisionRule;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.XacmlObject;
import com.google.common.base.Preconditions;

public abstract class BaseDecisionCombiningAlgorithm <D extends DecisionRule> extends XacmlObject
	implements DecisionCombiningAlgorithm <D>
{
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
	
	/**
	 * A helper method which invokes {@link DecisionRule#createContext(EvaluationContext)}
	 * then sub-sequentially invokes {@link DecisionRule#evaluateIfApplicable(EvaluationContext)}
	 * with the just created {@link EvaluationContext} instance as an argument
	 * 
	 * @param context a parent evaluation context
	 * @param decision a decision rule to be evaluated
	 * @return evaluation result as {@link Decision} instance
	 */
	protected final Decision evaluateIfApplicable(EvaluationContext context, D decision) {
		EvaluationContext decisionContext = decision.createContext(context);
		return decision.evaluateIfApplicable(decisionContext);
	}
	
	/**
	 * A helper method which invokes {@link DecisionRule#createContext(EvaluationContext)}
	 * then sub-sequentially invokes {@link DecisionRule#evaluate(EvaluationContext)}
	 * with the just created {@link EvaluationContext} instance as an argument
	 * 
	 * @param context a parent evaluation context
	 * @param decision a decision rule to be evaluated
	 * @return evaluation result as {@link Decision} instance
	 */
	protected final Decision evaluate(EvaluationContext context, D decision) {
		EvaluationContext decisionContext = decision.createContext(context);
		return decision.evaluate(decisionContext);
	}
}
