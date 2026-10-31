package org.xacml4j.v30.policy.function.impl;

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

import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.policy.function.*;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.IntegerVal;
import org.xacml4j.v30.types.XacmlTypes;

import java.util.List;


/**
 * This class contains the implementation for XACML logical functions
 * that operate on arguments of data-type "http://www.w3.org/2001/XMLSchema#boolean".
 *
 * @author Giedrius Trumpickas
 */
@XacmlFunctionProvider(description="XACML logical functions")
public final class LogicalFunctions
{
	private static final Expression[] NO_OPERANDS = new Expression[0];

	/** Private constructor for utility class */
	private LogicalFunctions() {}

	/**
	 * This function SHALL return "True" if it has no arguments and SHALL return "False"
	 * if one of its arguments evaluates to "False".
	 * The order of evaluation SHALL be from first argument to last.
	 * The evaluation SHALL stop with a result of "False" if any argument
	 * evaluates to "False", leaving the rest of the arguments unevaluated.
	 * An argument that evaluates to "False" decides the result even when an
	 * argument before it failed. When no argument is "False" and one failed,
	 * the result is Indeterminate with the status of the first failure.
	 *
	 * @param context evaluation context
	 * @param values function parameters
	 * @return {@link BooleanVal} representing function evaluation result
	 * @throws EvaluationException if an error occurs during evaluation
	 */
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:and", evaluateArguments=false)
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal and(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#boolean", min=0)Expression ...values)
		throws EvaluationException
	{
		return XacmlTypes.BOOLEAN.ofAny(DecidingOperands.allTrue(context, operands(values)));
	}

	static BooleanVal and(org.xacml4j.v30.EvaluationContext context, List<Expression> values)
	{
		return XacmlTypes.BOOLEAN.ofAny(DecidingOperands.allTrue(context, values));
	}

	static BooleanVal or(org.xacml4j.v30.EvaluationContext context, List<Expression> values)
	{
		return XacmlTypes.BOOLEAN.ofAny(DecidingOperands.anyTrue(context, values));
	}

	/** The invocation passes {@code null} for a variadic parameter without arguments. */
	private static Expression[] operands(Expression[] values)
	{
		return values == null?NO_OPERANDS:values;
	}


	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:not")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal not(
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#boolean") BooleanVal v)
	{
		return XacmlTypes.BOOLEAN.ofAny(!v.get());
	}

	/**
	 * This function SHALL return "False" if it has no arguments and SHALL return "True"
	 * if at least one of its arguments evaluates to "True".
	 * The order of evaluation SHALL be from first argument to last.
	 * The evaluation SHALL stop with a result of "True" if any argument
	 * evaluates to "True", leaving the rest of the arguments unevaluated.
	 * An argument that evaluates to "True" decides the result even when an
	 * argument before it failed. When no argument is "True" and one failed,
	 * the result is Indeterminate with the status of the first failure.
	 *
	 * @param context evaluation context
	 * @param values function parameters
	 * @return {@link BooleanVal} representing function evaluation result
	 * @throws EvaluationException if an error occurs during evaluation
	 */
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:or", evaluateArguments=false)
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal or(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#boolean", min=0)Expression...values)
		throws EvaluationException
	{
		return XacmlTypes.BOOLEAN.ofAny(DecidingOperands.anyTrue(context, operands(values)));
	}


	/**
	 * The first argument to this function SHALL be of data-type http://www.w3.org/2001/XMLSchema#integer.
	 * The remaining arguments SHALL be of data-type http://www.w3.org/2001/XMLSchema#boolean.
	 * The first argument specifies the minimum number of the remaining arguments
	 * that MUST evaluate to "True" for the expression to be considered "True".
	 * If the first argument is 0, the result SHALL be "True".
	 * If the number of arguments after the first one is less than the
	 * value of the first argument, then the expression SHALL result in "Indeterminate".
	 * The order of evaluation SHALL be: first evaluate the integer value, and
	 * then evaluate each subsequent argument. The evaluation SHALL stop and return "True"
	 * if the specified number of arguments evaluate to "True".
	 * The evaluation of arguments SHALL stop if it is determined that evaluating
	 * the remaining arguments will not satisfy the requirement.
	 * An argument that failed counts as possibly "True": the result is
	 * "True" when enough other arguments are "True", "False" when the
	 * number can not be reached even with the failed arguments, and
	 * Indeterminate with the status of the first failure otherwise.
	 *
	 * @param context evaluation context
	 * @param n minimum number of parameters
	 * @param values function parameters
	 * @return {@link BooleanVal} representing function evaluation result
	 * @throws EvaluationException if an error occurs during evaluation
	 */
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:n-of", evaluateArguments=false)
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal nof(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer") Expression n,
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#boolean", min=0)Expression...values)
		throws EvaluationException
	{
		// The integer is evaluated first, then the other arguments one by one.
		IntegerVal min = n.evaluate(context);
		Expression[] operands = operands(values);
		if(operands.length < min.get()){
			throw new IllegalArgumentException(String.format(
					"Number of arguments=\"%s\" is less " +
					"than minimum required number=\"%s\"",
					operands.length, min.get()));
		}
		if(min.get() > java.lang.Integer.MAX_VALUE){
			throw new IllegalArgumentException(String.format(
					"First parameter=\"%s\" is bigger than=\"%d\"",
					min, java.lang.Integer.MAX_VALUE));
		}
		if(min.get() == 0){
			return XacmlTypes.BOOLEAN.ofAny(true);
		}
		return XacmlTypes.BOOLEAN.ofAny(DecidingOperands.atLeast(context, min.get().intValue(), operands));
	}
}
