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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.BagStorage;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.ValueExpTypeInfo;
import org.xacml4j.v30.policy.function.FunctionCategory;
import org.xacml4j.v30.types.Value;


/**
 * A XACML function specification.
 * Provides function signature metadata
 *
 * @author Giedrius Trumpickas
 */
public interface FunctionSpec
{
	/**
	 * Gets function XACML identifier.
	 *
	 * @return XACML function identifier
	 */
	String getId();

	FunctionCategory getCategory();

	/**
	 * Gets short function identifier
	 * @return
	 */
	default String getShortId(){
		return getId();
	}

	/**
	 * Gets function legacy identifier
	 * if any available
	 *
	 * @return legacy identifier
	 */
	Optional<String> getLegacyId();
	
	/**
	 * Gets parameter specification
	 * at given
	 *
	 * @param index a parameter index
	 * @return {@link FunctionParamSpec} at given
	 * index
	 */
	FunctionParamSpec getParamSpecAt(int index);

	/**
	 * Gets number of function formal
	 * parameters
	 *
	 * @return gets number of function formal
	 * parameters
	 */
	int getNumberOfParams();

	/**
	 * Tells if this function has variable length
	 * parameter
	 *
	 * @return {@code true} if it does
	 * {@code false} otherwise
	 */
	boolean isVariadic();

	/**
	 * Tests if this function requires lazy
	 * parameters evaluation
	 *
	 * @return {@code true} if this
	 * function requires lazy parameters
	 * evaluation
	 */
	boolean isRequiresLazyParamEval();

	/**
	 * Validates given array of expressions
	 * as potential function invocation arguments
	 *
	 * @param arguments an array of expressions
	 */
	boolean validateParameters(List<Expression> arguments);

	/**
	 * Validates given array of expressions
	 * as potential function invocation arguments
	 *
	 * @param arguments an array of expressions
	 * @exception SyntaxException
	 */
	void validateParametersAndThrow(List<Expression> arguments)
		throws SyntaxException;

	/**
	 * Resolves function return type based on function
	 * invocation arguments
	 *
	 * @param arguments a function invocation arguments
	 * @return {@link ValueExpTypeInfo} resolved function return type
	 */
	ValueExpTypeInfo resolveReturnType(List<Expression> arguments);

	/**
	 * Invokes this function with a given arguments
	 *
	 * @return {@link ValueExp} defaultProvider representing
	 * function invocation result
	 */
	default<T extends ValueExp> T invoke(EvaluationContext context, Expression ...arguments)
		throws EvaluationException{
		return invokeWithList(context, arguments == null ? Collections.emptyList() : Arrays.asList(arguments));
	}

	/**
	 * Invokes this function with two arguments, as a {@code Match}
	 * predicate is called. The default calls the variadic form, so an
	 * implementation that overrides it keeps its behavior; the built-in
	 * implementation overrides this method to skip the argument array.
	 *
	 * @return {@link ValueExp} representing function invocation result
	 */
	default<T extends ValueExp> T invoke(EvaluationContext context, Expression arg0, Expression arg1)
		throws EvaluationException{
		return invoke(context, new Expression[]{arg0, arg1});
	}

	/** Scans a bag with this two-argument predicate, with the literal first. */
	default BagStorage.Scan scan(EvaluationContext context, Value<?> literal, BagOfValues bag) {
		return BagStorage.Scan.NONE;
	}

	/**
	 * Tells whether {@link #scan} with this literal can answer a match or a no-match: a built-in
	 * two-argument predicate with a bag scan over the literal's type can, so a target over it is
	 * matched before the context of its rule exists (design D41)
	 *
	 * @param literal the literal of a match
	 * @return {@code true} when a scan with this literal can answer; {@code false} by default
	 */
	default boolean hasDirectScan(Value<?> literal) {
		return false;
	}

	/**
	 * Binds a literal first argument of a two-argument call of this function at its call site
	 * (change add-regex-engine-spi, design D6), when this function prepares such a literal once,
	 * as the regexp-match functions compile a pattern. The default binds nothing.
	 *
	 * @param literal the literal first argument of a call
	 * @return the bound call, or nothing when this function binds no literal
	 */
	default Optional<BoundCall> bind(Value<?> literal) {
		return Optional.empty();
	}

	/**
	 * Tells whether this function writes no advice or obligations to the evaluation context. The
	 * functions of the engine's own providers, called through the default invocation factory, write
	 * none; any other function counts as one that can, unless its builder declares otherwise
	 * (design D36).
	 *
	 * @return {@code true} if this function writes no advice or obligations
	 */
	default boolean writesNoResponses() {
		return false;
	}

	/**
	 * Invokes this function with the given arguments. The list is not changed.
	 *
	 * @return {@link ValueExp} representing function invocation result
	 */
	<T extends ValueExp> T invokeWithList(EvaluationContext context, List<Expression> arguments)
			throws EvaluationException;

}
