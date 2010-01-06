package com.artagon.xacml.policy;

import com.artagon.xacml.util.Preconditions;


public class VariableReference implements ValueExpression
{
	private VariableDefinition varDef;
	
	/**
	 * Constructs variable reference with a given identifier.
	 * 
	 * @param variableId a  variable identifier
	 */
	public VariableReference(VariableDefinition varDef){
		Preconditions.checkNotNull(varDef);
		this.varDef = varDef;
	}
	
	/**
	 * Gets variable identifier.
	 * 
	 * @return variable identifier
	 */
	public String getVariableId(){
		return varDef.getVariableId();
	}
	
	@Override
	public ValueType getEvaluatesTo() {
		return varDef.getEvaluatesTo();
	}

	/**
	 * Evaluates appropriate variable definition.
	 * 
	 * @param context a policy evaluation context
	 * @return {@link Value} representing evaluation
	 * result
	 */
	public Value evaluate(EvaluationContext context) 
		throws PolicyEvaluationException
	{
		VariableDefinition variableDef = context.getVariableDefinition(getVariableId());
		if(variableDef == null){
			throw new PolicyEvaluationIndeterminateException("Failed to resolve variable=\"{}\"", getVariableId());
		}
		return varDef.evaluate(context);
	}
	
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		v.visitLeave(this);
	}	
}