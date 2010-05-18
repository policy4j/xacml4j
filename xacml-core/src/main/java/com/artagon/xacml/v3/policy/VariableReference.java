package com.artagon.xacml.v3.policy;

import com.google.common.base.Preconditions;

public final class VariableReference implements Expression
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