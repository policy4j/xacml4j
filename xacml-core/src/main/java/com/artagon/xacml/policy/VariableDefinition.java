package com.artagon.xacml.policy;

import com.artagon.xacml.util.Preconditions;

/**
 * Represents XACML variable definition.
 * 
 * @author Giedrius Trumpickas
 */
public class VariableDefinition implements ValueExpression
{
	private String variableId;
	private ValueExpression expression;
	
	/**
	 * Constructs variable definition 
	 * with given variable identifier and expression.
	 * 
	 * @param variableId a variable identifier
	 * @param expression a variable expression
	 */
	public VariableDefinition(String variableId, 
			ValueExpression expression){
		Preconditions.checkNotNull(variableId);
		Preconditions.checkNotNull(expression);
		this.variableId = variableId;
		this.expression = expression;
	}
	
	/**
	 * Gets variable identifier.
	 * 
	 * @return variable identifier
	 */
	public String getVariableId(){
		return variableId;
	}
	
	@Override
	public ValueType getEvaluatesTo() {
		return expression.getEvaluatesTo();
	}

	public Value evaluate(EvaluationContext context) throws PolicyEvaluationException
	{
		Value result = context.getVariableEvaluationResult(variableId);
		if(result != null){
			return result;
		}
		result = (Value)expression.evaluate(context);
		context.setVariableEvaluationResult(variableId, result);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see com.artagon.xacml.Expression#aceept(com.artagon.xacml.ExpressionVisitor)
	 */
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}
}
