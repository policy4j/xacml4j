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
		if(arguments.length == 2){
			// The usual predicate of a value and each value of the bag (design D39)
			return BooleanVal.of(DecidingOperands.anyTrue(context, ref, arguments[1 - bagIndex], bag, bagIndex == 0));
		}
		Expression[] parameters = arguments.clone();
		// Combined with "or": a True for a later element wins over an error.
		return BooleanVal.of(DecidingOperands.<Value<?>>anyTrue(bag, valueFromBag -> {
			parameters[bagIndex] = valueFromBag;
			return isTrue(context, ref, parameters);
		}));
	}

	static BooleanVal anyOfPair(org.xacml4j.v30.EvaluationContext context, FunctionReference ref,
			Expression first, Expression second) throws EvaluationException
	{
		requireValidArguments(ref, first, second, true, true);
		if(first instanceof BagOfValues bag){
			return BooleanVal.of(DecidingOperands.anyTrue(context, ref, second, bag, true));
		}
		if(second instanceof BagOfValues bag){
			return BooleanVal.of(DecidingOperands.anyTrue(context, ref, first, bag, false));
		}
		throw new IllegalArgumentException("A single bag argument is required");
	}

	static BooleanVal anyOfSingle(org.xacml4j.v30.EvaluationContext context, FunctionReference ref,
			Expression argument) throws EvaluationException
	{
		requireValidArguments(ref, argument, true, true);
		if(!(argument instanceof BagOfValues)) throw new IllegalArgumentException("A single bag argument is required");
		return BooleanVal.of(single(context, ref, argument, true));
	}

	private static boolean single(org.xacml4j.v30.EvaluationContext context, FunctionReference ref,
			Expression argument, boolean deciding)
	{
		if(!(argument instanceof BagOfValues bag)){
			BooleanVal result = ref.invoke(context, argument);
			return result.get();
		}
		RuntimeException failure = null;
		for(int i = 0; i < bag.size(); i++){
			try{
				BooleanVal result = ref.invoke(context, bag.valueAt(i));
				if(result.get() == deciding) return deciding;
			}catch(RuntimeException e){
				if(failure == null) failure = e;
			}
		}
		if(failure != null) throw failure;
		return !deciding;
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
		if(arguments.length == 2){
			// The usual predicate of a value and each value of the bag (design D39)
			return BooleanVal.of(DecidingOperands.allTrue(context, ref, arguments[1 - bagIndex], bag, bagIndex == 0));
		}
		Expression[] parameters = arguments.clone();
		// Combined with "and": a False for a later element wins over an error.
		return BooleanVal.of(DecidingOperands.<Value<?>>allTrue(bag, valueFromBag -> {
			parameters[bagIndex] = valueFromBag;
			return isTrue(context, ref, parameters);
		}));
	}

	static BooleanVal allOfPair(org.xacml4j.v30.EvaluationContext context, FunctionReference ref,
			Expression first, Expression second) throws EvaluationException
	{
		requireValidArguments(ref, first, second, true, true);
		if(first instanceof BagOfValues bag){
			return BooleanVal.of(DecidingOperands.allTrue(context, ref, second, bag, true));
		}
		if(second instanceof BagOfValues bag){
			return BooleanVal.of(DecidingOperands.allTrue(context, ref, first, bag, false));
		}
		throw new IllegalArgumentException("A single bag argument is required");
	}

	static BooleanVal allOfSingle(org.xacml4j.v30.EvaluationContext context, FunctionReference ref,
			Expression argument) throws EvaluationException
	{
		requireValidArguments(ref, argument, true, true);
		if(!(argument instanceof BagOfValues)) throw new IllegalArgumentException("A single bag argument is required");
		return BooleanVal.of(single(context, ref, argument, false));
	}

	static BooleanVal invokeSingle(org.xacml4j.v30.EvaluationContext context, FunctionReference ref,
			BagOfValues bag, boolean deciding) throws EvaluationException
	{
		return BooleanVal.of(single(context, ref, bag, deciding));
	}

	static BooleanVal invokePair(org.xacml4j.v30.EvaluationContext context, FunctionReference ref,
			Value<?> scalar, BagOfValues bag, boolean bagFirst, boolean deciding) throws EvaluationException
	{
		return BooleanVal.of(deciding ? DecidingOperands.anyTrue(context, ref, scalar, bag, bagFirst)
				: DecidingOperands.allTrue(context, ref, scalar, bag, bagFirst));
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

	static BooleanVal anyOfAnyPair(org.xacml4j.v30.EvaluationContext context, FunctionReference ref,
			Expression first, Expression second) throws EvaluationException
	{
		requireValidArguments(ref, first, second, false, true);
		if(!(first instanceof BagOfValues bag)){
			return BooleanVal.of(anyWithSecond(context, ref, first, second));
		}
		RuntimeException failure = null;
		for(int i = 0; i < bag.size(); i++){
			try{
				if(anyWithSecond(context, ref, bag.valueAt(i), second)) return BooleanVal.of(true);
			}catch(RuntimeException e){
				if(failure == null) failure = e;
			}
		}
		if(failure != null) throw failure;
		return BooleanVal.of(false);
	}

	static BooleanVal anyOfAnySingle(org.xacml4j.v30.EvaluationContext context, FunctionReference ref,
			Expression argument) throws EvaluationException
	{
		requireValidArguments(ref, argument, false, true);
		return BooleanVal.of(single(context, ref, argument, true));
	}

	private static boolean anyWithSecond(org.xacml4j.v30.EvaluationContext context, FunctionReference ref,
			Expression first, Expression second)
	{
		if(!(second instanceof BagOfValues bag)){
			BooleanVal result = ref.invoke(context, first, second);
			return result.get();
		}
		RuntimeException failure = null;
		for(int i = 0; i < bag.size(); i++){
			try{
				BooleanVal result = ref.invoke(context, first, bag.valueAt(i));
				if(result.get()) return true;
			}catch(RuntimeException e){
				if(failure == null) failure = e;
			}
		}
		if(failure != null) throw failure;
		return false;
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:all-of-any")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	@XacmlFuncParamValidator(validatorClass=AnyOfAnyFunctionParametersValidator.class)
	public static BooleanVal allOfAny(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParamFunctionReference FunctionReference ref,
			@XacmlFuncParamAnyBag BagOfValues a,
			@XacmlFuncParamAnyBag BagOfValues b)
		throws EvaluationException
	{
		// The bags are checked against the predicate also when one is empty.
		requireValidArguments(ref, a, b, false, true);
		// any-of for each value of the first bag, combined with "and".
		return BooleanVal.of(allAny(context, ref, a, b, true));
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:any-of-all")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	@XacmlFuncParamValidator(validatorClass=AnyOfAnyFunctionParametersValidator.class)
	public static BooleanVal anyOfAll(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParamFunctionReference FunctionReference ref,
			@XacmlFuncParamAnyBag BagOfValues a,
			@XacmlFuncParamAnyBag BagOfValues b)
		throws EvaluationException
	{
		// The bags are checked against the predicate also when one is empty.
		requireValidArguments(ref, a, b, false, true);
		// any-of for each value of the second bag, combined with "and".
		return BooleanVal.of(allAny(context, ref, b, a, false));
	}

	private static boolean allAny(org.xacml4j.v30.EvaluationContext context, FunctionReference ref,
			BagOfValues outer, BagOfValues inner, boolean outerFirst)
	{
		RuntimeException failure = null;
		for(int i = 0; i < outer.size(); i++){
			try{
				Value<?> value = outer.valueAt(i);
				BooleanVal result = outerFirst ? anyOfPair(context, ref, value, inner)
						: anyOfPair(context, ref, inner, value);
				if(!result.get()) return false;
			}catch(RuntimeException e){
				if(failure == null) failure = e;
			}
		}
		if(failure != null) throw failure;
		return true;
	}

	@XacmlFuncSpec(id="urn:oasis:names:tc:xacml:1.0:function:all-of-all")
	@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
	@XacmlFuncParamValidator(validatorClass=AnyOfAnyFunctionParametersValidator.class)
	public static BooleanVal allOfAll(
			@XacmlEvaluationContextParam org.xacml4j.v30.EvaluationContext context,
			@XacmlFuncParamFunctionReference FunctionReference ref,
			@XacmlFuncParamAnyBag BagOfValues a,
			@XacmlFuncParamAnyBag BagOfValues b)
		throws EvaluationException
	{
		// The bags are checked against the predicate also when one is empty.
		requireValidArguments(ref, a, b, false, true);
		RuntimeException failure = null;
		for(int i = 0; i < a.size(); i++){
			try{
				if(!allWithSecond(context, ref, a.valueAt(i), b)) return BooleanVal.of(false);
			}catch(RuntimeException e){
				if(failure == null) failure = e;
			}
		}
		if(failure != null) throw failure;
		return BooleanVal.of(true);
	}

	private static boolean allWithSecond(org.xacml4j.v30.EvaluationContext context, FunctionReference ref,
			Value<?> first, BagOfValues second)
	{
		RuntimeException failure = null;
		for(int i = 0; i < second.size(); i++){
			try{
				BooleanVal result = ref.invoke(context, first, second.valueAt(i));
				if(!result.get()) return false;
			}catch(RuntimeException e){
				if(failure == null) failure = e;
			}
		}
		if(failure != null) throw failure;
		return true;
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
		for(Value<?> v : bag){
			parameters[bagIndex] = v;
			values.add(ref.invoke(context, parameters));
		}
		return ref.getEvaluatesTo()
				.getValueType()
				.bagBuilder()
				.attributes(values)
				.build();
	}

	static BagOfValues mapPair(org.xacml4j.v30.EvaluationContext context, FunctionReference ref,
			Expression first, Expression second) throws EvaluationException
	{
		requireValidArguments(ref, first, second, true, false);
		boolean bagFirst = first instanceof BagOfValues;
		if(!bagFirst && !(second instanceof BagOfValues)){
			throw new IllegalArgumentException("A single bag argument is required");
		}
		BagOfValues bag = (BagOfValues)(bagFirst ? first : second);
		Collection<Value<?>> values = new ArrayList<>(bag.size());
		for(int i = 0; i < bag.size(); i++){
			Value<?> value = bag.valueAt(i);
			values.add(bagFirst ? ref.invoke(context, value, second)
					: ref.invoke(context, first, value));
		}
		return ref.getEvaluatesTo().getValueType().bagBuilder().attributes(values).build();
	}

	static BagOfValues mapSingle(org.xacml4j.v30.EvaluationContext context, FunctionReference ref,
			Expression argument) throws EvaluationException
	{
		requireValidArguments(ref, argument, true, false);
		if(!(argument instanceof BagOfValues bag)) throw new IllegalArgumentException("A single bag argument is required");
		Collection<Value<?>> values = new ArrayList<>(bag.size());
		for(int i = 0; i < bag.size(); i++) values.add(ref.invoke(context, bag.valueAt(i)));
		return ref.getEvaluatesTo().getValueType().bagBuilder().attributes(values).build();
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
			return DecidingOperands.<Value<?>>anyTrue((BagOfValues)arguments[index], value -> {
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
		if(!validateArguments(ref, arguments, singleBag, booleanResult)){
			throw new EvaluationException(Status.syntaxError().build(), "Invalid higher-order function arguments");
		}
	}

	private static void requireValidArguments(FunctionReference ref, Expression first, Expression second,
			boolean singleBag, boolean booleanResult) throws EvaluationException
	{
		if(!validateArguments(ref, first, second, 2, singleBag, booleanResult)){
			throw new EvaluationException(Status.syntaxError().build(), "Invalid higher-order function arguments");
		}
	}

	private static void requireValidArguments(FunctionReference ref, Expression argument,
			boolean singleBag, boolean booleanResult) throws EvaluationException
	{
		if(!validateArguments(ref, argument, null, 1, singleBag, booleanResult)){
			throw new EvaluationException(Status.syntaxError().build(), "Invalid higher-order function arguments");
		}
	}

	private static boolean validateArguments(FunctionReference ref, Expression first, Expression second,
			int count, boolean singleBag, boolean booleanResult)
	{
		if(ref == null || ref.getEvaluatesTo().isBag()
				|| (booleanResult && !XacmlTypes.BOOLEAN.equals(ref.getEvaluatesTo()))) return false;
		int bags = 0;
		for(int i = 0; i < count; i++){
			Expression argument = i == 0 ? first : second;
			if(argument == null || argument instanceof FunctionReference || argument.getEvaluatesTo() == null) return false;
			ValueExpTypeInfo type = argument.getEvaluatesTo();
			if(type.getValueType() == null) return false;
			if(type.isBag()) bags++;
		}
		if(singleBag && bags != 1) return false;
		try{
			if(takesOneValuePerParameter(ref, count)){
				return ref.getParamSpecAt(0).isValidParamType(first.getEvaluatesTo().getValueType())
						&& (count == 1 || ref.getParamSpecAt(1).isValidParamType(second.getEvaluatesTo().getValueType()));
			}
		}catch(RuntimeException e){
			return false;
		}
		Expression[] primitives = new Expression[count];
		primitives[0] = new PrimitiveTypeExpression(first.getEvaluatesTo().getValueType());
		if(count == 2) primitives[1] = new PrimitiveTypeExpression(second.getEvaluatesTo().getValueType());
		ListIterator<Expression> it = Arrays.asList(primitives).listIterator();
		try{
			for(int i = 0; i < ref.getNumberOfParams(); i++){
				if(!ref.getParamSpecAt(i).validate(it, true)) return false;
			}
			return !it.hasNext();
		}catch(RuntimeException e){
			return false;
		}
	}

	/** Validate the primitive signature after replacing each bag by its element type. */
	private static boolean validateArguments(List<Expression> arguments,
			boolean singleBag, boolean booleanResult)
	{
		if(arguments == null || arguments.size() < 2 || !(arguments.get(0) instanceof FunctionReference)){
			return false;
		}
		return validateArguments((FunctionReference)arguments.get(0),
				arguments.subList(1, arguments.size()).toArray(new Expression[0]),
				singleBag, booleanResult);
	}

	/** Validate the primitive signature after replacing each bag by its element type. */
	private static boolean validateArguments(FunctionReference ref, Expression[] arguments,
			boolean singleBag, boolean booleanResult)
	{
		if(ref == null || arguments == null || arguments.length == 0
				|| ref.getEvaluatesTo().isBag() || (booleanResult && !XacmlTypes.BOOLEAN.equals(ref.getEvaluatesTo()))){
			return false;
		}
		int bags = 0;
		for(int i = 0; i < arguments.length; i++){
			Expression argument = arguments[i];
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
		}
		if(singleBag && bags != 1){
			return false;
		}
		try{
			if(takesOneValuePerParameter(ref, arguments.length)){
				// Each parameter takes the element type of one argument, so the types are compared
				// directly, as the parameters would validate them (design D39)
				for(int i = 0; i < arguments.length; i++){
					if(!ref.getParamSpecAt(i).isValidParamType(arguments[i].getEvaluatesTo().getValueType())){
						return false;
					}
				}
				return true;
			}
		}catch(RuntimeException e){
			return false;
		}
		Expression[] primitives = new Expression[arguments.length];
		for(int i = 0; i < arguments.length; i++){
			primitives[i] = new PrimitiveTypeExpression(arguments[i].getEvaluatesTo().getValueType());
		}
		ListIterator<Expression> it = Arrays.asList(primitives).listIterator();
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

	/**
	 * Whether the predicate has one parameter per argument and each is an engine parameter that
	 * validates its one argument by its type; a parameter of another implementation keeps the full
	 * validation (review r-AG)
	 */
	private static boolean takesOneValuePerParameter(FunctionReference ref, int arguments)
	{
		if(ref.getNumberOfParams() != arguments){
			return false;
		}
		for(int i = 0; i < arguments; i++){
			if(!FunctionSpecBuilder.validatesOneArgumentByType(ref.getParamSpecAt(i))){
				return false;
			}
		}
		return true;
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
