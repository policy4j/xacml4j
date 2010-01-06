package com.artagon.xacml.policy;

import java.util.List;

public interface FunctionImplementation
{
	
	/**
	 * Invokes function instance
	 * 
	 * @param context an evaluation context
	 * @return {@link Value} instance representing
	 * function invocation result
	 * @throws PolicyEvaluationException if an error
	 * occurs during function invocation
	 */
	Value invoke(EvaluationContext context, List<Expression> arguments) 
		throws PolicyEvaluationException;
	
	/**
	 * Invokes function instance
	 * 
	 * @param context an evaluation context
	 * @return {@link Value} instance representing
	 * function invocation result
	 * @throws PolicyEvaluationException if an error
	 * occurs during function invocation
	 */
	Value invoke(EvaluationContext context, Expression ...expressions) 
		throws PolicyEvaluationException;
	
}
