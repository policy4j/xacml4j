package org.xacml4j.v30.policy.function;

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
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.BagOfValuesType;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.ValueExpTypeInfo;
import org.xacml4j.v30.policy.FunctionInvocationException;
import org.xacml4j.v30.policy.FunctionParamSpec;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.impl.PolicyToPlatformFunctionInvocation;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.IntegerVal;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.Value;
import org.xacml4j.v30.types.ValueType;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * Cycles I and Q of change optimize-pdp-evaluation (designs D15 and D23): a two- or one-argument call
 * of a built-in function goes straight to its method, with no argument list, no reflective array, and
 * no reflection, and gives the same result or the same failure as the reflective call of
 * {@code invokeWithList}.
 */
public class DirectPairCallTest
{
	private static final String F1 = "urn:oasis:names:tc:xacml:1.0:function:";
	private static final String NOT = F1 + "not";
	private static final String STRING_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:string-equal";
	private static final String INTEGER_EQUAL = "urn:oasis:names:tc:xacml:1.0:function:integer-equal";
	private static final String FAILING = "urn:xacml4j:test:function:failing-";
	private static final String WITH_REFERENCE = "urn:xacml4j:test:function:with-reference";
	private static final String PACKAGE_PRIVATE_EQUAL = "urn:xacml4j:test:function:package-private-equal";
	private static final String PRIVATE_PARAMETERS = "urn:xacml4j:test:function:private-parameters";

	private static final FunctionProvider FUNCTIONS = FunctionProviderBuilder.builder()
			.withDefaultFunctions()
			.fromClass(FailingFunctions.class, ReferenceFunctions.class, PrivateParameterFunctions.class, packagePrivateFunctions())
			.build();

	private static Class<?> packagePrivateFunctions()
	{
		try{
			return Class.forName("org.xacml4j.v30.policy.function.impl.PackagePrivateFunctions");
		}catch(ClassNotFoundException e){
			throw new AssertionError(e);
		}
	}

	/** Two or three values per built-in type; types that need a document or a category are left out. */
	private static final Map<ValueType, List<String>> SAMPLES = Map.ofEntries(
			Map.entry(XacmlTypes.STRING, List.of("a", "b", "A")),
			Map.entry(XacmlTypes.INTEGER, List.of("1", "2", "-3")),
			Map.entry(XacmlTypes.DOUBLE, List.of("1.5", "NaN", "-0.0")),
			Map.entry(XacmlTypes.BOOLEAN, List.of("true", "false")),
			Map.entry(XacmlTypes.DATE, List.of("2002-03-22", "2003-01-01Z")),
			Map.entry(XacmlTypes.DATETIME, List.of("2002-03-22T08:23:47-05:00", "2003-01-01T00:00:00Z")),
			Map.entry(XacmlTypes.TIME, List.of("08:23:47-05:00", "09:00:00Z")),
			Map.entry(XacmlTypes.DAYTIMEDURATION, List.of("P1DT2H", "PT5M")),
			Map.entry(XacmlTypes.YEARMONTHDURATION, List.of("P1Y2M", "P3M")),
			Map.entry(XacmlTypes.ANYURI, List.of("http://example.com/a", "http://example.com/b")),
			Map.entry(XacmlTypes.HEXBINARY, List.of("0FB7", "0fb8")),
			Map.entry(XacmlTypes.BASE64BINARY, List.of("AQID", "BAUG")),
			Map.entry(XacmlTypes.X500NAME, List.of("cn=a, o=example", "cn=b, o=example")),
			Map.entry(XacmlTypes.RFC822NAME, List.of("anne@example.com", "bob@EXAMPLE.com")),
			Map.entry(XacmlTypes.IPADDRESS, List.of("10.0.0.1", "10.0.0.2")),
			Map.entry(XacmlTypes.DNSNAME, List.of("example.com", "www.example.org")));

	/** An unobserved context that answers {@code isValidateFuncParamsAtRuntime}. */
	private static EvaluationContext context(boolean validate)
	{
		return (EvaluationContext) Proxy.newProxyInstance(EvaluationContext.class.getClassLoader(),
				new Class<?>[]{EvaluationContext.class}, (proxy, method, args) -> {
					if(method.getName().equals("isValidateFuncParamsAtRuntime")){
						return validate;
					}
					if(method.getName().equals("getDecisionObservation")){
						return org.xacml4j.v30.spi.observe.DecisionObservation.none();
					}
					throw new UnsupportedOperationException(method.getName());
				});
	}

	/** What a call gave: a value, or a failure with its status code, status message, and cause. */
	private record Outcome(Object value, Class<?> failure, String status, String message, Class<?> cause,
			String causeMessage)
	{
		static Outcome of(Callable<Object> call)
		{
			try{
				return new Outcome(call.call(), null, null, null, null, null);
			}catch(Exception e){
				Throwable cause = e.getCause();
				Status status = (e instanceof EvaluationException ee) ? ee.getStatus() : null;
				return new Outcome(null, e.getClass(),
						(status == null) ? null : status.getStatusCode().getValue().toString(),
						(status == null) ? null : status.getMessage().orElse(null),
						(cause == null) ? null : cause.getClass(), (cause == null) ? null : cause.getMessage());
			}
		}
	}

	private static Outcome single(FunctionSpec spec, EvaluationContext context, Expression a)
	{
		return Outcome.of(() -> spec.invoke(context, a));
	}

	private static Outcome list(FunctionSpec spec, EvaluationContext context, Expression a)
	{
		List<Expression> arguments = new ArrayList<>(1);
		arguments.add(a);
		return Outcome.of(() -> spec.invokeWithList(context, arguments));
	}

	private static Outcome pair(FunctionSpec spec, EvaluationContext context, Expression a, Expression b)
	{
		return Outcome.of(() -> spec.invoke(context, a, b));
	}

	private static Outcome list(FunctionSpec spec, EvaluationContext context, Expression a, Expression b)
	{
		List<Expression> arguments = new ArrayList<>(2);
		arguments.add(a);
		arguments.add(b);
		return Outcome.of(() -> spec.invokeWithList(context, arguments));
	}

	private static FunctionSpec function(String id)
	{
		return FUNCTIONS.getFunction(id).orElseThrow(() -> new AssertionError("no function " + id));
	}

	private static List<Expression> samples(FunctionParamSpec param)
	{
		if(!(param instanceof FunctionParamValueTypeSpec typed)){
			return List.of();
		}
		ValueExpTypeInfo type = typed.getParamType();
		ValueType valueType = (type instanceof BagOfValuesType bag) ? bag.getValueType() : (ValueType) type;
		List<Value<?>> values = new ArrayList<>();
		for(String lexical : SAMPLES.getOrDefault(valueType, List.of())){
			try{
				values.add(valueType.ofAny(lexical));
			}catch(RuntimeException e){
				// a lexical form the type does not take is left out
			}
		}
		if(!(type instanceof BagOfValuesType)){
			return List.copyOf(values);
		}
		if(values.isEmpty()){
			return List.of();
		}
		return List.of(valueType.emptyBag(),
				valueType.bagBuilder().attribute(values.get(0)).build(),
				valueType.bagBuilder().attributes(values).build());
	}

	@Test
	public void everyTwoParameterFunctionGivesTheSameOutcomeOnBothPaths()
	{
		EvaluationContext context = context(false);
		List<String> covered = new ArrayList<>();
		List<String> skipped = new ArrayList<>();
		for(FunctionSpec spec : FUNCTIONS.getProvidedFunctions()){
			if(spec.getNumberOfParams() != 2 || spec.isRequiresLazyParamEval() || spec.isVariadic()
					|| spec.getParamSpecAt(0).isOptional() || spec.getParamSpecAt(1).isOptional()
					|| spec.getId().startsWith(FAILING) || spec.getId().startsWith("urn:xacml4j:test:")){
				continue;
			}
			List<Expression> first = samples(spec.getParamSpecAt(0));
			List<Expression> second = samples(spec.getParamSpecAt(1));
			if(first.isEmpty() || second.isEmpty()){
				skipped.add(spec.getId());
				continue;
			}
			for(Expression a : first){
				for(Expression b : second){
					assertWithMessage("%s(%s, %s)", spec.getId(), a, b)
							.that(pair(spec, context, a, b)).isEqualTo(list(spec, context, a, b));
				}
			}
			covered.add(spec.getId());
		}
		assertWithMessage("functions compared; skipped (no samples for their types): %s", skipped)
				.that(covered.size()).isAtLeast(100);
		for(String id : skipped){
			assertWithMessage("skipped %s", id).that(id).containsMatch("xpath|entity|jpath|json|-is-in|map|any-of|all-of");
		}
	}

	/** Design D23: also an operand of the wrong type and a call with validation at runtime. */
	@Test
	public void everyOneParameterFunctionGivesTheSameOutcomeOnBothPaths()
	{
		List<String> covered = new ArrayList<>();
		List<String> skipped = new ArrayList<>();
		for(FunctionSpec spec : FUNCTIONS.getProvidedFunctions()){
			if(spec.getNumberOfParams() != 1 || spec.isRequiresLazyParamEval() || spec.isVariadic()
					|| spec.getParamSpecAt(0).isOptional() || spec.getId().startsWith("urn:xacml4j:test:")){
				continue;
			}
			List<Expression> operands = new ArrayList<>(samples(spec.getParamSpecAt(0)));
			if(operands.isEmpty()){
				skipped.add(spec.getId());
				continue;
			}
			operands.add(wrongType(spec.getParamSpecAt(0)));
			for(boolean validate : List.of(false, true)){
				EvaluationContext context = context(validate);
				for(Expression a : operands){
					assertWithMessage("%s(%s), validate=%s", spec.getId(), a, validate)
							.that(single(spec, context, a)).isEqualTo(list(spec, context, a));
				}
			}
			covered.add(spec.getId());
		}
		assertWithMessage("functions compared; skipped (no samples for their types): %s", skipped)
				.that(covered.size()).isAtLeast(40);
		for(String id : skipped){
			assertWithMessage("skipped %s", id).that(id).containsMatch("xpath|entity");
		}
	}

	/** An operand that is not of the parameter's type: a bag parameter gets a single value. */
	private static Expression wrongType(FunctionParamSpec param)
	{
		ValueExpTypeInfo type = ((FunctionParamValueTypeSpec) param).getParamType();
		return (type == XacmlTypes.INTEGER) ? XacmlTypes.STRING.ofAny("a") : XacmlTypes.INTEGER.ofAny(1);
	}

	/**
	 * Design D23 (c): a built-in without a direct binding (variadic, lazy, with the context, three
	 * operands, higher-order) gives the same result or the same failure as through a custom factory,
	 * which keeps the reflective call; also with an argument of the wrong type.
	 */
	@Test
	public void builtInsWithoutADirectBindingGiveTheSameOutcomeAsTheReflectiveCall()
	{
		FunctionInvocationFactory delegating = new FunctionInvocationFactory(){
			@Override
			public <T> PolicyToPlatformFunctionInvocation<T> create(Object instance, Method m){
				return FunctionInvocationFactory.defaultFactory().create(instance, m);
			}
		};
		FunctionProvider reflective = FunctionProviderBuilder.builder(delegating).build();
		EvaluationContext context = mock(EvaluationContext.class);
		when(context.getTimeZone()).thenReturn(TimeZone.getTimeZone("UTC"));
		when(context.getClock()).thenReturn(Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));
		Expression yes = XacmlTypes.BOOLEAN.ofAny(true);
		Expression no = XacmlTypes.BOOLEAN.ofAny(false);
		Expression one = XacmlTypes.INTEGER.ofAny(1);
		Expression three = XacmlTypes.INTEGER.ofAny(3);
		Expression a = XacmlTypes.STRING.ofAny("a");
		Expression hello = XacmlTypes.STRING.ofAny("hello");
		Expression equal = new org.xacml4j.v30.policy.FunctionReference(function(STRING_EQUAL));
		Expression ab = XacmlTypes.STRING.bagOf(XacmlTypes.STRING.ofAny("a"), XacmlTypes.STRING.ofAny("b"));
		Map<String, List<List<Expression>>> calls = Map.of(
				F1 + "integer-add", List.of(List.of(one, one, one), List.of(one, a)),
				F1 + "string-bag", List.of(List.of(a, a), List.of()),
				F1 + "and", List.of(List.of(yes, no), List.of(a)),
				F1 + "n-of", List.of(List.of(one, no, yes), List.of(three, yes)),
				F1 + "string-regexp-match", List.of(List.of(XacmlTypes.STRING.ofAny("a+"), a),
						List.of(XacmlTypes.STRING.ofAny("("), a), List.of(one, a)),
				"urn:oasis:names:tc:xacml:3.0:function:integer-from-string",
				List.of(List.of(XacmlTypes.STRING.ofAny("12")), List.of(a)),
				"urn:oasis:names:tc:xacml:3.0:function:string-substring",
				List.of(List.of(hello, one, three), List.of(hello, three, one)),
				"urn:oasis:names:tc:xacml:2.0:function:time-in-range", List.of(List.of(XacmlTypes.TIME.ofAny("09:00:00Z"),
						XacmlTypes.TIME.ofAny("08:00:00Z"), XacmlTypes.TIME.ofAny("10:00:00Z"))),
				"urn:oasis:names:tc:xacml:3.0:function:any-of", List.of(List.of(equal, a, ab), List.of(equal, one, ab)));
		for(Map.Entry<String, List<List<Expression>>> call : calls.entrySet()){
			for(List<Expression> arguments : call.getValue()){
				Expression[] array = arguments.toArray(new Expression[0]);
				assertWithMessage("%s%s", call.getKey(), arguments)
						.that(Outcome.of(() -> function(call.getKey()).invoke(context, array)))
						.isEqualTo(Outcome.of(() -> reflective.getFunction(call.getKey()).orElseThrow().invoke(context, array)));
			}
		}
		assertThat(Outcome.of(() -> function(F1 + "integer-add").invoke(context, one, one, one)).value())
				.isEqualTo(three);
		assertThat(Outcome.of(() -> function("urn:oasis:names:tc:xacml:3.0:function:any-of").invoke(context, equal, a, ab))
				.value()).isEqualTo(yes);
	}

	@Test
	public void operandOfAOneParameterFunctionIsEvaluatedOnce() throws EvaluationException
	{
		Expression operand = mock(Expression.class);
		AtomicInteger evaluations = new AtomicInteger();
		when(operand.evaluate(any())).thenAnswer(i -> { evaluations.incrementAndGet(); return XacmlTypes.BOOLEAN.ofAny(false); });

		BooleanVal result = function(NOT).invoke(context(false), operand);

		assertThat(result.get()).isTrue();
		assertThat(evaluations.get()).isEqualTo(1);
	}

	@Test
	public void failingOperandOfAOneParameterFunctionMapsAsOnTheListPath() throws EvaluationException
	{
		EvaluationContext context = context(false);
		FunctionSpec spec = function(NOT);
		Expression bug = mock(Expression.class);
		Expression missing = mock(Expression.class);
		when(bug.evaluate(any())).thenThrow(new IllegalStateException("bug"));
		when(missing.evaluate(any())).thenThrow(new EvaluationException(Status.missingAttribute("urn:test:attribute").build(), "missing"));

		Outcome failure = single(spec, context, bug);

		// The failure of an operand is not a failure of the function: the enclosing Apply maps it (design D34)
		assertThat(failure.failure()).isEqualTo(IllegalStateException.class);
		assertThat(failure).isEqualTo(list(spec, context, bug));
		assertThat(single(spec, context, missing)).isEqualTo(list(spec, context, missing));
	}


	@Test
	public void operandsAreEvaluatedOnceEachAndInOrder() throws EvaluationException
	{
		EvaluationContext context = mock(EvaluationContext.class);
		List<String> order = new ArrayList<>();
		Expression first = mock(Expression.class);
		Expression second = mock(Expression.class);
		when(first.evaluate(any())).thenAnswer(i -> { order.add("first"); return XacmlTypes.STRING.ofAny("x"); });
		when(second.evaluate(any())).thenAnswer(i -> { order.add("second"); return XacmlTypes.STRING.ofAny("x"); });

		BooleanVal result = function(STRING_EQUAL).invoke(context, first, second);

		assertThat(result.get()).isTrue();
		assertThat(order).containsExactly("first", "second").inOrder();
	}

	@Test
	public void failuresOfTheFunctionMapAsOnTheReflectivePath()
	{
		EvaluationContext context = context(false);
		IntegerVal one = XacmlTypes.INTEGER.ofAny(1);
		for(String kind : List.of("evaluation", "runtime", "nested", "error")){
			FunctionSpec spec = function(FAILING + kind);
			Outcome pair = pair(spec, context, one, one);

			assertWithMessage(kind).that(pair).isEqualTo(list(spec, context, one, one));
			assertWithMessage(kind).that(pair.failure()).isEqualTo(FunctionInvocationException.class);
		}
		assertThat(pair(function(FAILING + "evaluation"), context, one, one).cause()).isEqualTo(EvaluationException.class);
		assertThat(pair(function(FAILING + "nested"), context, one, one).cause()).isEqualTo(EvaluationException.class);
		assertThat(pair(function(FAILING + "error"), context, one, one).cause()).isEqualTo(AssertionError.class);
	}

	@Test
	public void argumentsOfTheWrongTypeFailAsOnTheReflectivePath()
	{
		EvaluationContext context = context(false);
		FunctionSpec spec = function(STRING_EQUAL);
		IntegerVal one = XacmlTypes.INTEGER.ofAny(1);
		StringVal a = XacmlTypes.STRING.ofAny("a");

		Outcome wrongFirst = pair(spec, context, one, a);
		Outcome wrongSecond = pair(spec, context, a, one);

		assertThat(wrongFirst.failure()).isEqualTo(FunctionInvocationException.class);
		assertThat(wrongFirst).isEqualTo(list(spec, context, one, a));
		assertThat(wrongSecond).isEqualTo(list(spec, context, a, one));
	}

	@Test
	public void validationAtRuntimeStillChecksTheArguments() throws EvaluationException
	{
		EvaluationContext context = context(true);
		FunctionSpec spec = function(STRING_EQUAL);
		StringVal a = XacmlTypes.STRING.ofAny("a");

		BooleanVal same = spec.invoke(context, a, a);
		FunctionInvocationException wrong = assertThrows(FunctionInvocationException.class,
				() -> spec.invoke(context, a, XacmlTypes.INTEGER.ofAny(1)));

		assertThat(same.get()).isTrue();
		assertThat(wrong).hasMessageThat().contains("Failed to validate");
	}

	/** A custom invocation factory sees every call, also of a standard function (review PR4: provenance, not id). */
	@Test
	public void customInvocationFactoryIsNotBypassed() throws EvaluationException
	{
		AtomicInteger calls = new AtomicInteger();
		FunctionInvocationFactory counting = new FunctionInvocationFactory(){
			@Override
			public <T> PolicyToPlatformFunctionInvocation<T> create(Object instance, Method m){
				PolicyToPlatformFunctionInvocation<T> real = FunctionInvocationFactory.defaultFactory().create(instance, m);
				return params -> {
					calls.incrementAndGet();
					return real.invoke(params);
				};
			}
		};
		FunctionProvider custom = FunctionProviderBuilder.builder(counting).build();
		FunctionSpec spec = custom.getFunction(STRING_EQUAL).orElseThrow();
		StringVal a = XacmlTypes.STRING.ofAny("a");

		BooleanVal result = spec.invoke(context(false), a, a);
		BooleanVal negated = custom.getFunction(NOT).orElseThrow().invoke(context(false), XacmlTypes.BOOLEAN.ofAny(true));

		assertThat(result.get()).isTrue();
		assertThat(negated.get()).isFalse();
		assertThat(calls.get()).isEqualTo(2);
	}

	@Test
	public void customSpecWithAStandardIdKeepsItsInvocation() throws EvaluationException
	{
		FunctionInvocation impl = mock(FunctionInvocation.class);
		FunctionSpec spec = FunctionSpecBuilder.builder(STRING_EQUAL)
				.param(XacmlTypes.STRING).param(XacmlTypes.STRING).build(XacmlTypes.BOOLEAN, impl);
		FunctionSpec not = FunctionSpecBuilder.builder(NOT).param(XacmlTypes.BOOLEAN).build(XacmlTypes.BOOLEAN, impl);
		when(impl.invoke(any(), any(), org.mockito.ArgumentMatchers.<List<Expression>>any())).thenReturn(XacmlTypes.BOOLEAN.ofAny(false));
		StringVal a = XacmlTypes.STRING.ofAny("a");

		BooleanVal result = spec.invoke(context(false), a, a);
		BooleanVal negated = not.invoke(context(false), XacmlTypes.BOOLEAN.ofAny(false));

		assertThat(result.get()).isFalse();
		assertThat(negated.get()).isFalse();
	}

	@Test
	public void concurrentPairCallsGiveTheirOwnResults() throws Exception
	{
		EvaluationContext context = context(false);
		FunctionSpec spec = function(INTEGER_EQUAL);
		ExecutorService pool = Executors.newFixedThreadPool(4);
		try{
			List<Future<Integer>> results = new ArrayList<>();
			for(int t = 0; t < 4; t++){
				int offset = t;
				results.add(pool.submit(() -> {
					int wrong = 0;
					for(int i = 0; i < 20_000; i++){
						IntegerVal left = XacmlTypes.INTEGER.ofAny(i + offset);
						IntegerVal right = XacmlTypes.INTEGER.ofAny((i % 3 == 0) ? i + offset : i);
						BooleanVal equal = spec.invoke(context, left, right);
						if(equal.get() != Objects.equals(left, right)){
							wrong++;
						}
					}
					return wrong;
				}));
			}
			for(Future<Integer> result : results){
				assertThat(result.get()).isEqualTo(0);
			}
		}finally{
			pool.shutdownNow();
		}
	}

	/** Review RVI1: a function-reference operand is an {@code Expression}, not a {@code ValueExp}. */
	@Test
	public void functionReferenceParameterWorksOnBothPaths()
	{
		EvaluationContext context = context(false);
		FunctionSpec spec = function(WITH_REFERENCE);
		Expression reference = new org.xacml4j.v30.policy.FunctionReference(function(STRING_EQUAL));
		IntegerVal one = XacmlTypes.INTEGER.ofAny(1);

		Outcome pair = pair(spec, context, reference, one);

		assertThat(pair).isEqualTo(list(spec, context, reference, one));
		assertThat(pair.value()).isEqualTo(XacmlTypes.BOOLEAN.ofAny(true));
	}

	/** Review RVI1: a function reference given to {@code string-equal} fails as on the reflective path, after both operands. */
	@Test
	public void functionReferenceOperandOfTheWrongTypeFailsAsOnTheReflectivePath() throws EvaluationException
	{
		EvaluationContext context = mock(EvaluationContext.class);
		FunctionSpec spec = function(STRING_EQUAL);
		Expression reference = new org.xacml4j.v30.policy.FunctionReference(function(STRING_EQUAL));
		List<String> order = new ArrayList<>();
		Expression second = mock(Expression.class);
		when(second.evaluate(any())).thenAnswer(i -> { order.add("second"); return XacmlTypes.STRING.ofAny("x"); });

		Outcome pair = pair(spec, context, reference, second);
		Outcome list = list(spec, context, reference, second);

		assertThat(pair.failure()).isEqualTo(FunctionInvocationException.class);
		assertThat(pair).isEqualTo(list);
		assertThat(order).containsExactly("second", "second");
	}

	/** Review RVI1: a binding may not reach a method that the reflective call cannot access. */
	@Test
	public void providerClassThatIsNotPublicFailsAsOnTheReflectivePath()
	{
		EvaluationContext context = context(false);
		FunctionSpec spec = function(PACKAGE_PRIVATE_EQUAL);
		StringVal a = XacmlTypes.STRING.ofAny("a");

		Outcome pair = pair(spec, context, a, a);

		assertThat(pair).isEqualTo(list(spec, context, a, a));
	}

	/** Review RVI2: a public method whose parameter types are not accessible to the binding. */
	@Test
	public void parametersOfATypeTheBindingCannotAccessWorkAsOnTheReflectivePath()
	{
		EvaluationContext context = context(false);
		FunctionSpec spec = function(PRIVATE_PARAMETERS);
		Expression a = new Secret();
		Expression b = new Secret();

		Outcome pair = pair(spec, context, a, b);

		assertThat(pair).isEqualTo(list(spec, context, a, b));
		assertThat(pair.value()).isEqualTo(XacmlTypes.BOOLEAN.ofAny(true));
	}

	/** A value class that only this test class can access. */
	private static final class Secret implements ValueExp
	{
		@Override
		public ValueExpTypeInfo getEvaluatesTo()
		{
			return XacmlTypes.STRING;
		}

		@Override
		public void accept(org.xacml4j.v30.ExpressionVisitor v)
		{
		}
	}

	/** A public function whose parameters have a private type; registered with the default factory. */
	@XacmlFunctionProvider(description = "DirectPairCallTest private parameter types")
	public static final class PrivateParameterFunctions
	{
		private PrivateParameterFunctions() {}

		@XacmlFuncSpec(id = PRIVATE_PARAMETERS)
		@XacmlFuncReturnType(typeId = "http://www.w3.org/2001/XMLSchema#boolean")
		public static BooleanVal privateParameters(
				@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#string") Secret a,
				@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#string") Secret b)
		{
			return BooleanVal.of(a != null && b != null);
		}
	}

	/** A function with a function-reference parameter; registered with the default factory. */
	@XacmlFunctionProvider(description = "DirectPairCallTest reference functions")
	public static final class ReferenceFunctions
	{
		private ReferenceFunctions() {}

		@XacmlFuncSpec(id = WITH_REFERENCE)
		@XacmlFuncReturnType(typeId = "http://www.w3.org/2001/XMLSchema#boolean")
		public static BooleanVal withReference(
				@XacmlFuncParamFunctionReference org.xacml4j.v30.policy.FunctionReference reference,
				@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#integer") IntegerVal value)
		{
			return BooleanVal.of(reference != null && value != null);
		}
	}

	/** Test functions that fail in each way a built-in function can; registered with the default factory. */
	@XacmlFunctionProvider(description = "DirectPairCallTest failing functions")
	public static final class FailingFunctions
	{
		private FailingFunctions() {}

		@XacmlFuncSpec(id = FAILING + "evaluation")
		@XacmlFuncReturnType(typeId = "http://www.w3.org/2001/XMLSchema#boolean")
		public static BooleanVal evaluation(
				@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#integer") IntegerVal a,
				@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#integer") IntegerVal b)
		{
			throw new EvaluationException(Status.missingAttribute("urn:test:attribute").build(), "missing");
		}

		@XacmlFuncSpec(id = FAILING + "runtime")
		@XacmlFuncReturnType(typeId = "http://www.w3.org/2001/XMLSchema#boolean")
		public static BooleanVal runtime(
				@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#integer") IntegerVal a,
				@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#integer") IntegerVal b)
		{
			throw new IllegalStateException("bug");
		}

		@XacmlFuncSpec(id = FAILING + "nested")
		@XacmlFuncReturnType(typeId = "http://www.w3.org/2001/XMLSchema#boolean")
		public static BooleanVal nested(
				@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#integer") IntegerVal a,
				@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#integer") IntegerVal b)
		{
			throw new IllegalStateException(new EvaluationException(Status.syntaxError().build(), "nested"));
		}

		@XacmlFuncSpec(id = FAILING + "error")
		@XacmlFuncReturnType(typeId = "http://www.w3.org/2001/XMLSchema#boolean")
		public static BooleanVal error(
				@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#integer") IntegerVal a,
				@XacmlFuncParam(typeId = "http://www.w3.org/2001/XMLSchema#integer") IntegerVal b)
		{
			throw new AssertionError("error");
		}
	}
}
