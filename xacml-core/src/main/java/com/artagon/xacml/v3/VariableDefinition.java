package com.artagon.xacml.v3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Represents XACML variable definition.
 * 
 * @author Giedrius Trumpickas
 */
public class VariableDefinition extends XacmlObject implements Expression
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
	
	public String getVariableId(){
		return variableId;
	}
	
	@Override
	public ValueType getEvaluatesTo() {
		return expression.getEvaluatesTo();
	}
	
	public Expression getExpression(){
		return expression;
	}

	/**
	 * Evaluates  variable definition and caches
	 * evaluation result in the current 
	 * {@link EvaluationContext} evaluation context
	 */
	@Override
	public ValueExpression evaluate(EvaluationContext context) throws EvaluationException
	{
		ValueExpression result = context.getVariableEvaluationResult(variableId);
		if(result != null){
			log.debug("Found cached variable=\"{}\" evaluation result=\"{}\"", 
					variableId, result);
			return result;
		}
		result = (ValueExpression)expression.evaluate(context);
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
