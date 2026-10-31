package org.xacml4j.v30.policy;

/*
 * #%L
 * Xacml4J Core Engine Implementation
 * %%
 * Copyright (C) 2009 - 2026 Xacml4J.org
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.List;
import java.util.Optional;

import org.xacml4j.v30.BagOfValuesType;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueExpTypeInfo;
import org.xacml4j.v30.policy.function.FunctionSpecBuilder;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.ValueType;
import org.xacml4j.v30.types.XacmlTypes;

/** A statically checked any-of or all-of call (design D52). */
final class HigherOrderCall
{
	private final FunctionSpec operator;
	private final List<Expression> arguments;
	private final FunctionReference predicate;
	private final BagOfValuesType bagType;
	private final ValueType scalarType;
	private final boolean bagFirst;

	private HigherOrderCall(FunctionSpec operator, List<Expression> arguments, FunctionReference predicate,
			BagOfValuesType bagType, ValueType scalarType, boolean bagFirst)
	{
		this.operator = operator;
		this.arguments = arguments;
		this.predicate = predicate;
		this.bagType = bagType;
		this.scalarType = scalarType;
		this.bagFirst = bagFirst;
	}

	static Optional<HigherOrderCall> prepare(FunctionSpec operator, List<Expression> arguments)
	{
		int count = arguments.size() - 1;
		if((count != 1 && count != 2) || !(arguments.get(0) instanceof FunctionReference ref)){
			return Optional.empty();
		}
		try{
			if(!XacmlTypes.BOOLEAN.equals(ref.getEvaluatesTo())
					|| !FunctionSpecBuilder.canPrepareHigherOrder(operator, ref.function(), count)) return Optional.empty();
			BagOfValuesType bag = null;
			ValueType scalar = null;
			boolean bagFirst = false;
			for(int i = 0; i < count; i++){
				Expression argument = arguments.get(i + 1);
				if(argument == null || argument instanceof FunctionReference) return Optional.empty();
				ValueExpTypeInfo type = argument.getEvaluatesTo();
				// Cache only canonical scalar types and final bag descriptors containing them.
				// Custom descriptors keep the checked invocation, even when equal at construction.
				if(type instanceof BagOfValuesType bagType && bag == null
						&& bagType.getValueType() instanceof XacmlTypes){
					bag = bagType;
					bagFirst = i == 0;
				}else if(type instanceof XacmlTypes valueType && scalar == null){
					scalar = valueType;
				}else{
					return Optional.empty();
				}
				if(!ref.getParamSpecAt(i).isValidParamType(type.getValueType())) return Optional.empty();
			}
			if(bag == null || (count == 2 && scalar == null)) return Optional.empty();
			return Optional.of(new HigherOrderCall(operator, arguments, ref, bag, scalar, bagFirst));
		}catch(RuntimeException e){
			return Optional.empty();
		}
	}

	BooleanVal evaluate(EvaluationContext context)
	{
		Expression first = arguments.get(1).evaluate(context);
		if(arguments.size() == 2){
			return FunctionSpecBuilder.invokeHigherOrderSingle(operator, context, arguments, predicate, first, bagType);
		}
		Expression second = arguments.get(2).evaluate(context);
		return FunctionSpecBuilder.invokeHigherOrderPair(operator, context, arguments, predicate,
				first, second, scalarType, bagType, bagFirst);
	}
}
