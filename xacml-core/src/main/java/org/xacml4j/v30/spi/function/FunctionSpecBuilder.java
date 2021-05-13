package org.xacml4j.v30.spi.function;

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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ValueExpression;
import org.xacml4j.v30.ValueType;
import org.xacml4j.v30.XacmlSyntaxException;
import org.xacml4j.v30.pdp.FunctionInvocationException;
import org.xacml4j.v30.pdp.FunctionParamSpec;
import org.xacml4j.v30.pdp.FunctionSpec;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;

public final class FunctionSpecBuilder
{
	private final String functionId;
	private final String legacyId;
	private final List<FunctionParamSpec> paramSpec;
	private boolean hadVarArg = false;
	private boolean hadOptional = false;
	private boolean lazyArgumentEvaluation;

	private FunctionSpecBuilder(String functionId){
		this(functionId, null);
	}

	private FunctionSpecBuilder(String functionId, String legacyId){
		Preconditions.checkNotNull(functionId);
		this.functionId = functionId;
		this.legacyId = legacyId;
		paramSpec = new LinkedList<FunctionParamSpec>();
	}


	public static FunctionSpecBuilder  builder(String functionId){
		return builder(functionId, null);
	}

	public static FunctionSpecBuilder  builder(String functionId, String legacyId){
		return new FunctionSpecBuilder(functionId, legacyId);
	}

	public FunctionSpecBuilder funcRefParam()
	{
		paramSpec.add(new FunctionParamFuncReferenceSpec());
		return this;
	}

	public FunctionSpecBuilder param(ValueType type){
		return param(type, null, false);
	}

	public FunctionSpecBuilder optional(ValueType type){
		return param(type, null, true);
	}

	public FunctionSpecBuilder optional(ValueType type, ValueExpression defaultValue){
		return param(type, defaultValue, true);
	}

 	public FunctionSpecBuilder param(ValueType type, ValueExpression defaultValue, boolean optional){
		Preconditions.checkNotNull(type);
		if(defaultValue != null && !optional){
			throw new XacmlSyntaxException(
					"Parameter can't have default value and be mandatory");
		}
		if(hadVarArg){
			throw new XacmlSyntaxException(
					"Can't add parameter after variadic parameter");
		}
		if(defaultValue != null){
			Preconditions.checkArgument(type.equals(defaultValue.getEvaluatesTo()));
		}
		hadOptional = defaultValue != null || optional;
		if(defaultValue != null && !optional){
			throw new XacmlSyntaxException(
					"Function=\"%s\" can not have default " +
					"value and be required at the same time",
					functionId);
		}
		if(paramSpec.size() == 0 &&
				defaultValue != null){
			throw new XacmlSyntaxException(
					"First parameter function=\"%s\" can not have default value",
					functionId);
		}
		paramSpec.add(new FunctionParamValueTypeSpec(type, defaultValue, optional));
		return this;
	}

	public FunctionSpecBuilder lazyArgEval(){
		lazyArgumentEvaluation = true;
		return this;
	}

	public FunctionSpecBuilder varArg(ValueType type, int min, int max){
		Preconditions.checkNotNull(type);
		Preconditions.checkArgument(min >= 0 && max > 0);
		Preconditions.checkArgument(max > min);
		Preconditions.checkArgument(max - min >= 1, "Max and min should be different at least by 1");
		if(hadVarArg){
			throw new XacmlSyntaxException("Can't add vararg " +
					"parameter after vararg parameter");
		}
		hadVarArg = true;
		paramSpec.add(new FunctionParamValueTypeSequenceSpec(min, max, type));
		return this;
	}

	public FunctionSpecBuilder anyBag() {
		paramSpec.add(new FunctionParamAnyBagSpec());
		return this;
	}

	public FunctionSpecBuilder anyAttribute() {
		paramSpec.add(new FunctionParamAnyAttributeSpec());
		return this;
	}

	public FunctionSpec build(FunctionReturnTypeResolver returnType,
			FunctionInvocation invocation) {
		return new FunctionSpecImpl(functionId,
				legacyId,paramSpec, returnType, invocation, lazyArgumentEvaluation);
	}



	public FunctionSpec build(FunctionReturnTypeResolver returnType,
			FunctionParametersValidator validator,
			FunctionInvocation invocation) {
		return new FunctionSpecImpl(functionId,
				legacyId,
				paramSpec,
				returnType,
				invocation,
				validator,
				lazyArgumentEvaluation);
	}

	public FunctionSpec build(ValueType returnType,
			FunctionInvocation invocation) {
		return build(
				new StaticFunctionReturnTypeResolver(returnType),
				invocation);
	}

	public FunctionSpec build(ValueType returnType,
			FunctionParametersValidator validator,
			FunctionInvocation invocation) {
		return build(
				new StaticFunctionReturnTypeResolver(returnType),
				validator,
				invocation);
	}

	/**
	 * A XACML function specification implementation
	 */
	static final class FunctionSpecImpl implements FunctionSpec
	{
		private final static Logger log = LoggerFactory.getLogger(FunctionSpecImpl.class);

		private final String functionId;
		private final String legacyId;
		private final List<FunctionParamSpec> parameters = new LinkedList<FunctionParamSpec>();
		private boolean evaluateParameters = false;

		private final FunctionInvocation invocation;
		private final FunctionReturnTypeResolver resolver;
		private final FunctionParametersValidator validator;

		/**
		 * Constructs function spec with given function
		 * identifier and parameters
		 *
		 * @param functionId a function identifier
		 * @param legacyId a legacy identifier
		 * @param params a function parameters spec
		 * @param resolver a function return type resolver
		 * @param invocation a function implementation
		 * @param evaluateParameters a flag indicating
		 * if function parameters needs to be evaluated
		 * before passing them to the function
		 */
		public FunctionSpecImpl(
				String functionId,
				String legacyId,
				List<FunctionParamSpec> params,
				FunctionReturnTypeResolver resolver,
				FunctionInvocation invocation,
				FunctionParametersValidator validator,
				boolean evaluateParameters){
			this.functionId = Preconditions.checkNotNull(functionId);
			parameters.addAll(Preconditions.checkNotNull(params));
			this.resolver = Preconditions.checkNotNull(resolver);
			this.validator = validator;
			this.invocation = Preconditions.checkNotNull(invocation);
			this.evaluateParameters = evaluateParameters;
			this.legacyId = legacyId;
		}

		public FunctionSpecImpl(
				String functionId,
				String legacyId,
				List<FunctionParamSpec> params,
				FunctionReturnTypeResolver resolver,
				FunctionInvocation invocation,
				boolean evaluateParameters){
			this(functionId, legacyId, params, resolver, invocation, null, evaluateParameters);
		}

		public FunctionSpecImpl(
				String functionId,
				List<FunctionParamSpec> params,
				FunctionReturnTypeResolver resolver,
				FunctionInvocation invocation,
				boolean lazyParamEval){
			this(functionId, null, params, resolver, invocation, null, lazyParamEval);
		}

		@Override
		public  String getId(){
			return functionId;
		}

		@Override
		public String getLegacyId() {
			return legacyId;
		}

		@Override
		public final FunctionParamSpec getParamSpecAt(int index){
			return parameters.get(index);
		}

		@Override
		public boolean isRequiresLazyParamEval() {
			return evaluateParameters;
		}

		@Override
		public boolean isVariadic(){
			return parameters.isEmpty()?false:parameters.get(parameters.size() - 1).isVariadic();
		}

		@Override
		public  int getNumberOfParams(){
			return parameters.size();
		}

		@Override
		public ValueType resolveReturnType(List<Expression> arguments) {
			return resolver.resolve(this, arguments);
		}

		@Override
		public <T extends ValueExpression> T invoke(EvaluationContext context,
				Expression ...arguments) throws EvaluationException {
			return this.invoke(context, Arrays.asList(arguments));
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends ValueExpression> T invoke(EvaluationContext context,
				List<Expression> arguments) throws EvaluationException {

			try
			{
				List<Expression> normalizedArgs = normalize(arguments);
				if(context.isValidateFuncParamsAtRuntime()){
					if(log.isDebugEnabled()){
						log.debug("Validating " +
								"function=\"{}\" parameters", functionId);
					}
					if(!doValidateNormalizedParameters(normalizedArgs)){
						throw new FunctionInvocationException(this,
								"Failed to validate function=\"%s\" parameters=\"%s\"",
								functionId, normalizedArgs);
					}
				}
				if(log.isDebugEnabled()){
					log.debug("Invoking function=\"{}\" with params=\"{}\"",
							functionId, normalizedArgs);
				}
				T result = (T)invocation.invoke(this, context,
						evaluateParameters ?normalizedArgs:evaluate(context, normalizedArgs));
				if(log.isDebugEnabled()){
					log.debug("Function=\"{}\" " +
							"invocation result=\"{}\"", functionId, result);
				}
				return result;
			}
			catch(EvaluationException e){
				throw e;
			}
			catch(Exception e){
				if(log.isDebugEnabled()){
					log.debug("Failed to invoke function", e);
				}
				throw new FunctionInvocationException(this, e,
						"Failed to invoke function=\"%s\"", functionId);
			}
		}

		@Override
		public void validateParametersAndThrow(List<Expression> arguments) throws XacmlSyntaxException
		{
			ListIterator<FunctionParamSpec> it = parameters.listIterator();
			List<Expression> normalizedParameters = normalize(arguments);
			ListIterator<Expression> expIt = normalizedParameters.listIterator();
			if(log.isDebugEnabled()){
				log.debug("Function=\"{}\" normalized parameters=\"{}\"",
						functionId, normalizedParameters);
			}
			while(it.hasNext())
			{
				FunctionParamSpec p = it.next();
				if(!p.validate(expIt)){
					throw new XacmlSyntaxException(
							"Expression at index=\"%d\", " +
							"can't be used as function=\"%s\" parameter",
							expIt.nextIndex() - 1, functionId);
				}
				if((!it.hasNext() &&
						expIt.hasNext())){
					throw new XacmlSyntaxException(
							"Expression at index=\"%d\", " +
							"can't be used as function=\"%s\" parameter, too many arguments",
							expIt.nextIndex() - 1, functionId);
				}

				if((it.hasNext() &&
						!expIt.hasNext())){
					throw new XacmlSyntaxException(
							"More arguments expected for function=\"%s\"", functionId);
				}
			}
			if(!validateAdditional(arguments)){
				throw new XacmlSyntaxException("Failed addition validation");
			}
		}

		@Override
		public boolean validateParameters(List<Expression> arguments)
		{
			List<Expression> normalizedParameters = normalize(arguments);
			return doValidateNormalizedParameters(normalizedParameters);
		}

		private boolean doValidateNormalizedParameters(List<Expression> normalizedParameters) {
			ListIterator<FunctionParamSpec> it = parameters.listIterator();
			ListIterator<Expression> expIt = normalizedParameters.listIterator();
			if(log.isDebugEnabled()){
				log.debug("Function=\"{}\" normalized parameters=\"{}\"",
						functionId, normalizedParameters);
			}
			while(it.hasNext())
			{
				FunctionParamSpec p = it.next();
				if(!p.validate(expIt)){
					return false;
				}
				if((it.hasNext() &&
						!expIt.hasNext())){
					if(log.isDebugEnabled()){
						log.debug("Additional arguments are " +
								"expected for function=\"{}\"",  functionId);
					}
					return false;
				}
				if(!it.hasNext() &&
						expIt.hasNext()){
					if(log.isDebugEnabled()){
						log.debug("To many arguments=\"{}\", " +
								"found for function=\"{}\"",
								normalizedParameters,  functionId);
					}
					return false;
				}
			}
			return validateAdditional(normalizedParameters);
		}

		/**
		 * Normalizes function parameter list
		 *
		 * @param actualParameters a list of function parameters
		 * @return a normalized list of function parameters
		 */
		private List<Expression> normalize(List<Expression> actualParameters) {
			ListIterator<Expression> actualParameterIterator = actualParameters.listIterator();
			ListIterator<FunctionParamSpec> formalParameterIterator = parameters.listIterator();

			// the assumption is made that parameter types
			// follow in mandatory, optional, variadic order
			// and parameter type groups do not interleave
			List<Expression> normalizedParams = new LinkedList<Expression>();
			normalizedParams = normalizeMandatoryParameters(actualParameterIterator,
					formalParameterIterator, normalizedParams);
			normalizedParams = normalizeOptionalParameters(actualParameterIterator,
					formalParameterIterator, normalizedParams);
			normalizedParams = normalizeVariadicParameters(actualParameterIterator,
					formalParameterIterator, normalizedParams);

			// Add rest of the format parameters to normalized list
			Iterators.addAll(normalizedParams, actualParameterIterator);

			return normalizedParams;
		}

		private List<Expression> normalizeMandatoryParameters(
				ListIterator<Expression> actualParameterIterator,
				ListIterator<FunctionParamSpec> formalParameterIterator,
				List<Expression> normalizedParamBuilder) {
			while (formalParameterIterator.hasNext()) {
				FunctionParamSpec functionParamSpec = formalParameterIterator.next();
				if (functionParamSpec.isOptional() || functionParamSpec.isVariadic()) {
					formalParameterIterator.previous();
					return normalizedParamBuilder;
				}
				if (actualParameterIterator.hasNext()) {
					normalizedParamBuilder.add(actualParameterIterator.next());
				}
			}

			return normalizedParamBuilder;
		}

		private List<Expression> normalizeOptionalParameters(
				ListIterator<Expression> actualParameterIterator,
				ListIterator<FunctionParamSpec> formalParameterIterator,
				List<Expression> normalizedParamBuilder) {
			while (formalParameterIterator.hasNext()) {
				FunctionParamSpec formalParameterSpec = formalParameterIterator.next();
				if (!formalParameterSpec.isOptional()) {
					formalParameterIterator.previous();
					return normalizedParamBuilder;
				}

				Expression optionalParamValue = null;
				if (actualParameterIterator.hasNext()) {
					optionalParamValue = actualParameterIterator.next();
				}

				if (optionalParamValue == null) {
					optionalParamValue = formalParameterSpec.getDefaultValue();
				}

				normalizedParamBuilder.add(optionalParamValue);
			}

			return normalizedParamBuilder;
		}

		private List<Expression> normalizeVariadicParameters(
				ListIterator<Expression> actualParameterIterator,
				ListIterator<FunctionParamSpec> formalParameterIterator,
				List<Expression> normalizedParamBuilder) {
			while (formalParameterIterator.hasNext()) {
				FunctionParamSpec formalParameterSpec = formalParameterIterator.next();
				if (!formalParameterSpec.isVariadic()) {
					formalParameterIterator.previous();
					return normalizedParamBuilder;
				}
				Expression variadicParamValue = null;
				if (actualParameterIterator.hasNext()) {
					variadicParamValue = actualParameterIterator.next();
				}
				if (variadicParamValue == null) {
					variadicParamValue = null;
				}

				normalizedParamBuilder.add(variadicParamValue);
			}
			return normalizedParamBuilder;
		}

		/**
		 * Evaluates given array of function parameters
		 *
		 * @param context an evaluation context
		 * @param arguments function invocation arguments
		 * parameters
		 * @return an array of evaluated parameters
		 * @throws EvaluationException if an evaluation
		 * error occurs
		 */
		private List<Expression> evaluate(EvaluationContext context, List<Expression> arguments)
			throws EvaluationException
		{
			List<Expression> eval = new ArrayList<Expression>(arguments.size());
			for(Expression exp : arguments){
				eval.add((exp == null)?null:exp.evaluate(context));
			}
			return eval;
		}

		/**
		 * Additional function parameter validation function
		 *
		 * @param arguments an array of additional function arguments
		 * @return {@code true} if a given parameter is valid
		 * according specification
		 */
		private boolean validateAdditional(List<Expression> arguments){
			return (validator == null)?true:validator.validate(this, arguments);
		}

		@Override
		public String toString(){
			return Objects.toStringHelper(this)
					.add("functionId", functionId)
					.add("legacyId", legacyId)
					.add("evaluateParams", evaluateParameters)
					.add("params", parameters)
					.toString();
		}

		@Override
		public int hashCode() {
			return functionId.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if(!(obj instanceof FunctionSpecImpl)) {
				return false;
			}
			return functionId.equals(((FunctionSpecImpl) obj).functionId);
		}
	}

}
