package com.artagon.xacml.v3.policy;

public interface VariableEvaluationCache 
{
	/**
	 * Gets variable evaluation result for given
	 * variable identifier.
	 * 
	 * @param variableId a variable identifier
	 * @return {@link Value} instance or {@code null}
	 */
	 Value getVariableEvaluationResult(String variableId);
	
	/**
	 * Stores variable evaluation result for later
	 * re-use in the context of current policy
	 * evaluation
	 * 
	 * @param variableId a variable identifier
	 * @param value a variable value
	 */
	void setVariableEvaluationResult(String variableId);
}
