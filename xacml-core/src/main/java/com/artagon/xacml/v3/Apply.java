package com.artagon.xacml.v3;


public interface Apply extends PolicyElement, Expression
{
	/**
	 * Gets XACML function identifier
	 * 
	 * @return XACML function identifier
	 */
	String getFunctionId();

	/**
	 * Evaluates given expression by invoking function
	 * with a given parameters
	 * 
	 * @param context an evaluation context
	 * @return expression evaluation result as {@link Value}
	 * instance
	 */
	Value evaluate(EvaluationContext context)
			throws EvaluationException;

}