package org.xacml4j.v30.pdp;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueExpression;
import org.xacml4j.v30.ValueType;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Represents XACML variable definition.
 *
 * @author Giedrius Trumpickas
 */
public class VariableDefinition implements PolicyElement
{
	private final static Logger log = LoggerFactory.getLogger(VariableDefinition.class);

	private final String variableId;
	private final Expression expression;

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

	public ValueType getEvaluatesTo() {
		return expression.getEvaluatesTo();
	}

	/**
	 * Gets variable expression
	 *
	 * @return a variable expression
	 */
	public Expression getExpression(){
		return expression;
	}

	@Override
	public String toString(){
		return Objects.toStringHelper(this)
				.add("variableId", variableId)
				.add("expression", expression)
				.toString();
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(
				variableId,
				expression);
	}

	@Override
	public boolean equals(Object o){
		if(o == this){
			return true;
		}
		if(!(o instanceof VariableDefinition)){
			return false;
		}
		VariableDefinition var = (VariableDefinition)o;
		return variableId.equals(var.variableId) &&
				expression.equals(var.expression);
	}

	/**
	 * Evaluates  variable definition and caches
	 * evaluation result in the current
	 * {@link EvaluationContext} evaluation context
	 */
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
		v.visitLeave(this);
	}
}
