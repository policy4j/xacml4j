package com.artagon.xacml.policy;

import java.util.List;

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
	 * @throws PolicyEvaluationException if an error occurs
	 * while invoking function or evaluating function
	 * parameters
	 */
	Value invoke(EvaluationContext context, List<Expression> parameters) 
		throws PolicyEvaluationException;
}
