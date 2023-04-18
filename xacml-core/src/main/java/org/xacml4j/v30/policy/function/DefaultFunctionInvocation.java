package org.xacml4j.v30.policy.function;

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

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.policy.FunctionInvocationException;
import org.xacml4j.v30.policy.FunctionSpec;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

public final class DefaultFunctionInvocation implements FunctionInvocation
{
	private final static Logger LOG = LoggerFactory.getLogger(DefaultFunctionInvocation.class);

	private boolean evalContextRequired;
	private PolicyToPlatformFunctionInvocation<ValueExp> invocation;

	/**
	 * Constructs XACML function invoker
	 *
	 * @param invocation an expression invocation
	 * @param evalContextRequired a flag indicating if method
	 * requires an {@link EvaluationContext} reference
	 */
	public DefaultFunctionInvocation(
			PolicyToPlatformFunctionInvocation<ValueExp> invocation,
			boolean evalContextRequired)
	{
		this.invocation = Objects.requireNonNull(invocation, "invocation");
		this.evalContextRequired = evalContextRequired;
	}

	@Override
	public ValueExp invoke(FunctionSpec spec,
	                       EvaluationContext context, Expression ...params)
			throws FunctionInvocationException{
		return invoke(spec, context, (params == null)?
				Collections.<Expression>emptyList():
					Arrays.asList(params));
	}

	@Override
	public ValueExp invoke(
			FunctionSpec spec,
			EvaluationContext context,
			List<Expression> arguments)
			throws FunctionInvocationException
	{
		Preconditions.checkNotNull(spec, "Function spec is null");
		Preconditions.checkNotNull(context, "Evaluation context is null");
		Preconditions.checkNotNull(arguments, "Arguments list is null");
		try
		{
			int numOfParms = spec.getNumberOfParams();
			Object[] params = new Object[numOfParms + (evalContextRequired?1:0)];
			int startIndex = 0;
			if(evalContextRequired){
				LOG.debug("Adding evaluation context");
				params[0] = context;
				startIndex++;
			}
			copyInto(arguments, 0, params,
					startIndex,
					(spec.isVariadic()?numOfParms - 1:numOfParms));
			if(spec.isVariadic()){
				Object[] varArgArray = null;
				if(numOfParms <= arguments.size()){
					int size = arguments.size() - (numOfParms - 1) ;
					if(LOG.isDebugEnabled()){
						LOG.debug("Number of variadic parameters=\"{}\" to copy from index={}",
						          size, numOfParms - 1);
					}
					Expression exp = arguments.get(numOfParms - 1);
					if(exp == null){
						varArgArray = (Object[])Array.newInstance(Object.class, size);
					}else {
						varArgArray = (Object[])Array.newInstance(exp.getClass(), size);
					}
					copyInto(arguments, numOfParms - 1, varArgArray, 0, size);
					if(LOG.isDebugEnabled()){
						LOG.debug("VarArg param size={} value={}",
						          varArgArray.length, Arrays.deepToString(varArgArray));
					}
				}
				params[params.length - 1] = (varArgArray != null && varArgArray.length == 1 &&
						varArgArray[0] == null)?null:varArgArray;
			}
			if(LOG.isDebugEnabled()){
				LOG.debug("Params={}", Arrays.deepToString(params));
			}

			return invocation.invoke(params);
		}
		catch(Exception e){
			throw new FunctionInvocationException(spec, 
					Throwables.getRootCause(e));
		}
	}

	/**
	 * Copies elements from a given list to the given array
	 * at the given position
	 *
	 * @param src a source list
	 * @param srcPos a starting position in the list
	 * @param dst a destination array
	 * @param dstPos a destination position in array
	 * @param size a number of elements to copy
	 */
	private static void copyInto(
			List<?> src,
			int srcPos, Object[] dst, int dstPos, int size)
	{
		for(int i = srcPos, j = 0; j < size ; i++ ){
			dst[dstPos + j] = src.get(i);
			j++;
		}
	}
}
