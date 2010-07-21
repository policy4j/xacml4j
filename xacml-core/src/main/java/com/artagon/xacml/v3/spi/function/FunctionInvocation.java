package com.artagon.xacml.v3.spi.function;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.FunctionInvocationException;
import com.artagon.xacml.v3.FunctionSpec;
import com.artagon.xacml.v3.ValueExpression;


public interface FunctionInvocation
{
	/**
	 * Invokes function
	 * 
	 * @param <T>
	 * @param spec a function spec
	 * @param context an evaluation context
	 * @param arguments a function invocation parameters
	 * @return {@link ValueExpression} representing function invocation result
	 * @throws FunctionInvocationException if function invocation
	 * fails for some reason
	 */
	<T extends ValueExpression> T invoke(FunctionSpec spec, 
			EvaluationContext context, Expression ...arguments) 
		throws FunctionInvocationException;
}
