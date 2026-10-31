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

import java.util.Objects;

import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ExpressionVisitor;
import org.xacml4j.v30.ValueExpTypeInfo;
import org.xacml4j.v30.types.DynamicType;

import com.google.common.base.MoreObjects;

/**
 * An argument whose data type is known only at evaluation
 * ({@link DynamicType}), together with the parameter that it is passed
 * to. When the expression was built the argument was accepted because its
 * type is consistent with the parameter. Here its value is checked: the
 * argument is evaluated, and the type of the value has to fit the
 * parameter. {@link Apply} puts this around every argument of dynamic
 * type, so a function never gets a value that does not fit.
 * <p>
 * A function that evaluates its own arguments evaluates this expression
 * when it needs the operand, and not otherwise.
 */
final class TypeAssertion implements Expression
{
	private final Expression argument;
	private final FunctionParamSpec parameter;
	private final FunctionSpec function;

	TypeAssertion(Expression argument, FunctionParamSpec parameter, FunctionSpec function){
		this.argument = Objects.requireNonNull(argument, "argument");
		this.parameter = Objects.requireNonNull(parameter, "parameter");
		this.function = Objects.requireNonNull(function, "function");
	}

	@Override
	public ValueExpTypeInfo getEvaluatesTo(){
		return argument.getEvaluatesTo();
	}

	/**
	 * @return the value of the argument
	 * @exception EvaluationException with status processing-error if the
	 * type of the value does not fit the parameter
	 */
	@Override
	public <T extends Expression> T evaluate(EvaluationContext context) throws EvaluationException
	{
		T value = argument.evaluate(context);
		if(!parameter.isValidParamType(value.getEvaluatesTo())){
			// The status message names the function only; the value is from the request.
			FunctionInvocationException failure = new FunctionInvocationException(function, Apply.NOT_TYPE_CORRECT, function.getId());
			if(!function.isRequiresLazyParamEval()){
				// A lazy function evaluates this inside its call, which reports the failure (design D34)
				RootEvaluationContext.observationOf(context).functionFailed(context, function, failure);
			}
			throw failure;
		}
		return value;
	}

	@Override
	public void accept(ExpressionVisitor v){
		argument.accept(v);
	}

	@Override
	public String toString(){
		return MoreObjects.toStringHelper(this)
				.add("argument", argument)
				.add("parameter", parameter)
				.toString();
	}
}
