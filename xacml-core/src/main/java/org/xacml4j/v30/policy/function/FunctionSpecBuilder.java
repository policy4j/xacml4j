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
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.BagStorage;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.ValueExpTypeInfo;
import org.xacml4j.v30.policy.FunctionInvocationException;
import org.xacml4j.v30.policy.FunctionParamSpec;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.PolicySyntaxException;
import org.xacml4j.v30.policy.function.impl.DefaultFunctionInvocation;
import org.xacml4j.v30.types.Value;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

/**
 * A builder for building {@link FunctionSpec} instances
 * from various metadata sources, the assumption is made that parameter types
 * follow in mandatory, optional, variadic order and parameter type groups
 * do not interleave.
 *
 * @author Giedrius Trumpickas
 */
public final class FunctionSpecBuilder {
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
		this.paramSpec = new ArrayList<>();
	}

	public static String getShortId(String id, String shortId) {
		if (!Strings.isNullOrEmpty(shortId)) {
			return shortId;
		}
		int index = CharMatcher.anyOf(":").lastIndexIn(id);
		if (index > 0 && index + 1 < id.length()) {
			return id.substring(index + 1);
		}
		return id;
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

	public FunctionSpecBuilder param(ValueExpTypeInfo type) {
		return param(type, null, false);
	}

	public FunctionSpecBuilder optional(ValueExpTypeInfo type) {
		return param(type, null, true);
	}

	public FunctionSpecBuilder optional(ValueExpTypeInfo type, ValueExp defaultValue) {
		return param(type, defaultValue, true);
	}

	public FunctionSpecBuilder param(ValueExpTypeInfo type, ValueExp defaultValue, boolean optional) {
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
					                                    type.getValueType(), defaultValue.getEvaluatesTo()));
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

	public FunctionSpecBuilder varArg(ValueExpTypeInfo type, int min, int max) {
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

	/** A variadic sequence of primitive values or bags, for higher-order functions. */
	public FunctionSpecBuilder anyValuesVarArg(int min, int max) {
		Preconditions.checkArgument(min >= 0 && max > min);
		if (hadVarArg) {
			throw new SyntaxException("Can't add vararg parameter after vararg parameter");
		}
		hadVarArg = true;
		paramSpec.add(new FunctionParamValueTypeSequenceSpec(min, max));
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
			ValueExpTypeInfo returnType,
			FunctionInvocation invocation) {
		return build(
				new StaticFunctionReturnTypeResolver(returnType),
				invocation);
	}

	public FunctionSpec build(
			ValueExpTypeInfo returnType,
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
		private List<FunctionParamSpec> parameters = new ArrayList<FunctionParamSpec>();
		private boolean evaluateParameters;
		// two mandatory parameters, evaluated before the call (design D8)
		private final boolean eagerPair;
		// one mandatory parameter, evaluated before the call (design D23)
		private final boolean eagerSingle;
		// the invocation of an eager pair or single when it calls its method directly, or null (designs D15, D23)
		private final DefaultFunctionInvocation direct;

		private static final Class<?> ARRAYS_AS_LIST = Arrays.asList(new Object[0]).getClass();
		private static final Class<?> LIST_12 = List.of(1).getClass();
		private static final Class<?> LIST_N = List.of(1, 2, 3).getClass();

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
			this.eagerPair = !evaluateParameters && parameters.size() == 2 &&
					isMandatory(parameters.get(0)) && isMandatory(parameters.get(1));
			this.eagerSingle = !evaluateParameters && parameters.size() == 1 && isMandatory(parameters.get(0));
			this.direct = ((eagerPair || eagerSingle) && invocation instanceof DefaultFunctionInvocation d && d.isDirect())
					? d : null;
		}

		private static boolean isMandatory(FunctionParamSpec p) {
			return !(p.isOptional() || p.isVariadic());
		}

		@Override
		public BagStorage.Scan scan(EvaluationContext context, Value<?> literal, BagOfValues bag) {
			if (direct == null || context.isValidateFuncParamsAtRuntime()) return BagStorage.Scan.NONE;
			return direct.scan(this, literal, bag);
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
		public ValueExpTypeInfo resolveReturnType(List<Expression> arguments) {
			return resolver.resolve(this, arguments);
		}

		@Override
		public <T extends ValueExp> T invokeWithList(
				EvaluationContext context,
				List<Expression> arguments) throws EvaluationException {
			return doInvoke(context, arguments, false);
		}

		/**
		 * One operand of an eager single bound to its method takes the direct call (design D23), and two
		 * operands of an eager pair take {@link #invoke(EvaluationContext, Expression, Expression)}; any
		 * other call takes the list path, as the interface's default does. The array is read during the
		 * call and not kept.
		 */
		@Override
		public <T extends ValueExp> T invoke(
				EvaluationContext context,
				Expression... arguments) throws EvaluationException {
			if (arguments != null && arguments.length == 1 && eagerSingle && direct != null) {
				return invokeDirect(context, arguments[0]);
			}
			if (arguments != null && arguments.length == 2 && eagerPair) {
				return invoke(context, arguments[0], arguments[1]);
			}
			return invokeWithList(context, (arguments == null) ? List.of() : Arrays.asList(arguments));
		}

		/**
		 * Two mandatory parameters normalize two arguments to themselves, so
		 * the call skips {@link #normalize(List)} and evaluates the operands
		 * into the list it creates here (design D8).
		 */
		@Override
		public <T extends ValueExp> T invoke(
				EvaluationContext context,
				Expression arg0, Expression arg1) throws EvaluationException {
			if (eagerPair && direct != null) {
				return invokeDirect(context, arg0, arg1);
			}
			return eagerPair ? doInvoke(context, pairList(arg0, arg1), true) : invokeWithList(context, pairList(arg0, arg1));
		}

		/** A modifiable list, as the evaluated list of the generic path is (review RVC1, RVC2) */
		private static List<Expression> pairList(Expression arg0, Expression arg1) {
			List<Expression> arguments = new ArrayList<>(2);
			arguments.add(arg0);
			arguments.add(arg1);
			return arguments;
		}

		/**
		 * A built-in function bound directly (design D15): the operands are evaluated in order
		 * and passed to the method, with no argument list, no argument array, and no reflection.
		 * With validation at runtime the call takes the validated path. Failures map as in
		 * {@link #doInvoke}. The operands stay {@link Expression}s: a function reference
		 * evaluates to itself (review RVI1).
		 */
		@SuppressWarnings("unchecked")
		private <T extends ValueExp> T invokeDirect(
				EvaluationContext context,
				Expression arg0, Expression arg1) throws EvaluationException {
			try {
				if (context.isValidateFuncParamsAtRuntime()) {
					return invokeNormalized(context, pairList(arg0, arg1), true, true);
				}
				Expression value0 = (arg0 == null) ? null : arg0.evaluate(context);
				Expression value1 = (arg1 == null) ? null : arg1.evaluate(context);
				return (T) direct.invokeDirect(this, context, value0, value1);
			} catch (EvaluationException e) {
				throw e;
			} catch (Exception e) {
				throw new FunctionInvocationException(this, e,
				                                      "Failed to invoke function=\"%s\"", shortId);
			}
		}

		/** One operand of a built-in function bound directly (design D23), as the pair above. */
		@SuppressWarnings("unchecked")
		private <T extends ValueExp> T invokeDirect(
				EvaluationContext context, Expression arg0) throws EvaluationException {
			try {
				if (context.isValidateFuncParamsAtRuntime()) {
					return invokeNormalized(context, normalize(Arrays.asList(arg0)), false, true);
				}
				Expression value0 = (arg0 == null) ? null : arg0.evaluate(context);
				return (T) direct.invokeDirect(this, context, value0);
			} catch (EvaluationException e) {
				throw e;
			} catch (Exception e) {
				throw new FunctionInvocationException(this, e,
				                                      "Failed to invoke function=\"%s\"", shortId);
			}
		}

		/**
		 * @param pair {@code true} when the arguments are a list owned by this
		 *             call that matches the two mandatory eager parameters
		 */
		private <T extends ValueExp> T doInvoke(
				EvaluationContext context,
				List<Expression> arguments, boolean pair) throws EvaluationException {

			try {
				List<Expression> normalizedArgs = pair ? arguments : normalize(arguments);
				return invokeNormalized(context, normalizedArgs, pair, context.isValidateFuncParamsAtRuntime());
			} catch (EvaluationException e) {
				throw e;
			} catch (Exception e) {
				throw new FunctionInvocationException(this, e,
				                                      "Failed to invoke function=\"%s\"", shortId);
			}
		}

		/**
		 * Validates and evaluates normalized arguments and invokes the function; the caller maps
		 * failures as {@link #doInvoke} does and reads the validation setting once per call.
		 */
		@SuppressWarnings("unchecked")
		private <T extends ValueExp> T invokeNormalized(
				EvaluationContext context,
				List<Expression> normalizedArgs, boolean pair, boolean validate) throws EvaluationException {
			if (validate && !doValidateNormalizedParameters(normalizedArgs)) {
				throw new FunctionInvocationException(this,
				                                      "Failed to validate function=\"%s\" parameters=\"%s\"",
				                                      shortId, normalizedArgs);
			}
			// In place only when no validator has seen the list (review rv009): a validator may keep it
			List<Expression> params = evaluateParameters ? normalizedArgs :
					(pair && !validate) ? evaluateInPlace(context, normalizedArgs) : evaluate(context, normalizedArgs);
			return (T) invocation.invoke(this, context, params);
		}

		@Override
		public void validateParametersAndThrow(List<Expression> arguments) throws SyntaxException {
			ListIterator<FunctionParamSpec> it = parameters.listIterator();
			List<Expression> normalizedParameters = normalize(arguments);
			ListIterator<Expression> expIt = normalizedParameters.listIterator();
			if (LOG.isDebugEnabled()) {
				LOG.debug("Function=\"{}\" normalized parameters=\"{}\"",
				          shortId, normalizedParameters);
			}
			// The loop below finds an argument too many after the last
			// parameter. A function without parameters has none.
			if (parameters.isEmpty() && !arguments.isEmpty()) {
				throw PolicySyntaxException.invalidFunction(shortId, "the function takes no arguments");
			}
			while (it.hasNext()) {
				FunctionParamSpec p = it.next();
				if(!p.validate(expIt)){
					throw PolicySyntaxException.invalidFunctionParam(shortId, p, expIt.previousIndex(),
					                                                 "invalid parameter");
				}
				if ((!it.hasNext() &&
						expIt.hasNext())) {
					throw PolicySyntaxException
							.invalidFunctionParameter(
									shortId, p, expIt.nextIndex(),
									String.format("expecting less function params, unexpected expression=\"%s\"",
									              expIt.next()));
				}

				if ((it.hasNext() &&
						!expIt.hasNext())) {
					throw PolicySyntaxException.invalidFunctionParam(shortId,
					                                                 p, expIt.previousIndex(), "more arguments expected");
				}
			}

			if (!validateAdditional(arguments)) {
				throw PolicySyntaxException.invalidFunction(shortId, "failed additional validation");
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
			if (parameters.isEmpty() && !normalizedParameters.isEmpty()) {
				return false;
			}
			while (it.hasNext()) {
				FunctionParamSpec p = it.next();
				if (!p.validate(expIt)) {
					return false;
				}

				if ((it.hasNext() &&
						!expIt.hasNext())) {
					FunctionParamSpec spec = it.next();
					if (!(spec.isOptional() || spec.isVariadic())) {
						return false;
					}
				}
				if (!it.hasNext() &&
						expIt.hasNext()) {
					return false;
				}
			}
			return validateAdditional(normalizedParameters);
		}

		/**
		 * Lists that an index loop reads exactly as their iterator does: immutable lists, and the
		 * fixed-size array views of {@code Arrays.asList} (the varargs path), whose iterator also
		 * reads the array live.
		 */
		private static boolean isIndexable(List<?> list) {
			Class<?> type = list.getClass();
			return list instanceof ImmutableList || type == ARRAYS_AS_LIST || type == LIST_12 || type == LIST_N;
		}

		/**
		 * Normalizes function parameter list
		 *
		 * @param actualParameters a list of function parameters
		 * @return a normalized list of function parameters
		 */
		private List<Expression> normalize(List<Expression> actualParameters) {
			// Index loops instead of iterators (design D16). Any other caller list is copied once
			// through its own iterator, so it is read as before, for example as the snapshot of a
			// copy-on-write list (review RCJ2). The assumption is made that parameter types follow
			// in mandatory, optional, variadic order and parameter type groups do not interleave.
			List<Expression> actual = isIndexable(actualParameters) ?
					actualParameters : new ArrayList<>(actualParameters);
			int actualSize = actual.size();
			int formalSize = parameters.size();
			List<Expression> normalizedParams = new ArrayList<Expression>(Math.max(actualSize, formalSize));
			int a = 0;
			int f = 0;
			for (; f < formalSize; f++) {
				FunctionParamSpec spec = parameters.get(f);
				if (spec.isOptional() || spec.isVariadic()) {
					break;
				}
				if (a < actualSize) {
					normalizedParams.add(actual.get(a++));
				}
			}
			for (; f < formalSize; f++) {
				FunctionParamSpec spec = parameters.get(f);
				if (!spec.isOptional()) {
					break;
				}
				Expression optionalParamValue = (a < actualSize) ? actual.get(a++) : null;
				if (optionalParamValue == null) {
					optionalParamValue = spec.getDefaultValue().orElse(null);
				}
				normalizedParams.add(optionalParamValue);
			}
			for (; f < formalSize; f++) {
				FunctionParamSpec spec = parameters.get(f);
				if (!spec.isVariadic()) {
					break;
				}
				Expression variadicParamValue = (a < actualSize) ? actual.get(a++) : null;
				normalizedParams.add(variadicParamValue);
			}
			// Add the rest of the actual parameters
			for (; a < actualSize; a++) {
				normalizedParams.add(actual.get(a));
			}
			return normalizedParams;
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
		 * Evaluates the given arguments in the given list, in order
		 *
		 * @param context   an evaluation context
		 * @param arguments a mutable list owned by the caller
		 * @return the given list
		 * @throws EvaluationException if an evaluation
		 *                             error occurs
		 */
		private static List<Expression> evaluateInPlace(EvaluationContext context, List<Expression> arguments)
				throws EvaluationException {
			for (int i = 0; i < arguments.size(); i++) {
				Expression exp = arguments.get(i);
				arguments.set(i, (exp == null) ? null : exp.evaluate(context));
			}
			return arguments;
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
			                  .add("functionId", shortId)
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
