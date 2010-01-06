package com.artagon.xacml.policy;

public interface Expression extends PolicyElement
{
	/**
	 * Evaluates this expression
	 * 
	 * @param context an evaluation context
	 * @return {@link Expression} an expression 
	 * representing evaluation result
	 * @throws PolicyEvaluationException if an evaluation error
	 * occurs
	 */
	Expression evaluate(EvaluationContext context) throws PolicyEvaluationException;
}
