package org.xacml4j.v30.policy.function.regex;

/*
 * #%L
 * Xacml4J Core
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.BoundCall;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.FunctionInvocationFactory;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.policy.function.impl.PolicyToPlatformFunctionInvocation;
import org.xacml4j.v30.policy.function.regex.jdk.JdkRegexEngine;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.IntegerVal;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.XacmlTypes;

class RegexBindingTest
{
	private static final String REGEX = "urn:oasis:names:tc:xacml:1.0:function:string-regexp-match";

	@Test
	void customFactoryHandlesLiteralAndDynamicPatterns(){
		FunctionInvocationFactory factory = new FunctionInvocationFactory(){
			@Override
			@SuppressWarnings("unchecked")
			public <T> PolicyToPlatformFunctionInvocation<T> create(Object instance, Method method){
				return args -> (T)BooleanVal.of(false);
			}
		};
		FunctionSpec function = FunctionProviderBuilder.builder(factory).build().getFunction(REGEX).orElseThrow();
		EvaluationContext context = mock(EvaluationContext.class);
		Expression dynamic = mock(Expression.class);
		doReturn(XacmlTypes.STRING).when(dynamic).getEvaluatesTo();
		doReturn(StringVal.of("a")).when(dynamic).evaluate(context);
		Apply literal = Apply.builder(function).param(StringVal.of("a"), StringVal.of("a")).build();
		Apply computed = Apply.builder(function).param(dynamic, StringVal.of("a")).build();

		assertEquals(BooleanVal.of(false), literal.evaluate(context));
		assertEquals(BooleanVal.of(false), computed.evaluate(context));
	}

	@Test
	void boundCallsKeepTheOrdinaryParameterTypeAndNullOutcomes(){
		FunctionSpec function = FunctionProviderBuilder.builder().build().getFunction(REGEX).orElseThrow();
		EvaluationContext context = mock(EvaluationContext.class);
		StringVal pattern = StringVal.of("1");
		BoundCall bound = function.bind(pattern).orElseThrow();
		BooleanVal ordinaryMatch = function.invoke(context, pattern, StringVal.of("1"));
		BooleanVal boundMatch = bound.invoke(context, StringVal.of("1"));
		assertEquals(ordinaryMatch, boundMatch);
		for(Expression operand : Arrays.asList(null, IntegerVal.of(1))){
			EvaluationException ordinary = assertThrows(EvaluationException.class,
					() -> function.invoke(context, pattern, operand));
			EvaluationException retained = assertThrows(EvaluationException.class,
					() -> bound.invoke(context, operand));
			assertEquals(ordinary.getStatus().getStatusCode(), retained.getStatus().getStatusCode());
			assertEquals(ordinary.getStatus().getMessage(), retained.getStatus().getMessage());
		}
		Expression changing = mock(Expression.class);
		doReturn(IntegerVal.of(1), StringVal.of("1")).when(changing).evaluate(context);
		assertThrows(EvaluationException.class, () -> bound.invoke(context, changing));
	}

	@Test
	void aValidatedLiteralSurvivesCacheChurn(){
		FunctionSpec function = onceOnlyFunction();
		EvaluationContext context = mock(EvaluationContext.class);
		when(context.isValidateFuncParamsAtRuntime()).thenReturn(true);
		Apply literal = Apply.builder(function).param(StringVal.of("^a$"), StringVal.of("a")).build();
		assertEquals(BooleanVal.of(true), literal.evaluate(context));
		for(int i = 0; i < 4; i++){
			String text = "value" + i;
			assertEquals(BooleanVal.of(true), function.invoke(context, StringVal.of(text), StringVal.of(text)));
		}
		assertEquals(BooleanVal.of(true), literal.evaluate(context));
	}

	@Test
	void concurrentColdLiteralCallsRemainValidDuringCacheChurn() throws Exception {
		FunctionSpec function = onceOnlyFunction();
		EvaluationContext context = mock(EvaluationContext.class);
		BoundCall bound = function.bind(StringVal.of("^a$")).orElseThrow();
		CountDownLatch start = new CountDownLatch(1);
		try(var executor = Executors.newFixedThreadPool(8)){
			List<Callable<BooleanVal>> calls = new ArrayList<>();
			for(int thread = 0; thread < 6; thread++){
				calls.add(() -> {
					start.await();
					for(int i = 0; i < 30; i++){
						BooleanVal value = bound.invoke(context, StringVal.of("a"));
						if(!value.equals(BooleanVal.of(true))) return value;
					}
					return BooleanVal.of(true);
				});
			}
			for(int thread = 0; thread < 2; thread++){
				final int worker = thread;
				calls.add(() -> {
					start.await();
					for(int i = 0; i < 30; i++){
						String text = "other-" + worker + "-" + i;
						BooleanVal value = function.invoke(context, StringVal.of(text), StringVal.of(text));
						if(!value.equals(BooleanVal.of(true))) return value;
					}
					return BooleanVal.of(true);
				});
			}
			List<Future<BooleanVal>> results = new ArrayList<>();
			for(Callable<BooleanVal> call : calls) results.add(executor.submit(call));
			start.countDown();
			for(Future<BooleanVal> result : results) assertEquals(BooleanVal.of(true), result.get());
		}
	}

	private static FunctionSpec onceOnlyFunction(){
		RegexEngine engine = new RegexEngine(){
			private final Set<String> compiled = ConcurrentHashMap.newKeySet();
			private final RegexEngine jdk = new JdkRegexEngine();
			@Override public String name(){ return "once-only"; }
			@Override public Set<RegexProfile> profiles(){ return jdk.profiles(); }
			@Override public CompiledRegex compile(String pattern, RegexProfile profile, RegexLimits limits){
				if(!compiled.add(pattern)) throw RegexCompileException.malformed("compiled twice");
				return jdk.compile(pattern, profile, limits);
			}
		};
		return org.xacml4j.v30.policy.function.FunctionProvider.builder()
				.withRegexEngine(engine, RegexProfile.LEGACY, RegexLimits.builder().cacheSize(2).build())
				.withDefaultFunctions().build().getFunction(REGEX).orElseThrow();
	}
}
