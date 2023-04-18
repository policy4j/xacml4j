package org.xacml4j.v30.policy;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2014 Xacml4J.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ExpressionVisitor;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.ValueExpTypeInfo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class VariableReference implements Expression
{
	private final VariableDefinition varDef;

	/**
	 * Constructs variable reference with a given definition.
	 *
	 * @param varDef a variable definition
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
	public ValueExpTypeInfo getEvaluatesTo() {
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
	 * @return {@link ValueExp} representing evaluation
	 * result
	 */
	public ValueExp evaluate(EvaluationContext context)
		throws EvaluationException
	{
		return varDef.evaluate(context);
	}

	public void accept(ExpressionVisitor expv)
	{
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
		return MoreObjects
				.toStringHelper(this)
				.add("variableDef", varDef)
				.toString();
	}

	@Override
	public boolean equals(Object o){
		if (o == this) {
			return true;
		}
		if (!(o instanceof VariableReference)) {
			return false;
		}
		VariableReference r = (VariableReference)o;
		return varDef.equals(r.varDef);
	}

	public interface VariableReferenceVisitor extends ExpressionVisitor
	{
		default void visitEnter(VariableReference var){
			var.accept(this);
		}

		default void visitLeave(VariableReference var){
			var.accept(this);
		}
	}
}
