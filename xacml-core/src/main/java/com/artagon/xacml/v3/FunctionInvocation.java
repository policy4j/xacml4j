package com.artagon.xacml.v3;


public interface FunctionInvocation
{
	/**
	 * Invokes function
	 * 
	 * @param <T>
	 * @param spec a function spec
	 * @param context an evaluation context
	 * @param arguments a function invocation parameters
	 * @return {@link Value} representing function invocation result
	 * @throws FunctionInvocationException if function invocation
	 * fails for some reason
	 */
	<T extends Value> T invoke(FunctionSpec spec, 
			EvaluationContext context, Expression ...arguments) 
		throws FunctionInvocationException;
}
