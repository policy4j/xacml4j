package com.artagon.xacml.v3.policy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.util.Preconditions;

/**
 * Represents XACML variable definition.
 * 
 * @author Giedrius Trumpickas
 */
public final class VariableDefinition implements Expression
{
	private final static Logger log = LoggerFactory.getLogger(VariableDefinition.class);
	
	private String variableId;
	private Expression expression;
	
	/**
	 * Constructs variable definition 
	 * with given variable identifier and expression.
	 * 
	 * @param variableId a variable identifier
	 * @param expression a variable expression
	 */
	public VariableDefinition(String variableId, 
			Expression expression){
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

	/**
	 * Evaluates  variable definition and caches
	 * evaluation result in the current 
	 * {@link EvaluationContext} evaluation context
	 */
	@Override
	public Value evaluate(EvaluationContext context) throws EvaluationException
	{
		Value result = context.getVariableEvaluationResult(variableId);
		if(result != null){
			log.debug("Found cached variable=\"{}\" evaluation result=\"{}\"", 
					variableId, result);
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
		expression.accept(v);
		v.visitLeave(this);
	}
}
