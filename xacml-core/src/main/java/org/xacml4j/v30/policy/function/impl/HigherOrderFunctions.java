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

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ExpressionVisitor;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.ValueExpTypeInfo;
import org.xacml4j.v30.policy.FunctionReference;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.*;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.Value;
import org.xacml4j.v30.types.XacmlTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

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

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:any-of")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:any-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	@XacmlFuncParamValidator(validatorClass=SingleBagFunctionParametersValidator.class)
	public static BooleanVal anyOf(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParamFunctionReference FunctionReference ref,
			@XacmlFuncParamVarArg(typeId="", min=1) Expression... arguments)
		throws EvaluationException
	{
		requireValidArguments(ref, arguments, true, true);
		int bagIndex = singleBagIndex(arguments);
		BagOfValues bag = (BagOfValues)arguments[bagIndex];
		Expression[] parameters = arguments.clone();
		// Combined with "or": a True for a later element wins over an error.
		return BooleanVal.of(DecidingOperands.<Value<?>>anyTrue(bag.values(), valueFromBag -> {
			parameters[bagIndex] = valueFromBag;
			return isTrue(context, ref, parameters);
		}));
	}

	private static boolean isTrue(org.xacml4j.v30.EvaluationContext context,
			FunctionReference ref, Expression... parameters)
	{
		BooleanVal r = ref.invoke(context, parameters);
		return r.get();
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:all-of")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:all-of")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	@XacmlFuncParamValidator(validatorClass=SingleBagFunctionParametersValidator.class)
	public static BooleanVal allOf(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParamFunctionReference FunctionReference ref,
			@XacmlFuncParamVarArg(typeId="", min=1) Expression... arguments)
		throws EvaluationException
	{
		requireValidArguments(ref, arguments, true, true);
		int bagIndex = singleBagIndex(arguments);
		BagOfValues bag = (BagOfValues)arguments[bagIndex];
		Expression[] parameters = arguments.clone();
		// Combined with "and": a False for a later element wins over an error.
		return BooleanVal.of(DecidingOperands.<Value<?>>allTrue(bag.values(), valueFromBag -> {
			parameters[bagIndex] = valueFromBag;
			return isTrue(context, ref, parameters);
		}));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:any-of-any")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:any-of-any")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	@XacmlFuncParamValidator(validatorClass=AnyOfAnyFunctionParametersValidator.class)
	public static BooleanVal anyOfAny(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParamFunctionReference FunctionReference ref,
			@XacmlFuncParamVarArg(typeId="", min=1) Expression... arguments)
		throws EvaluationException
	{
		requireValidArguments(ref, arguments, false, true);
		return BooleanVal.of(anyOfAny(context, ref, arguments, new Expression[arguments.length], 0));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:all-of-any")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal allOfAny(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParamFunctionReference FunctionReference ref,
			@XacmlFuncParamAnyBag BagOfValues a,
			@XacmlFuncParamAnyBag BagOfValues b)
		throws EvaluationException
	{
		// any-of for each value of the first bag, combined with "and".
		return BooleanVal.of(DecidingOperands.<Value<?>>allTrue(a.values(),
				v -> anyOf(context, ref, v, b).get()));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:any-of-all")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal anyOfAll(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParamFunctionReference FunctionReference ref,
			@XacmlFuncParamAnyBag BagOfValues a,
			@XacmlFuncParamAnyBag BagOfValues b)
		throws EvaluationException
	{
		// any-of for each value of the second bag, combined with "and".
		return BooleanVal.of(DecidingOperands.<Value<?>>allTrue(b.values(),
				vb -> anyOf(context, ref, a, vb).get()));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:all-of-all")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	public static BooleanVal allOfAll(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParamFunctionReference FunctionReference ref,
			@XacmlFuncParamAnyBag BagOfValues a,
			@XacmlFuncParamAnyBag BagOfValues b)
		throws EvaluationException
	{
		// Every pair, combined with "and".
		return BooleanVal.of(DecidingOperands.<Value<?>>allTrue(a.values(),
				aValue -> DecidingOperands.<Value<?>>allTrue(b.values(),
						bValue -> isTrue(context, ref, aValue, bValue))));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:3.0:function:map")
	@XacmlLegacyFunc(id="urn:oasis:names:tc:xacml:1.0:function:map")
	@XacmlFuncReturnTypeResolver(resolverClass=MapFunctionResolverValidator.class)
	@XacmlFuncParamValidator(validatorClass=MapFunctionResolverValidator.class)
	public static BagOfValues map(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParamFunctionReference FunctionReference ref,
			@XacmlFuncParamVarArg(typeId="", min=1) Expression... arguments)
		throws EvaluationException
	{
		requireValidArguments(ref, arguments, true, false);
		int bagIndex = singleBagIndex(arguments);
		BagOfValues bag = (BagOfValues)arguments[bagIndex];
		Expression[] parameters = arguments.clone();
		Collection<Value<?>> values = new ArrayList<>(bag.size());
		for(Value<?> v : bag.values()){
			parameters[bagIndex] = v;
			values.add(ref.invoke(context, parameters));
		}
		return ref.getEvaluatesTo()
				.getValueType()
				.bagBuilder()
				.attributes(values)
				.build();
	}

	private static int singleBagIndex(Expression[] arguments)
	{
		for(int i = 0; i < arguments.length; i++){
			if(arguments[i] instanceof BagOfValues){
				return i;
			}
		}
		throw new IllegalArgumentException("A single bag argument is required");
	}

	private static boolean anyOfAny(org.xacml4j.v30.EvaluationContext context,
			FunctionReference ref, Expression[] arguments, Expression[] parameters, int index)
		throws EvaluationException
	{
		if(index == arguments.length){
			return isTrue(context, ref, parameters);
		}
		if(arguments[index] instanceof BagOfValues){
			// Every tuple, combined with "or".
			return DecidingOperands.<Value<?>>anyTrue(((BagOfValues)arguments[index]).values(), value -> {
				parameters[index] = value;
				return anyOfAny(context, ref, arguments, parameters, index + 1);
			});
		}
		parameters[index] = arguments[index];
		return anyOfAny(context, ref, arguments, parameters, index + 1);
	}

	private static void requireValidArguments(FunctionReference ref, Expression[] arguments,
			boolean singleBag, boolean booleanResult) throws EvaluationException
	{
		List<Expression> parameters = new ArrayList<>();
		parameters.add(ref);
		if(arguments != null){
			parameters.addAll(Arrays.asList(arguments));
		}
		if(!validateArguments(parameters, singleBag, booleanResult)){
			throw new EvaluationException(Status.syntaxError().build(), "Invalid higher-order function arguments");
		}
	}

	/** Validate the primitive signature after replacing each bag by its element type. */
	private static boolean validateArguments(List<Expression> arguments,
			boolean singleBag, boolean booleanResult)
	{
		if(arguments == null || arguments.size() < 2 || !(arguments.get(0) instanceof FunctionReference)){
			return false;
		}
		FunctionReference ref = (FunctionReference)arguments.get(0);
		if(ref.getEvaluatesTo().isBag() || (booleanResult && !XacmlTypes.BOOLEAN.equals(ref.getEvaluatesTo()))){
			return false;
		}
		int bags = 0;
		List<Expression> primitives = new ArrayList<>();
		for(int i = 1; i < arguments.size(); i++){
			Expression argument = arguments.get(i);
			if(argument == null || argument instanceof FunctionReference || argument.getEvaluatesTo() == null){
				return false;
			}
			ValueExpTypeInfo type = argument.getEvaluatesTo();
			if(type.getValueType() == null){
				return false;
			}
			if(type.isBag()){
				bags++;
			}
			primitives.add(new PrimitiveTypeExpression(type.getValueType()));
		}
		if(singleBag && bags != 1){
			return false;
		}
		ListIterator<Expression> it = primitives.listIterator();
		try{
			for(int i = 0; i < ref.getNumberOfParams(); i++){
				if(!ref.getParamSpecAt(i).validate(it, true)){
					return false;
				}
			}
			return !it.hasNext();
		}catch(RuntimeException e){
			return false;
		}
	}

	private static class PrimitiveTypeExpression implements Expression
	{
		private final ValueExpTypeInfo type;

		PrimitiveTypeExpression(ValueExpTypeInfo type){
			this.type = type;
		}

		@Override
		public ValueExpTypeInfo getEvaluatesTo(){
			return type;
		}

		@Override
		public <T extends Expression> T evaluate(org.xacml4j.v30.EvaluationContext context){
			throw new IllegalStateException("A validation-only expression cannot be evaluated");
		}

		@Override
		public void accept(ExpressionVisitor visitor){
		}
	}

	public static class SingleBagFunctionParametersValidator implements FunctionParametersValidator
	{
		@Override
		public boolean validate(FunctionSpec spec, List<Expression> arguments){
			return validateArguments(arguments, true, true);
		}
	}

	public static class AnyOfAnyFunctionParametersValidator implements FunctionParametersValidator
	{
		@Override
		public boolean validate(FunctionSpec spec, List<Expression> arguments){
			return validateArguments(arguments, false, true);
		}
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
		public ValueExpTypeInfo resolve(FunctionSpec spec,
		                                List<Expression> arguments)
		{
			Preconditions.checkArgument(arguments != null,
					"Can't resolve function=\"%s\" return type " +
					"dynamically, arguments must be specified", spec.getId());
			Preconditions.checkArgument(!arguments.isEmpty(),
					"Can't resolve function=\"%s\" return type " +
					"dynamically, a function reference must be specified", spec.getId());
			Expression ref = arguments.get(0);
			Preconditions.checkArgument(ref instanceof FunctionReference,
					"First function argument must be function reference");
			if(ref.getEvaluatesTo().isBag()){
				spec.validateParametersAndThrow(arguments);
			}
			return ref.getEvaluatesTo().getValueType().bagType();
		}

		@Override
		public boolean validate(FunctionSpec spec, List<Expression> arguments)
		{
			if(log.isDebugEnabled()){
				log.debug("Validating function=\"{}\" parameters", spec.getId());
			}
			return validateArguments(arguments, true, false);
		}
	}
}
