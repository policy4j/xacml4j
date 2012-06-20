package com.artagon.xacml.v30.spi.combine;

import com.artagon.xacml.v30.Decision;
import com.artagon.xacml.v30.DecisionRule;
import com.artagon.xacml.v30.EvaluationContext;

/**
 * An utility class for evaluating {@link DecisionRule}
 * 
 * @author Giedrius Trumpickas
 */
public final class DecisionCombingingAlgorithms 
{
	private DecisionCombingingAlgorithms(){
	}
	
	/**
	 * A helper method which invokes {@link DecisionRule#createContext(EvaluationContext)}
	 * then sub-sequentially invokes {@link DecisionRule#evaluateIfMatch(EvaluationContext)}
	 * with the just created {@link EvaluationContext} instance as an argument
	 * 
	 * @param context a parent evaluation context
	 * @param decision a decision rule to be evaluated
	 * @return evaluation result as {@link Decision} instance
	 */
	public static <D extends DecisionRule> Decision evaluateIfMatch(EvaluationContext context, D decision) {
		EvaluationContext decisionContext = decision.createContext(context);
		return decision.evaluateIfMatch(decisionContext);
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
	public static <D extends DecisionRule> Decision evaluate(EvaluationContext context, D decision) {
		EvaluationContext decisionContext = decision.createContext(context);
		return decision.evaluate(decisionContext);
	}
}
