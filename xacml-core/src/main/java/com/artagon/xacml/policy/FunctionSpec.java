package com.artagon.xacml.policy;

import java.util.List;

import com.artagon.xacml.FunctionId;

public interface FunctionSpec 
{
	/**
	 * Gets function identifier.
	 * 
	 * @return XACML function identifier
	 */
	FunctionId getId();

	/**
	 * Gets function parameter specifications.
	 * 
	 * @return list of {@link ParamSpec} instances
	 */
	List<ParamSpec> getParamSpecs();

	/**
	 * Gets number of function formal 
	 * parameters
	 * 
	 * @return gets number of function formal
	 * parameters
	 */
	int getNumberOfParams();
	
	/**
	 * Creates invocation expression for this
	 * function with a given arguments
	 * 
	 * @param arguments a function arguments
	 * @return {@link Apply} to be used
	 * in XACML policy expressions
	 */
	Apply createApply(List<Expression> arguments);
	
	/**
	 * @see {@link #createApply(List)}
	 */
	Apply createApply(Expression ... arguments);
	
	/**
	 * Creates this function reference 
	 * expression to be used in XACML
	 * policy expressions to pass
	 * functions as arguments to XACML higher
	 * order functions
	 * 
	 * @return {@link FunctionReferenceExpression}
	 */
	FunctionReferenceExpression createReference();
	
	/**
	 * Invokes this function with a given arguments
	 * 
	 * @return {@link Value} instance representing
	 * function invocation result
	 */
	Value invoke(EvaluationContext context, List<Expression> arguments) 
		throws PolicyEvaluationException;
	
	/**
	 * Invokes this function with a given arguments
	 * 
	 * @return {@link Value} instance representing
	 * function invocation result
	 */
	Value invoke(EvaluationContext context, Expression ...expressions) 
		throws PolicyEvaluationException;
}