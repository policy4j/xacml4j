package com.artagon.xacml.v3;

import com.google.common.base.Preconditions;

public class VariableReference extends XacmlObject implements Expression
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
	 * @return {@link ValueExpression} representing evaluation
	 * result
	 */
	public ValueExpression evaluate(EvaluationContext context) 
		throws EvaluationException
	{
		return varDef.evaluate(context);
	}
	
	public void accept(PolicyVisitor v) {
		v.visitEnter(this);
		varDef.accept(v);
		v.visitLeave(this);
	}	
}