package com.artagon.xacml.v3.policy.impl;

import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.PolicyVisitor;
import com.artagon.xacml.v3.Value;
import com.artagon.xacml.v3.ValueType;
import com.artagon.xacml.v3.VariableDefinition;
import com.artagon.xacml.v3.VariableReference;
import com.artagon.xacml.v3.XacmlObject;
import com.google.common.base.Preconditions;

final class DefaultVariableReference extends XacmlObject implements VariableReference
{
	private VariableDefinition varDef;
	
	/**
	 * Constructs variable reference with a given identifier.
	 * 
	 * @param variableId a  variable identifier
	 */
	public DefaultVariableReference(VariableDefinition varDef){
		Preconditions.checkNotNull(varDef);
		this.varDef = varDef;
	}
	
	@Override
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