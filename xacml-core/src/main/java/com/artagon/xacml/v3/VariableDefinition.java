package com.artagon.xacml.v3;

public interface VariableDefinition extends Expression 
{
	/**
	 * Gets variable identifier.
	 * 
	 * @return variable identifier
	 */
	String getVariableId();
	
	Expression getExpression();
	
	@Override
	Value evaluate(EvaluationContext context) throws EvaluationException;

}