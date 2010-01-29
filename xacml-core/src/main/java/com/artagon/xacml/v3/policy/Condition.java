package com.artagon.xacml.v3.policy;

public interface Condition extends PolicyElement
{
	/**
	 * Evaluates this condition and returns instance of
	 * {@link ConditionResult}
	 * 
	 * @param context an evaluation context
	 * @return {@link ConditionResult}
	 */
	ConditionResult evaluate(EvaluationContext context);
}