package org.xacml4j.v30.spi.function;

import java.util.List;

import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueExpression;
import org.xacml4j.v30.pdp.FunctionInvocationException;
import org.xacml4j.v30.pdp.FunctionSpec;


/**
 * An interface for a function invocation
 * 
 * @author Giedrius Trumpickas
 */
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
	 * fails 
	 */
	ValueExpression invoke(FunctionSpec spec, 
			EvaluationContext context, Expression ...arguments) 
		throws FunctionInvocationException;
	
	/**
	 * Invokes function
	 * 
	 * @param <T>
	 * @param spec a function spec
	 * @param context an evaluation context
	 * @param params a function invocation parameters
	 * @return {@link T} a function invocation result
	 * @throws FunctionInvocationException if a function
	 * invocation fails
	 */
	ValueExpression invoke(FunctionSpec spec, 
			EvaluationContext context, List<Expression> params) 
		throws FunctionInvocationException;
}
