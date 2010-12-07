package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.ValueExpression;


/**
 * A XACML function
 * 
 * @author Giedrius Trumpickas
 */
public interface Function 
{
	/**
	 * Invokes function with a given arguments
	 * 
	 * @param context an evaluation context
	 * @param parameters a function 
	 * invocation parameters
	 * @return {@link ValueExpression} a function invocation
	 * result
	 * @throws EvaluationException if an error occurs
	 * while invoking function or evaluating function
	 * parameters
	 */
	<T extends ValueExpression> T invoke(EvaluationContext context, 
			Expression ...parameters) throws EvaluationException;
}
