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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.ValueExpression;
import org.xacml4j.v30.ValueTypeInfo;
import org.xacml4j.v30.policy.FunctionInvocationException;
import org.xacml4j.v30.policy.FunctionParamSpec;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.PolicySyntaxException;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterators;

/**
 * A builder for building {@link FunctionSpec} instances
 * from various metadata sources, the assumption is made that parameter types
 * follow in mandatory, optional, variadic order and parameter type groups
 * do not interleave.
 *
 * @author Giedrius Trumpickas
 */
public final class FunctionSpecBuilder {
	private static final Logger LOG = LoggerFactory.getLogger(FunctionSpecBuilder.class);

	private String functionId;
	private String shortId;
	private String legacyId;
	private List<FunctionParamSpec> paramSpec;
	private boolean hadVarArg = false;
	private boolean hadOptional = false;
	private boolean lazyArgumentEvaluation;

	private FunctionSpecBuilder(String functionId) {
		this(functionId, null);
	}

	private FunctionSpecBuilder(String functionId, String legacyId) {
		this(functionId, functionId, legacyId);
	}

	private FunctionSpecBuilder(
			String functionId, String shortId,
			String legacyId) {
		Preconditions.checkNotNull(functionId);
		this.functionId = Objects.requireNonNull(functionId, "functionId");
		this.shortId = getShortId(functionId, shortId);
		this.legacyId = legacyId;
		this.paramSpec = new LinkedList<>();
	}

	public static String getShortId(String id, String shortId) {
		if (!Strings.isNullOrEmpty(shortId)) {
			return shortId;
		}
		List<String> components = Splitter.on(":").splitToList(id);
		if (components.size() > 1) {
			return Joiner.on(":")
			             .join(
					             components.get(components.size() - 2),
					             components.get(components.size() - 1));
		}
		return components.get(0);
	}

	public static FunctionSpecBuilder builder(String functionId) {
		return builder(functionId, null);
	}

	public static FunctionSpecBuilder builder(String functionId, String legacyId) {
		return new FunctionSpecBuilder(functionId, legacyId);
	}

	public FunctionSpecBuilder funcRefParam() {
		paramSpec.add(new FunctionParamFuncReferenceSpec());
		return this;
	}

	public FunctionSpecBuilder shortId(String id) {
		this.shortId = id;
		return this;
	}

	public FunctionSpecBuilder param(ValueTypeInfo type) {
		return param(type, null, false);
	}

	public FunctionSpecBuilder optional(ValueTypeInfo type) {
		return param(type, null, true);
	}

	public FunctionSpecBuilder optional(ValueTypeInfo type, ValueExpression defaultValue) {
		return param(type, defaultValue, true);
	}

	public FunctionSpecBuilder param(ValueTypeInfo type, ValueExpression defaultValue, boolean optional) {
		Preconditions.checkNotNull(type);
		FunctionParamSpec spec = new FunctionParamValueTypeSpec(type, defaultValue, optional);
		if (defaultValue != null && !optional) {
			throw PolicySyntaxException
					.invalidFunctionParam(functionId, spec, paramSpec.size() - 1,
					                      "Parameter can't have default value and be mandatory");
		}
		if (defaultValue != null && !type.equals(defaultValue.getEvaluatesTo())) {
			throw PolicySyntaxException
					.invalidFunctionParam(functionId, spec, paramSpec.size() - 1,
					                      String.format("Parameter default value type=\"%s\" but param requires=\"%s\"",
					                                    type.getDataType(), defaultValue.getType()));
		}
		if (hadVarArg) {
			throw PolicySyntaxException
					.invalidFunctionParam(functionId, spec, paramSpec.size() - 1,
					                      "Can't add parameter after variadic parameter");
		}
		hadOptional = defaultValue != null || optional;
		if (defaultValue != null && !optional) {
			throw PolicySyntaxException
					.invalidFunctionParameter(
							functionId, spec, paramSpec.size(),
							"parameter can not have default value and be required at the same time");
		}
		if (paramSpec.size() == 0 &&
				defaultValue != null) {
			throw PolicySyntaxException
					.invalidFunctionParam(functionId, spec, paramSpec.size(),
					                      "first parameter can not have default value");
		}
		paramSpec.add(new FunctionParamValueTypeSpec(type, defaultValue, optional));
		return this;
	}

	public FunctionSpecBuilder lazyArgEval() {
		lazyArgumentEvaluation = true;
		return this;
	}

	public FunctionSpecBuilder varArg(ValueTypeInfo type, int min, int max) {
		Preconditions.checkNotNull(type);
		Preconditions.checkArgument(min >= 0 && max > 0);
		Preconditions.checkArgument(max > min);
		Preconditions.checkArgument(max - min >= 1, "Max and min should be different at least by 1");
		if (hadVarArg) {
			throw new SyntaxException("Can't add vararg " +
					                          "parameter after vararg parameter");
		}
		hadVarArg = true;
		paramSpec.add(new FunctionParamValueTypeSequenceSpec(min, max, type));
		return this;
	}

	public FunctionSpecBuilder anyBag() {
		FunctionParamSpec spec = new FunctionParamAnyBagSpec();
		if (hadVarArg) {
			throw PolicySyntaxException
					.invalidFunctionParameter(functionId, spec);
		}
		paramSpec.add(spec);
		return this;
	}

	public FunctionSpecBuilder anyAttribute() {
		paramSpec.add(new FunctionParamAnyValueSpec());
		return this;
	}

	public FunctionSpec build(
			FunctionReturnTypeResolver returnType,
			FunctionInvocation invocation) {
		return new FunctionSpecImpl(functionId,
		                            legacyId, paramSpec, returnType, invocation, lazyArgumentEvaluation);
	}


	public FunctionSpec build(
			FunctionReturnTypeResolver returnType,
			FunctionParametersValidator validator,
			FunctionInvocation invocation) {
		return new FunctionSpecImpl(
				functionId,
				shortId,
				legacyId,
				paramSpec,
				returnType,
				invocation,
				validator,
				lazyArgumentEvaluation);
	}

	public FunctionSpec build(
			ValueTypeInfo returnType,
			FunctionInvocation invocation) {
		return build(
				new StaticFunctionReturnTypeResolver(returnType),
				invocation);
	}

	public FunctionSpec build(
			ValueTypeInfo returnType,
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
	static final class FunctionSpecImpl implements FunctionSpec {
		private final static Logger LOG = LoggerFactory.getLogger(FunctionSpecImpl.class);

		private String functionId;
		private String shortId;
		private Optional<String> legacyId;
		private List<FunctionParamSpec> parameters = new LinkedList<FunctionParamSpec>();
		private boolean evaluateParameters;

		private FunctionInvocation invocation;
		private FunctionReturnTypeResolver resolver;
		private FunctionParametersValidator validator;
		private FunctionCategory functionCategory;

		/**
		 * Constructs function spec with given function
		 * identifier and parameters
		 *
		 * @param functionId         a function identifier
		 * @param legacyId           a legacy identifier
		 * @param params             a function parameters spec
		 * @param resolver           a function return type resolver
		 * @param invocation         a function implementation
		 * @param evaluateParameters a flag indicating
		 *                           if function parameters needs to be evaluated
		 *                           before passing them to the function
		 */
		FunctionSpecImpl(
				String functionId,
				String shortId,
				String legacyId,
				List<FunctionParamSpec> params,
				FunctionReturnTypeResolver resolver,
				FunctionInvocation invocation,
				FunctionParametersValidator validator,
				boolean evaluateParameters) {
			this.functionId = Preconditions.checkNotNull(functionId);
			this.functionCategory = FunctionCategory.fromString(functionId);
			this.parameters.addAll(Preconditions.checkNotNull(params));
			this.resolver = Preconditions.checkNotNull(resolver);
			this.validator = validator;
			this.invocation = Preconditions.checkNotNull(invocation);
			this.evaluateParameters = evaluateParameters;
			this.shortId = Objects.requireNonNull(FunctionSpecBuilder.getShortId(functionId, shortId));
			this.legacyId = Optional.ofNullable(legacyId);
		}

		public FunctionSpecImpl(
				String functionId,
				String legacyId,
				List<FunctionParamSpec> params,
				FunctionReturnTypeResolver resolver,
				FunctionInvocation invocation,
				boolean evaluateParameters) {
			this(functionId, functionId, legacyId, params,
			     resolver, invocation, null, evaluateParameters);
		}

		@Override
		public String getId() {
			return functionId;
		}

		@Override
		public String getShortId() {
			return shortId;
		}

		public FunctionCategory getCategory() {
			return functionCategory;
		}

		@Override
		public Optional<String> getLegacyId() {
			return legacyId;
		}

		@Override
		public final FunctionParamSpec getParamSpecAt(int index) {
			return parameters.get(index);
		}

		@Override
		public boolean isRequiresLazyParamEval() {
			return evaluateParameters;
		}

		@Override
		public boolean isVariadic() {
			return !parameters.isEmpty() && parameters.get(parameters.size() - 1).isVariadic();
		}

		@Override
		public int getNumberOfParams() {
			return parameters.size();
		}

		@Override
		public ValueTypeInfo resolveReturnType(List<Expression> arguments) {
			return resolver.resolve(this, arguments);
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends ValueExpression> T invokeWithList(
				EvaluationContext context,
				List<Expression> arguments) throws EvaluationException {

			try {
				List<Expression> normalizedArgs = normalize(arguments);
				LOG.debug("functionId={} normalizedArgs={}", functionId, normalizedArgs);
				if (context.isValidateFuncParamsAtRuntime()) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Validating " +
								          "functionId=\"{}\" parameters=\"{}\"", functionId, parameters);
					}
					if (!doValidateNormalizedParameters(normalizedArgs)) {
						LOG.debug("Failed to validate function=\"{}\" params=\"{}\"", functionId, normalizedArgs);
						throw new FunctionInvocationException(this,
						                                      "Failed to validate function=\"%s\" parameters=\"%s\"",
						                                      functionId, normalizedArgs);
					}
				}
				if (LOG.isDebugEnabled()) {
					LOG.debug("Invoking function=\"{}\" with length={} params=\"{}\"",
					          functionId, normalizedArgs.size(), normalizedArgs);
				}
				List<Expression> params = evaluateParameters ? normalizedArgs : evaluate(context, normalizedArgs);
				LOG.debug("Function=\"{}\" params=\"{}\"", functionId, params);
				T result = (T) invocation.invoke(this, context, params);
				if (LOG.isDebugEnabled()) {
					LOG.debug("Function=\"{}\" " +
							          "invocation result=\"{}\"", functionId, result);
				}
				return result;
			} catch (EvaluationException e) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Failed to invoke function", e);
				}
				throw e;
			} catch (Exception e) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Failed to invoke function", e);
				}
				throw new FunctionInvocationException(this, e,
				                                      "Failed to invoke function=\"%s\"", functionId);
			}
		}

		@Override
		public void validateParametersAndThrow(List<Expression> arguments) throws SyntaxException {
			ListIterator<FunctionParamSpec> it = parameters.listIterator();
			List<Expression> normalizedParameters = normalize(arguments);
			ListIterator<Expression> expIt = normalizedParameters.listIterator();
			if (LOG.isDebugEnabled()) {
				LOG.debug("Function=\"{}\" normalized parameters=\"{}\"",
				          functionId, normalizedParameters);
			}
			while (it.hasNext()) {
				FunctionParamSpec p = it.next();
				if(!p.validate(expIt)){
					throw PolicySyntaxException.invalidFunctionParam(functionId, p, expIt.previousIndex(),
					                                                 "invalid parameter");
				}
				if ((!it.hasNext() &&
						expIt.hasNext())) {
					throw PolicySyntaxException
							.invalidFunctionParameter(
									functionId, p, expIt.nextIndex(),
									String.format("expecting less function params, unexpected expression=\"%s\"",
									              expIt.next()));
				}

				if ((it.hasNext() &&
						!expIt.hasNext())) {
					throw PolicySyntaxException.invalidFunctionParam(functionId,
					                                                 p, expIt.previousIndex(), "more arguments expected");
				}
			}

			if (!validateAdditional(arguments)) {
				throw PolicySyntaxException.invalidFunction(functionId, "failed additional validation");
			}
		}

		@Override
		public boolean validateParameters(List<Expression> arguments) {
			List<Expression> normalizedParameters = normalize(arguments);
			return doValidateNormalizedParameters(normalizedParameters);
		}

		private boolean doValidateNormalizedParameters(List<Expression> normalizedParameters) {
			ListIterator<FunctionParamSpec> it = parameters.listIterator();
			ListIterator<Expression> expIt = normalizedParameters.listIterator();
			if (LOG.isDebugEnabled()) {
				LOG.debug("Function=\"{}\" normalized parameters=\"{}\"",
				          functionId, normalizedParameters);
			}
			while (it.hasNext()) {
				FunctionParamSpec p = it.next();
				LOG.debug("Validating param=\"{}\"", p);
				if (!p.validate(expIt)) {
					return false;
				}

				if ((it.hasNext() &&
						!expIt.hasNext())) {
					FunctionParamSpec spec = it.next();
					if (!(spec.isOptional() || spec.isVariadic())) {
						if (LOG.isDebugEnabled()) {
							LOG.debug("Additional arguments are " +
									          "expected for function=\"{}\"", functionId);
						}
						return false;
					}
				}
				if (!it.hasNext() &&
						expIt.hasNext()) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("To many arguments=\"{}\", " +
								          "found for function=\"{}\"",
						          normalizedParameters, functionId);
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
				// Iterator over actual params
				ListIterator<Expression> actualParameterIterator,
				// Iterator over formal params
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
					optionalParamValue = formalParameterSpec
							.getDefaultValue().orElse(null);
				}
				FunctionSpecBuilder.LOG.debug("Formal optional param=\"{}\" actual param value={}", formalParameterSpec, optionalParamValue);
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
				FunctionSpecBuilder.LOG.debug("Formal variadic param=\"{}\" actual param value={}", formalParameterSpec, variadicParamValue);
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
		 * @param context   an evaluation context
		 * @param arguments function invocation arguments
		 *                  parameters
		 * @return an array of evaluated parameters
		 * @throws EvaluationException if an evaluation
		 *                             error occurs
		 */
		private List<Expression> evaluate(EvaluationContext context, List<Expression> arguments)
				throws EvaluationException {
			List<Expression> eval = new ArrayList<Expression>(arguments.size());
			for (Expression exp : arguments) {
				eval.add((exp == null) ? null : exp.evaluate(context));
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
		private boolean validateAdditional(List<Expression> arguments) {
			return (validator == null) || validator.validate(this, arguments);
		}

		@Override
		public String toString() {
			return MoreObjects.toStringHelper(this)
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
			if (((obj == null) ||
					!(obj instanceof FunctionSpecImpl))) {
				return false;
			}
			FunctionSpec functionSpec = (FunctionSpec) obj;
			return functionId.equals(functionSpec.getId());
		}
	}

}
