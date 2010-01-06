package com.artagon.xacml.policy;

import java.util.List;

public interface FunctionInvocation
{
	/**
	 * Gets function specification
	 * 
	 * @return {@link FunctionSpec}
	 */
	FunctionSpec getSpec();
	
	/**
	 * Gets function invocation
	 * arguments
	 * 
	 * @return list of invocation arguments
	 */
	List<Expression> getArguments();
	
	/**
	 * Gets function return value type
	 * 
	 * @return function return value type
	 */
	ValueType getReturnType();
	
	/**
	 * Invokes function instance
	 * 
	 * @param context an evaluation context
	 * @return {@link Value} instance representing
	 * function invocation result
	 * @throws PolicyEvaluationException if an error
	 * occurs during function invocation
	 */
	Value invoke(EvaluationContext context) throws PolicyEvaluationException;
	
}
