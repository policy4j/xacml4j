package com.artagon.xacml.v30.policy;


public interface Function 
{
	/**
	 * Invokes function with a given arguments
	 * 
	 * @param context an evaluation context
	 * @param parameters a function 
	 * invocation parameters
	 * @return {@link Value} a function invocation
	 * result
	 * @throws EvaluationException if an error occurs
	 * while invoking function or evaluating function
	 * parameters
	 */
	Value invoke(EvaluationContext context, Expression ...parameters) 
		throws EvaluationException;
}
