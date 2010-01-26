package com.artagon.xacml.v3.policy;

import java.util.List;

public interface FunctionSpec 
{
	/**
	 * Gets function identifier.
	 * 
	 * @return XACML function identifier
	 */
	String getXacmlId();

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
	 * Tests if this function requires lazy
	 * parameters evaluation
	 * 
	 * @return <code>true</code> if this
	 * function requires lazy parameters
	 * evaluation
	 */
	boolean isRequiresLazyParamEval();
	
	/**
	 * Creates invocation expression for this
	 * function with a given arguments
	 * 
	 * @param arguments a function arguments
	 * @return {@link Apply} to be used
	 * in XACML policy expressions
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
	
	
	boolean validateParameters(Expression ... params);
	
	/**
	 * Invokes this function with a given arguments
	 * 
	 * @return {@link Value} instance representing
	 * function invocation result
	 */
	<T extends Value> T invoke(EvaluationContext context, Expression ...expressions) 
		throws EvaluationException;
}