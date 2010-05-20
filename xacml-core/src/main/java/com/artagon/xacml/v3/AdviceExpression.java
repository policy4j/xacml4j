package com.artagon.xacml.v3;


public interface AdviceExpression extends DecisionRuleResponseExpression
{

	/**
	 * Evaluates this advice expression by evaluating
	 * all {@link DefaultAttributeAssignmentExpression}
	 * 
	 * @param context an evaluation context
	 * @return {@link DefaultAdvice} instance
	 * @throws EvaluationException if an evaluation error
	 * occurs
	 */
	Advice evaluate(EvaluationContext context) throws EvaluationException;

}