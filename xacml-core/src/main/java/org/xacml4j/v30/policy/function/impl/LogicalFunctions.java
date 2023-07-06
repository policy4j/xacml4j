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
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.policy.function.*;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.IntegerVal;
import org.xacml4j.v30.types.XacmlTypes;


/**
 * This class contains the implementation for XACML logical functions
 * that operate on arguments of data-type "http://www.w3.org/2001/XMLSchema#boolean".
 *
 * @author Giedrius Trumpickas
 */
@XacmlFunctionProvider(description="XACML logical functions")
public final class LogicalFunctions
{

	/** Private constructor for utility class */
	private LogicalFunctions() {}

	/**
	 * This function SHALL return "True" if it has no arguments and SHALL return "False"
	 * if one of its arguments evaluates to "False".
	 * The order of evaluation SHALL be from first argument to last.
	 * The evaluation SHALL stop with a result of "False" if any argument
	 * evaluates to "False", leaving the rest of the arguments unevaluated.
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
		java.lang.Boolean r = java.lang.Boolean.TRUE;
		for(Expression e : values){
			r &= ((BooleanVal) e.evaluate(context)).get();
			if(!r){
				break;
			}
		}
		return XacmlTypes.BOOLEAN.ofAny(r);
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
		java.lang.Boolean r = java.lang.Boolean.FALSE;
		for(Expression e : values){
			java.lang.Boolean v = ((BooleanVal)e.evaluate(context)).get();
			r |= v;
			if(r){
				break;
			}
		}
		return XacmlTypes.BOOLEAN.ofAny(r);
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
	 *
	 * @param context evaluation context
	 * @param n minimum number of parameters
	 * @param values function parameters
	 * @return {@link BooleanVal} representing function evaluation result
	 * @throws EvaluationException if an error occurs during evaluation
	 */
	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:n-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal nof(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#integer") IntegerVal n,
			@XacmlFuncParamVarArg(typeId="http://www.w3.org/2001/XMLSchema#boolean", min=0)Expression...values)
		throws EvaluationException
	{
		if(values.length < n.get()){
			throw new IllegalArgumentException(String.format(
					"Number of arguments=\"%s\" is less " +
					"than minimum required number=\"%s\"",
					values.length, n.get()));
		}
		if(n.get() > java.lang.Integer.MAX_VALUE){
			throw new IllegalArgumentException(String.format(
					"First parameter=\"%s\" is bigger than=\"%d\"",
					n, java.lang.Integer.MAX_VALUE));
		}
		BooleanVal TRUE = XacmlTypes.BOOLEAN.ofAny(true);
		if(n.get() == 0){
			return TRUE;
		}
		int count = 0;
		int num = n.get().intValue();
		for (Expression value : values) {
			ValueExp v = value.evaluate(context);
			if (v.equals(TRUE)) {
				count++;
				if (num == count) {
					return TRUE;
				}
			}
		}
		return XacmlTypes.BOOLEAN.ofAny(false);
	}
}
