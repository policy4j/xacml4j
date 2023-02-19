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

import java.util.List;

import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueExpression;
import org.xacml4j.v30.policy.FunctionInvocationException;
import org.xacml4j.v30.policy.FunctionSpec;


/**
 * An interface for a function invocation
 *
 * @author Giedrius Trumpickas
 */
public interface FunctionInvocation
{
	/**
	 * Invokes function
	 *
	 * @param spec a function spec
	 * @param context an evaluation context
	 * @param arguments a function invocation parameters
	 * @return {@link ValueExpression} representing function invocation result
	 * @throws FunctionInvocationException if function invocation fails
	 */
	ValueExpression invoke(FunctionSpec spec,
			EvaluationContext context, Expression ...arguments)
		throws FunctionInvocationException;

	/**
	 * Invokes function
	 *
	 * @param spec a function spec
	 * @param context an evaluation context
	 * @param params a function invocation parameters
	 * @return a function invocation result
	 * @throws FunctionInvocationException if a function invocation fails
	 */
	ValueExpression invoke(FunctionSpec spec,
			EvaluationContext context, List<Expression> params)
		throws FunctionInvocationException;
}
