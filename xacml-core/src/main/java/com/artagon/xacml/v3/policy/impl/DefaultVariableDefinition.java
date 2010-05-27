package com.artagon.xacml.v3.policy.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.PolicyVisitor;
import com.artagon.xacml.v3.Value;
import com.artagon.xacml.v3.ValueType;
import com.artagon.xacml.v3.VariableDefinition;
import com.artagon.xacml.v3.XacmlObject;
import com.google.common.base.Preconditions;

/**
 * Represents XACML variable definition.
 * 
 * @author Giedrius Trumpickas
 */
final class DefaultVariableDefinition extends XacmlObject implements VariableDefinition
{
	private final static Logger log = LoggerFactory.getLogger(DefaultVariableDefinition.class);
	
	private String variableId;
	private Expression expression;
	
	/**
	 * Constructs variable definition 
	 * with given variable identifier and expression.
	 * 
	 * @param variableId a variable identifier
	 * @param expression a variable expression
	 */
	public DefaultVariableDefinition(String variableId, 
			Expression expression){
		Preconditions.checkNotNull(variableId);
		Preconditions.checkNotNull(expression);
		this.variableId = variableId;
		this.expression = expression;
	}
	
	@Override
	public String getVariableId(){
		return variableId;
	}
	
	@Override
	public ValueType getEvaluatesTo() {
		return expression.getEvaluatesTo();
	}
	
	@Override
	public Expression getExpression(){
		return expression;
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

	@Override
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		expression.accept(v);
		v.visitLeave(this);
	}
}
