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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.Value;
import org.xacml4j.v30.ValueType;
import org.xacml4j.v30.ValueTypeInfo;
import org.xacml4j.v30.policy.FunctionReference;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.FunctionParametersValidator;
import org.xacml4j.v30.policy.function.FunctionReturnTypeResolver;
import org.xacml4j.v30.policy.function.XacmlEvaluationContextParam;
import org.xacml4j.v30.policy.function.XacmlFuncParamAnyAttribute;
import org.xacml4j.v30.policy.function.XacmlFuncParamAnyBag;
import org.xacml4j.v30.policy.function.XacmlFuncParamFunctionReference;
import org.xacml4j.v30.policy.function.XacmlFuncParamValidator;
import org.xacml4j.v30.policy.function.XacmlFuncReturnType;
import org.xacml4j.v30.policy.function.XacmlFuncReturnTypeResolver;
import org.xacml4j.v30.policy.function.XacmlFuncSpec;
import org.xacml4j.v30.policy.function.XacmlFunctionProvider;
import org.xacml4j.v30.types.BooleanValue;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.base.Preconditions;

/**
 * XACML higher order functions
 *
 * @author Giedrius Trumpickas
 */
@XacmlFunctionProvider(description="XACML higher order functions")
public final class HigherOrderFunctions
{
	/** Private constructor for utility class */
	private HigherOrderFunctions() {}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:any-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue anyOf(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParamFunctionReference FunctionReference ref,
			@XacmlFuncParamAnyAttribute Value value,
			@XacmlFuncParamAnyBag BagOfValues bag)
		throws EvaluationException
	{
		for(Value valueFromBag : bag.values()){
			BooleanValue r = ref.invoke(context, value, valueFromBag);
			if(r.value()){
				return XacmlTypes.BOOLEAN.of(true);
			}
		}
		return XacmlTypes.BOOLEAN.of(false);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:all-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue allOf(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParamFunctionReference FunctionReference ref,
			@XacmlFuncParamAnyAttribute Value value,
			@XacmlFuncParamAnyBag BagOfValues bag)
		throws EvaluationException
	{
		for(Value valueFromBag : bag.values()){
			BooleanValue r = ref.invoke(context, value, valueFromBag);
			if(!r.value()){
				return XacmlTypes.BOOLEAN.of(false);
			}
		}
		return XacmlTypes.BOOLEAN.of(true);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:any-of-any")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue anyOfAny(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParamFunctionReference FunctionReference ref,
			@XacmlFuncParamAnyBag BagOfValues a,
			@XacmlFuncParamAnyBag BagOfValues b)
		throws EvaluationException
	{
		for(Value aValue : a.values()){
			for(Value bValue : b.values()){
				BooleanValue r = ref.invoke(context, aValue, bValue);
				if(r.value()){
					return XacmlTypes.BOOLEAN.of(true);
				}
			}
		}
		return XacmlTypes.BOOLEAN.of(false);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:all-of-any")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue allOfAny(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParamFunctionReference FunctionReference ref,
			@XacmlFuncParamAnyBag BagOfValues a,
			@XacmlFuncParamAnyBag BagOfValues b)
		throws EvaluationException
	{
		boolean result = true;
		for(Value v : a.values()){
			result  &= anyOf(context, ref, v, b).value();
			if(!result){
				break;
			}
		}
		return XacmlTypes.BOOLEAN.of(result);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:any-of-all")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue anyOfAll(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParamFunctionReference FunctionReference ref,
			@XacmlFuncParamAnyBag BagOfValues a,
			@XacmlFuncParamAnyBag BagOfValues b)
		throws EvaluationException
	{
		for(Value va : a.values())
		{
			boolean result = allOf(context, ref, va, b).value();
			if(result){
				return XacmlTypes.BOOLEAN.of(true);
			}
		}
		return XacmlTypes.BOOLEAN.of(false);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:all-of-all")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanValue allOfAll(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParamFunctionReference FunctionReference ref,
			@XacmlFuncParamAnyBag BagOfValues a,
			@XacmlFuncParamAnyBag BagOfValues b)
		throws EvaluationException
	{
		for(Value aValue : a.values())
		{
			for(Value bValue : b.values()){
				BooleanValue r = ref.invoke(context, aValue, bValue);
				if(!r.value()){
					return XacmlTypes.BOOLEAN.of(false);
				}
			}
		}
		return XacmlTypes.BOOLEAN.of(true);
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:map")
	@XacmlFuncReturnTypeResolver(resolverClass=MapFunctionResolverValidator.class)
	@XacmlFuncParamValidator(validatorClass=MapFunctionResolverValidator.class)
	public static BagOfValues map(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParamFunctionReference FunctionReference ref,
			@XacmlFuncParamAnyBag BagOfValues bag)
		throws EvaluationException
	{
		Collection<Value> values = new ArrayList<Value>(bag.size());
		for(Value v : bag.values()){
			values.add((Value)ref.invoke(context, v));
		}
		ValueType type = (ValueType)ref.getEvaluatesTo();
		return type.bag()
				.attributes(values)
				.build();
	}

	/**
	 * An implementation of {@link FunctionReturnTypeResolver} and
	 * {@link FunctionParametersValidator} for XACML map higher
	 * order function
	 *
	 * @author Giedrius Trumpickas
	 */
	public static class MapFunctionResolverValidator implements
		FunctionParametersValidator, FunctionReturnTypeResolver
	{
		private final static Logger log = LoggerFactory.getLogger(MapFunctionResolverValidator.class);

		public MapFunctionResolverValidator(){
		}

		@Override
		public ValueTypeInfo resolve(FunctionSpec spec,
		                             List<Expression> arguments)
		{
			Preconditions.checkArgument(arguments != null,
					"Can't resolve function=\"%s\" return type " +
					"dynamically, arguments must be specified", spec.getId());
			Preconditions.checkArgument(arguments.size() == spec.getNumberOfParams(),
					"Can't resolve function=\"%s\" return type " +
					"dynamically, function requires 2 parameters to be specified", spec.getId());
			Expression ref = arguments.get(0);
			Preconditions.checkArgument(ref instanceof FunctionReference,
					"First function argument must be function reference");
			ValueType type = (ValueType)ref.getEvaluatesTo();
			return type.bagType();
		}

		@Override
		public boolean validate(FunctionSpec spec, List<Expression> arguments)
		{
			if(log.isDebugEnabled()){
				log.debug("Validating function=\"{}\" parameters", spec.getId());
			}
			if(arguments == null ||
					arguments.size() != 2){
				return false;
			}
			Expression exp = arguments.get(0);
			if(!(exp instanceof FunctionReference)){
				return false;
			}
			FunctionReference ref = (FunctionReference)exp;
			return ref.getNumberOfParams() == 1;
		}
	}
}
