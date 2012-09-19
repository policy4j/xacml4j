package com.artagon.xacml.v30.pdp;

import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.EvaluationException;
import com.artagon.xacml.v30.Expression;
import com.artagon.xacml.v30.ExpressionVisitor;
import com.artagon.xacml.v30.ValueExpression;
import com.artagon.xacml.v30.ValueType;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class VariableReference implements Expression
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
	 * Gets variable identifier
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
	 * Gets a definition for this variable
	 *
	 * @return a definition for this variable
	 */
	public VariableDefinition getDefinition(){
		return varDef;
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

	public void accept(ExpressionVisitor expv) {
		VariableReferenceVisitor v = (VariableReferenceVisitor)expv;
		v.visitEnter(this);
		v.visitLeave(this);
	}

	@Override
	public int hashCode(){
		return varDef.hashCode();
	}

	@Override
	public String toString(){
		return Objects
				.toStringHelper(this)
				.add("variableDef", varDef)
				.toString();
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(o == null){
			return false;
		}
		if(!(o instanceof VariableReference)){
			return false;
		}
		VariableReference r = (VariableReference)o;
		return varDef.equals(r.varDef);
	}

	public interface VariableReferenceVisitor extends ExpressionVisitor
	{
		void visitEnter(VariableReference var);
		void visitLeave(VariableReference var);
	}
}