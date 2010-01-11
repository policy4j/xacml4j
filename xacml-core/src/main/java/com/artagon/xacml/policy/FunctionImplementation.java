package com.artagon.xacml.policy;

import java.util.List;

public interface FunctionImplementation
{
	/**
	 * Resolves function return type based
	 * on a given function arguments
	 * 
	 * @param arguments a function invocation arguments
	 * @return {@link ValueType} a function return type
	 */
	ValueType resolveReturnType(List<Expression> arguments);
	
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
