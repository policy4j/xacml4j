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
import org.xacml4j.v30.policy.BoundCall;
import org.xacml4j.v30.policy.FunctionInvocationException;
import org.xacml4j.v30.policy.FunctionParamSpec;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.PolicySyntaxException;
import org.xacml4j.v30.policy.RootEvaluationContext;
import org.xacml4j.v30.policy.function.impl.DefaultFunctionInvocation;
import org.xacml4j.v30.types.Value;
import org.xacml4j.v30.types.XacmlTypes;

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
	private boolean writesNoResponses;

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

	/**
	 * Declares that the function writes no advice or obligations to the evaluation context, so a
	 * policy that calls it needs no staging for it (design D36). The engine's own functions are
	 * built so when the default invocation factory calls them; declare it only for a function that
	 * never writes responses.
	 *
	 * @return this builder
	 */
	public FunctionSpecBuilder writesNoResponses() {
		writesNoResponses = true;
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
		return new FunctionSpecImpl(functionId, functionId,
		                            legacyId, paramSpec, returnType, invocation, null, lazyArgumentEvaluation,
		                            writesNoResponses);
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
				lazyArgumentEvaluation,
				writesNoResponses);
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
	 * Tells whether a function reads the argument list of an expression directly, with no argument
	 * array: a function that this builder built does (design D39). An expression invokes any other
	 * function on its variadic method, as before.
	 *
	 * @param spec a function
	 * @return {@code true} for a function that this builder built
	 */
	public static boolean readsArgumentLists(FunctionSpec spec) {
		return spec instanceof FunctionSpecImpl;
	}

	/**
	 * Tells whether a prepared condition may share the value of a function within a decision (change
	 * optimize-pdp-evaluation, design D45): string-one-and-only, string-normalize-space or
	 * string-normalize-to-lower-case as the engine builds them. The proof is the identity of the direct
	 * binding of the engine's method, which only the default invocation factory makes, together with
	 * one argument evaluated eagerly, the standard parameter and return type, no validator, and no
	 * responses; a function id or {@link FunctionSpec#writesNoResponses()} proves nothing alone. For
	 * preparation: the answer reads a few fields and calls nothing by reflection.
	 *
	 * @param spec a function
	 * @return {@code true} for one of the three functions as the engine builds them
	 */
	public static boolean isShareableBuiltin(FunctionSpec spec) {
		return spec instanceof FunctionSpecImpl built && built.eagerSingle && built.direct != null
				&& built.validator == null && built.writesNoResponses && built.direct.isShareable()
				&& built.parameters.get(0) instanceof FunctionParamValueTypeSpec parameter
				&& (XacmlTypes.STRING.equals(parameter.getParamType())
						|| XacmlTypes.STRING.bagType().equals(parameter.getParamType()))
				&& built.resolver instanceof StaticFunctionReturnTypeResolver
				&& XacmlTypes.STRING.equals(built.resolver.resolve(built, List.of()));
	}

	/** Whether the function uses a trusted default built-in invocation. */
	public static boolean usesBuiltInInvocation(FunctionSpec spec) {
		return spec instanceof FunctionSpecImpl built
				&& built.invocation instanceof DefaultFunctionInvocation invocation
				&& invocation.isTrustedBuiltIn();
	}

	/** Invokes one operand as the variadic method does, using a trusted direct binding when available. */
	public static <T extends ValueExp> T invokeVariadicSingle(
			FunctionSpec spec, EvaluationContext context, Expression argument) {
		if (spec instanceof FunctionSpecImpl built && usesBuiltInInvocation(spec)
				&& built.eagerSingle && built.direct != null) {
			return built.invokeDirect(context, argument);
		}
		return spec.invoke(context, new Expression[]{argument});
	}

	/**
	 * Tells whether a parameter validates one argument by the argument's type alone: the builder's
	 * own parameters other than the variadic one do, so {@code validate} of one non-null argument
	 * that is not a {@link org.xacml4j.v30.policy.FunctionReference} gives
	 * {@link FunctionParamSpec#isValidParamType} of its type (design D39). Callers exclude null
	 * arguments and function references first: a function-reference parameter accepts a reference
	 * in {@code validate}, while its {@code isValidParamType} is always false (review r-AG2). A
	 * parameter of another implementation can validate a sequence otherwise (review r-AG).
	 *
	 * @param param a function parameter
	 * @return {@code true} for a single-argument parameter that this builder makes
	 */
	public static boolean validatesOneArgumentByType(FunctionParamSpec param) {
		Class<?> type = param.getClass();
		return type == FunctionParamValueTypeSpec.class || type == FunctionParamAnyValueSpec.class
				|| type == FunctionParamAnyBagSpec.class || type == FunctionParamFuncReferenceSpec.class;
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
		private final DefaultFunctionInvocation fast;
		private final int fastMinArguments;
		private final int fastMaxArguments;
		private final boolean writesNoResponses;
		// whether a parameter is optional, whose default can replace a null argument
		private final boolean hasOptional;
		// whether a call can normalize to the caller's own immutable list: an eager call passes evaluated
		// copies on, and the engine's invocation only reads the arguments of a lazy one (design D39)
		private final boolean sharesArguments;

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
				boolean evaluateParameters,
				boolean writesNoResponses) {
			this.functionId = Preconditions.checkNotNull(functionId);
			this.writesNoResponses = writesNoResponses;
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
			this.fast = invocation instanceof DefaultFunctionInvocation d
					&& d.callShape() != DefaultFunctionInvocation.CallShape.NONE ? d : null;
			boolean mandatoryParameters = parameters.stream().allMatch(FunctionSpecImpl::isMandatory);
			int min = -1;
			int max = -1;
			if (fast != null) {
				switch (fast.callShape()) {
					case FIXED -> {
						if (!evaluateParameters && mandatoryParameters) min = max = fast.fixedArgumentCount();
					}
					case AND, OR -> {
						if (evaluateParameters && parameters.size() == 1 && isVariadic()) {
							min = 0;
							max = Integer.MAX_VALUE;
						}
					}
					case ANY_OF, ALL_OF, ANY_OF_ANY, MAP -> {
						if (!evaluateParameters && parameters.size() == 2
								&& isMandatory(parameters.get(0)) && isVariadic()) {
							min = 2;
							max = 3;
						}
					}
					default -> { }
				}
			}
			this.fastMinArguments = min;
			this.fastMaxArguments = max;
			this.hasOptional = parameters.stream().anyMatch(FunctionParamSpec::isOptional);
			this.sharesArguments = !evaluateParameters || invocation instanceof DefaultFunctionInvocation;
		}

		private static boolean isMandatory(FunctionParamSpec p) {
			return !(p.isOptional() || p.isVariadic());
		}

		@Override
		public BagStorage.Scan scan(EvaluationContext context, Value<?> literal, BagOfValues bag) {
			if (direct == null || context.isValidateFuncParamsAtRuntime()) return BagStorage.Scan.NONE;
			try {
				return direct.scan(this, literal, bag);
			} catch (EvaluationException e) {
				throw failed(context, e);
			}
		}

		@Override
		public boolean hasDirectScan(Value<?> literal) {
			return direct != null && direct.canScan(literal);
		}

		/**
		 * The call site of a literal first argument of an eager pair, when the provider instance
		 * binds literals (design D6 of add-regex-engine-spi): the bound call evaluates the second
		 * argument and invokes the binding, validates when validation is on at runtime, and maps
		 * and reports a failure as
		 * {@link #invokeDirect(EvaluationContext, Expression, Expression)} does.
		 */
		@Override
		public Optional<BoundCall> bind(Value<?> literal) {
			if (!eagerPair || !(invocation instanceof DefaultFunctionInvocation bindable)) {
				return Optional.empty();
			}
			return bindable.bind(this, literal).map(bound -> new BoundCall() {
				@Override
				public <T extends ValueExp> T invoke(EvaluationContext context, Expression argument)
						throws EvaluationException {
					if (context.isValidateFuncParamsAtRuntime()) {
						List<Expression> normalized;
						try {
							normalized = normalize(Arrays.<Expression>asList(literal, argument), false);
							if (!doValidateNormalizedParameters(normalized)) {
								throw new FunctionInvocationException(FunctionSpecImpl.this,
										"Failed to validate function=\"%s\" parameters=\"%s\"", shortId, normalized);
							}
						} catch (Exception e) {
							throw failed(context, e);
						}
						List<Expression> evaluated = evaluate(context, normalized);
						try {
							if (evaluated.size() != 2 || evaluated.get(0) != literal) {
								@SuppressWarnings("unchecked")
								T result = (T) invocation.invoke(FunctionSpecImpl.this, context, evaluated);
								return result;
							}
							return bound.invoke(context, evaluated.get(1));
						} catch (Exception e) {
							throw failed(context, e);
						}
					}
					Expression value = (argument == null) ? null : argument.evaluate(context);
					try {
						return bound.invoke(context, value);
					} catch (Exception e) {
						throw failed(context, e);
					}
				}
			});
		}

		public FunctionSpecImpl(
				String functionId,
				String legacyId,
				List<FunctionParamSpec> params,
				FunctionReturnTypeResolver resolver,
				FunctionInvocation invocation,
				boolean evaluateParameters) {
			this(functionId, functionId, legacyId, params,
			     resolver, invocation, null, evaluateParameters, false);
		}

		@Override
		public boolean writesNoResponses() {
			return writesNoResponses;
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

		/**
		 * One operand of an eager single and two operands of an eager pair bound to their method take
		 * the direct call (designs D15, D23); any other call takes the list path, with no copy of an
		 * immutable argument list that normalizes to itself (design D39).
		 */
		@Override
		public <T extends ValueExp> T invokeWithList(
				EvaluationContext context,
				List<Expression> arguments) throws EvaluationException {
			int size = arguments.size();
			if (size == 1 && eagerSingle && direct != null) {
				return invokeDirect(context, arguments.get(0));
			}
			if (size == 2 && eagerPair && direct != null) {
				return invokeDirect(context, arguments.get(0), arguments.get(1));
			}
			if (fast != null && isImmutable(arguments) && fastShape(size)) {
				return invokeFast(context, arguments);
			}
			return doInvoke(context, arguments);
		}

		private boolean fastShape(int size) {
			return size >= fastMinArguments && size <= fastMaxArguments;
		}

		@SuppressWarnings("unchecked")
		private <T extends ValueExp> T invokeFast(EvaluationContext context, List<Expression> arguments) {
			if (context.isValidateFuncParamsAtRuntime()) {
				return doInvoke(context, arguments, true);
			}
			Expression v0 = null, v1 = null, v2 = null, v3 = null;
			if (!evaluateParameters) {
				int size = arguments.size();
				if (size > 0) v0 = arguments.get(0).evaluate(context);
				if (size > 1) v1 = arguments.get(1).evaluate(context);
				if (size > 2) v2 = arguments.get(2).evaluate(context);
				if (size > 3) v3 = arguments.get(3).evaluate(context);
			}
			try {
				return (T) fast.invokeFast(this, context, arguments, v0, v1, v2, v3);
			} catch (Exception e) {
				throw failed(context, e);
			}
		}

		/** As {@link #invokeWithList}; the array is read during the call and not kept. */
		@Override
		public <T extends ValueExp> T invoke(
				EvaluationContext context,
				Expression... arguments) throws EvaluationException {
			if (arguments != null && arguments.length == 1 && eagerSingle && direct != null) {
				return invokeDirect(context, arguments[0]);
			}
			if (arguments != null && arguments.length == 2 && eagerPair && direct != null) {
				return invokeDirect(context, arguments[0], arguments[1]);
			}
			return doInvoke(context, (arguments == null) ? List.of() : Arrays.asList(arguments));
		}

		@Override
		public <T extends ValueExp> T invoke(
				EvaluationContext context,
				Expression arg0, Expression arg1) throws EvaluationException {
			if (eagerPair && direct != null) {
				return invokeDirect(context, arg0, arg1);
			}
			return doInvoke(context, Arrays.asList(arg0, arg1));
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
			if (context.isValidateFuncParamsAtRuntime()) {
				return doInvoke(context, Arrays.asList(arg0, arg1), true);
			}
			Expression value0 = (arg0 == null) ? null : arg0.evaluate(context);
			Expression value1 = (arg1 == null) ? null : arg1.evaluate(context);
			try {
				return (T) direct.invokeDirect(this, context, value0, value1);
			} catch (Exception e) {
				throw failed(context, e);
			}
		}

		/** One operand of a built-in function bound directly (design D23), as the pair above. */
		@SuppressWarnings("unchecked")
		private <T extends ValueExp> T invokeDirect(
				EvaluationContext context, Expression arg0) throws EvaluationException {
			if (context.isValidateFuncParamsAtRuntime()) {
				return doInvoke(context, Arrays.asList(arg0), true);
			}
			Expression value0 = (arg0 == null) ? null : arg0.evaluate(context);
			try {
				return (T) direct.invokeDirect(this, context, value0);
			} catch (Exception e) {
				throw failed(context, e);
			}
		}

		private <T extends ValueExp> T doInvoke(
				EvaluationContext context,
				List<Expression> arguments) throws EvaluationException {
			return doInvoke(context, arguments, context.isValidateFuncParamsAtRuntime());
		}

		/**
		 * Normalizes and validates the arguments, evaluates them, and invokes the function. The
		 * arguments are evaluated outside the call's own failure mapping: the failure of an argument
		 * is the argument's (design D34). The caller reads the validation setting once per call.
		 */
		@SuppressWarnings("unchecked")
		private <T extends ValueExp> T doInvoke(
				EvaluationContext context,
				List<Expression> arguments, boolean validate) throws EvaluationException {
			List<Expression> normalizedArgs;
			try {
				// A validated call copies, so a validator, which may keep the list, never sees one
				// that the caller changes later (review rv009)
				normalizedArgs = normalize(arguments, !validate && sharesArguments);
				if (validate && !doValidateNormalizedParameters(normalizedArgs)) {
					throw new FunctionInvocationException(this,
					                                      "Failed to validate function=\"%s\" parameters=\"%s\"",
					                                      shortId, normalizedArgs);
				}
			} catch (Exception e) {
				throw failed(context, e);
			}
			// In place only in the copy that normalizing made, and only when no validator has seen it
			// (review rv009): a validator may keep it, and the caller's list is not changed
			List<Expression> params = evaluateParameters ? normalizedArgs :
					(!validate && normalizedArgs != arguments) ? evaluateInPlace(context, normalizedArgs)
							: evaluate(context, normalizedArgs);
			try {
				return (T) invocation.invoke(this, context, params);
			} catch (Exception e) {
				throw failed(context, e);
			}
		}

		/**
		 * Maps a failure of a call of this function, an evaluation exception as it is, and reports it
		 * to the observation of the decision (design D34)
		 */
		private EvaluationException failed(EvaluationContext context, Exception e) {
			EvaluationException failure = (e instanceof EvaluationException evaluation) ? evaluation
					: new FunctionInvocationException(this, e, "Failed to invoke function=\"%s\"", shortId);
			RootEvaluationContext.observationOf(context).functionFailed(context, this, failure);
			return failure;
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

		/** Lists that no caller can change: the immutable lists of {@link #isIndexable} */
		private static boolean isImmutable(List<?> list) {
			Class<?> type = list.getClass();
			return list instanceof ImmutableList || type == LIST_12 || type == LIST_N;
		}

		/**
		 * Normalizes function parameter list into a list of its own, which a validator may keep or
		 * change (review r-AG)
		 *
		 * @param actualParameters a list of function parameters
		 * @return a normalized list of function parameters
		 */
		private List<Expression> normalize(List<Expression> actualParameters) {
			return normalize(actualParameters, false);
		}

		/**
		 * @param mayReturnItself whether an immutable list that normalizes to itself is returned as it
		 *                        is, with no copy (design D39); a list that the caller can change is
		 *                        copied before an operand is evaluated, so an operand that changes it
		 *                        does not change a later operand (review r-AG)
		 */
		private List<Expression> normalize(List<Expression> actualParameters, boolean mayReturnItself) {
			// Index loops instead of iterators (design D16). Any other caller list is copied once
			// through its own iterator, so it is read as before, for example as the snapshot of a
			// copy-on-write list (review RCJ2). The assumption is made that parameter types follow
			// in mandatory, optional, variadic order and parameter type groups do not interleave.
			if (mayReturnItself && isImmutable(actualParameters) && normalizesToItself(actualParameters)) {
				return actualParameters;
			}
			boolean indexable = isIndexable(actualParameters);
			List<Expression> actual = indexable ? actualParameters : new ArrayList<>(actualParameters);
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
		 * Tells whether {@link #normalize(List)} gives the same arguments: every parameter has an
		 * argument, and no optional parameter has a null one, which its default would replace
		 */
		private boolean normalizesToItself(List<Expression> arguments) {
			int formalSize = parameters.size();
			if (arguments.size() < formalSize) {
				return false;
			}
			for (int f = 0; hasOptional && f < formalSize; f++) {
				if (arguments.get(f) == null && parameters.get(f).isOptional()) {
					return false;
				}
			}
			return true;
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
