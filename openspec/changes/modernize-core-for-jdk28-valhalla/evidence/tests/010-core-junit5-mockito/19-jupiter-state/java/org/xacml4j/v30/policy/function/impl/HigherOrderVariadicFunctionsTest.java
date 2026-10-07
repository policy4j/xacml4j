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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.Status;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.Condition;
import org.xacml4j.v30.policy.ConditionResult;
import org.xacml4j.v30.policy.FunctionReference;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.FunctionInvocationFactory;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.policy.function.XacmlFuncParam;
import org.xacml4j.v30.policy.function.XacmlFuncReturnType;
import org.xacml4j.v30.policy.function.XacmlFuncSpec;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.Value;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * XACML 3.0 A.3.12: substitute the one bag in its original argument position;
 * any-of-any takes the cross product of every bag. All calls use Apply and
 * the default provider with runtime parameter checking both disabled/enabled.
 * https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html
 */
public class HigherOrderVariadicFunctionsTest
{
	private static final FunctionProvider FUNCTIONS =
			FunctionProviderBuilder.builder().withDefaultFunctions().build();
	private boolean runtimeValidation;
	private EvaluationContext context;
	private ArgumentCaptor<Status> status;

	public void initHigherOrderVariadicFunctionsTest(boolean runtimeValidation)
	{
		this.runtimeValidation = runtimeValidation;
	}

	public static Collection<Object[]> modes()
	{
		return Arrays.asList(new Object[]{false}, new Object[]{true});
	}

	@BeforeEach
	public void context()
	{
		context = mock(EvaluationContext.class, invocation ->
				invocation.getMethod().getName().equals("isValidateFuncParamsAtRuntime")
						? runtimeValidation : RETURNS_DEFAULTS.answer(invocation));
		status = ArgumentCaptor.forClass(Status.class);
	}

	@AfterEach
	public void verifyContextInteractions()
	{
		verify(context, atLeast(0)).isValidateFuncParamsAtRuntime();
		verifyNoMoreInteractions(context);
	}

	@MethodSource("modes")
	@ParameterizedTest(name = "runtimeValidation={0}")
	public void anyOfSpecExample(boolean runtimeValidation) throws Exception
	{
		initHigherOrderVariadicFunctionsTest(runtimeValidation);
		assertValue(BooleanVal.of(true), "any-of", ref("string-equal"), string("Paul"),
				strings("John", "Paul", "George", "Ringo"));
	}

	@MethodSource("modes")
	@ParameterizedTest(name = "runtimeValidation={0}")
	public void anyOfThreeArgumentPredicate(boolean runtimeValidation) throws Exception
	{
		initHigherOrderVariadicFunctionsTest(runtimeValidation);
		assertValue(BooleanVal.of(true), "any-of", orderedStrings(), string("prefix"),
				string("suffix"), strings("other", "match"));
	}

	@MethodSource("modes")
	@ParameterizedTest(name = "runtimeValidation={0}")
	public void anyOfBagFirst(boolean runtimeValidation) throws Exception
	{
		initHigherOrderVariadicFunctionsTest(runtimeValidation);
		assertValue(BooleanVal.of(true), "any-of", ref("string-equal"), strings("a", "b"), string("b"));
	}

	@MethodSource("modes")
	@ParameterizedTest(name = "runtimeValidation={0}")
	public void anyOfPreservesEveryBagPosition(boolean runtimeValidation) throws Exception
	{
		initHigherOrderVariadicFunctionsTest(runtimeValidation);
		assertValue(BooleanVal.of(true), "any-of", orderedStrings(), strings("other", "prefix"),
				string("suffix"), string("match"));
		assertValue(BooleanVal.of(true), "any-of", orderedStrings(), string("prefix"),
				strings("other", "suffix"), string("match"));
		assertValue(BooleanVal.of(false), "any-of", orderedStrings(), string("suffix"),
				string("prefix"), strings("match"));
	}

	@MethodSource("modes")
	@ParameterizedTest(name = "runtimeValidation={0}")
	public void allOfSpecExample(boolean runtimeValidation) throws Exception
	{
		initHigherOrderVariadicFunctionsTest(runtimeValidation);
		assertValue(BooleanVal.of(true), "all-of", ref("integer-greater-than"), integer(10), integers(9, 3, 4, 2));
	}

	@MethodSource("modes")
	@ParameterizedTest(name = "runtimeValidation={0}")
	public void allOfPreservesEveryBagPosition(boolean runtimeValidation) throws Exception
	{
		initHigherOrderVariadicFunctionsTest(runtimeValidation);
		assertValue(BooleanVal.of(true), "all-of", orderedStrings(), strings("prefix", "prefix"),
				string("suffix"), string("match"));
		assertValue(BooleanVal.of(true), "all-of", orderedStrings(), string("prefix"),
				strings("suffix", "suffix"), string("match"));
		assertValue(BooleanVal.of(false), "all-of", orderedStrings(), string("prefix"),
				string("suffix"), strings("match", "other"));
	}

	@MethodSource("modes")
	@ParameterizedTest(name = "runtimeValidation={0}")
	public void unaryPredicateNeedsOnlyOneBag(boolean runtimeValidation) throws Exception
	{
		initHigherOrderVariadicFunctionsTest(runtimeValidation);
		FunctionReference unary = testRef("nonEmpty", StringVal.class);
		assertValue(BooleanVal.of(true), "any-of", unary, strings("", "a"));
		assertValue(BooleanVal.of(false), "all-of", unary, strings("a", ""));
	}

	@MethodSource("modes")
	@ParameterizedTest(name = "runtimeValidation={0}")
	public void variadicPredicateAcceptsMatchingOperandTypes(boolean runtimeValidation) throws Exception
	{
		initHigherOrderVariadicFunctionsTest(runtimeValidation);
		assertValue(BooleanVal.of(true), "any-of", ref("and"), BooleanVal.of(true),
				XacmlTypes.BOOLEAN.bagOf(BooleanVal.of(false), BooleanVal.of(true)), BooleanVal.of(true));
	}

	@MethodSource("modes")
	@ParameterizedTest(name = "runtimeValidation={0}")
	public void anyOfAnySpecExample(boolean runtimeValidation) throws Exception
	{
		initHigherOrderVariadicFunctionsTest(runtimeValidation);
		// The normative example has two bags, not a primitive plus two bags.
		assertValue(BooleanVal.of(true), "any-of-any", ref("string-equal"),
				strings("Ringo", "Mary"), strings("John", "Paul", "George", "Ringo"));
	}

	@MethodSource("modes")
	@ParameterizedTest(name = "runtimeValidation={0}")
	public void anyOfAnyMixedCrossProduct(boolean runtimeValidation) throws Exception
	{
		initHigherOrderVariadicFunctionsTest(runtimeValidation);
		assertValue(BooleanVal.of(true), "any-of-any", orderedStrings(), string("prefix"),
				strings("other", "suffix"), strings("other", "match"));
		assertValue(BooleanVal.of(false), "any-of-any", orderedStrings(), string("prefix"),
				strings("suffix"), strings("other"));
	}

	@MethodSource("modes")
	@ParameterizedTest(name = "runtimeValidation={0}")
	public void anyOfAnyThreeBags(boolean runtimeValidation) throws Exception
	{
		initHigherOrderVariadicFunctionsTest(runtimeValidation);
		assertValue(BooleanVal.of(true), "any-of-any", orderedStrings(), strings("other", "prefix"),
				strings("other", "suffix"), strings("other", "match"));
	}

	@MethodSource("modes")
	@ParameterizedTest(name = "runtimeValidation={0}")
	public void anyOfAnyAllowsAllPrimitiveOperands(boolean runtimeValidation) throws Exception
	{
		initHigherOrderVariadicFunctionsTest(runtimeValidation);
		assertValue(BooleanVal.of(true), "any-of-any", ref("string-equal"), string("a"), string("a"));
		assertValue(BooleanVal.of(false), "any-of-any", ref("string-equal"), string("a"), string("b"));
	}

	@MethodSource("modes")
	@ParameterizedTest(name = "runtimeValidation={0}")
	public void mapSpecExample(boolean runtimeValidation) throws Exception
	{
		initHigherOrderVariadicFunctionsTest(runtimeValidation);
		assertValue(strings("hello", "world!"), "map", ref("string-normalize-to-lower-case"), strings("Hello", "World!"));
	}

	@MethodSource("modes")
	@ParameterizedTest(name = "runtimeValidation={0}")
	public void mapConcatenateWithPrimitive(boolean runtimeValidation) throws Exception
	{
		initHigherOrderVariadicFunctionsTest(runtimeValidation);
		assertValue(strings("Hello A", "Hello B"), "map", ref("string-concatenate", "2.0"),
				string("Hello "), strings("A", "B"));
	}

	@MethodSource("modes")
	@ParameterizedTest(name = "runtimeValidation={0}")
	public void mapBagFirstPreservesOrderAndMultiplicity(boolean runtimeValidation) throws Exception
	{
		initHigherOrderVariadicFunctionsTest(runtimeValidation);
		assertValue(strings("A!", "A!", "B!"), "map", ref("string-concatenate", "2.0"),
				strings("A", "A", "B"), string("!"));
	}

	@MethodSource("modes")
	@ParameterizedTest(name = "runtimeValidation={0}")
	public void emptyBagsHaveIdentityResults(boolean runtimeValidation) throws Exception
	{
		initHigherOrderVariadicFunctionsTest(runtimeValidation);
		assertValue(BooleanVal.of(false), "any-of", ref("string-equal"), string("a"), strings());
		assertValue(BooleanVal.of(true), "all-of", ref("string-equal"), string("a"), strings());
		assertValue(BooleanVal.of(false), "any-of-any", ref("string-equal"), strings(), strings("a"));
		assertValue(strings(), "map", ref("string-normalize-to-lower-case"), strings());
	}

	@MethodSource("modes")
	@ParameterizedTest(name = "runtimeValidation={0}")
	public void anyOfNoBagIsSyntaxErrorAndIndeterminate(boolean runtimeValidation) throws Exception
	{
		initHigherOrderVariadicFunctionsTest(runtimeValidation);
		assertInvalid("any-of", ref("string-equal"), string("a"), string("a"));
	}

	@MethodSource("modes")
	@ParameterizedTest(name = "runtimeValidation={0}")
	public void allOfNoBagIsSyntaxErrorAndIndeterminate(boolean runtimeValidation) throws Exception
	{
		initHigherOrderVariadicFunctionsTest(runtimeValidation);
		assertInvalid("all-of", ref("string-equal"), string("a"), string("a"));
	}

	@MethodSource("modes")
	@ParameterizedTest(name = "runtimeValidation={0}")
	public void mapNoBagIsSyntaxErrorAndIndeterminate(boolean runtimeValidation) throws Exception
	{
		initHigherOrderVariadicFunctionsTest(runtimeValidation);
		assertInvalid("map", ref("string-normalize-to-lower-case"), string("a"));
	}

	@MethodSource("modes")
	@ParameterizedTest(name = "runtimeValidation={0}")
	public void anyOfTwoBagsIsSyntaxErrorAndIndeterminate(boolean runtimeValidation) throws Exception
	{
		initHigherOrderVariadicFunctionsTest(runtimeValidation);
		assertInvalid("any-of", ref("string-equal"), strings("a"), strings("a"));
	}

	@MethodSource("modes")
	@ParameterizedTest(name = "runtimeValidation={0}")
	public void allOfTwoBagsIsSyntaxErrorAndIndeterminate(boolean runtimeValidation) throws Exception
	{
		initHigherOrderVariadicFunctionsTest(runtimeValidation);
		assertInvalid("all-of", ref("string-equal"), strings("a"), strings("a"));
	}

	@MethodSource("modes")
	@ParameterizedTest(name = "runtimeValidation={0}")
	public void mapTwoBagsIsSyntaxErrorAndIndeterminate(boolean runtimeValidation) throws Exception
	{
		initHigherOrderVariadicFunctionsTest(runtimeValidation);
		assertInvalid("map", ref("string-concatenate", "2.0"), strings("a"), strings("b"));
	}

	@MethodSource("modes")
	@ParameterizedTest(name = "runtimeValidation={0}")
	public void missingOperandsAreSyntaxErrorAndIndeterminate(boolean runtimeValidation) throws Exception
	{
		initHigherOrderVariadicFunctionsTest(runtimeValidation);
		assertInvalid("any-of", ref("string-equal"));
		assertInvalid("all-of", ref("string-equal"));
		assertInvalid("any-of-any", ref("string-equal"));
		assertInvalid("map", ref("string-normalize-to-lower-case"));
	}

	@MethodSource("modes")
	@ParameterizedTest(name = "runtimeValidation={0}")
	public void packetBinaryPredicateWithThreeOperandsIsArityError(boolean runtimeValidation) throws Exception
	{
		initHigherOrderVariadicFunctionsTest(runtimeValidation);
		assertInvalid("any-of-any", ref("string-equal"), string("Ringo"),
				strings("John", "Paul"), strings("George", "Ringo"));
	}

	@MethodSource("modes")
	@ParameterizedTest(name = "runtimeValidation={0}")
	public void referencedPredicateTypesAndReturnTypeAreChecked(boolean runtimeValidation) throws Exception
	{
		initHigherOrderVariadicFunctionsTest(runtimeValidation);
		assertInvalid("any-of", ref("string-equal"), integer(1), strings("a"));
		assertInvalid("any-of", ref("string-equal"), string("a"), integers());
		assertInvalid("any-of", ref("string-normalize-to-lower-case"), strings("a"));
	}

	@MethodSource("modes")
	@ParameterizedTest(name = "runtimeValidation={0}")
	public void mapRejectsBagReturningFunctionAtValidation(boolean runtimeValidation)
	{
		initHigherOrderVariadicFunctionsTest(runtimeValidation);
		FunctionSpec spec = higher("map");
		List<Expression> arguments = Arrays.asList(ref("string-bag"), strings("a"));
		assertFalse(spec.validateParameters(arguments));
		SyntaxException error = assertThrows(SyntaxException.class, () -> spec.validateParametersAndThrow(arguments));
		assertEquals(StatusCodeId.SYNTAX_ERROR, error.getStatus().getStatusCode().getValue());
	}

	@MethodSource("modes")
	@ParameterizedTest(name = "runtimeValidation={0}")
	public void mapRejectsBagReturningFunctionDuringApplyTypeResolution(boolean runtimeValidation)
	{
		initHigherOrderVariadicFunctionsTest(runtimeValidation);
		for(String version : new String[]{"3.0", "1.0"}){
			FunctionSpec spec = FUNCTIONS.getFunction("urn:oasis:names:tc:xacml:" + version + ":function:map").get();
			List<Expression> arguments = Arrays.asList(ref("string-bag"), strings("a"));
			SyntaxException validationError = assertThrows(SyntaxException.class,
					() -> spec.validateParametersAndThrow(arguments));
			assertEquals(StatusCodeId.SYNTAX_ERROR, validationError.getStatus().getStatusCode().getValue());
			Apply apply = Apply.builder(spec).params(arguments).build();
			assertEquals(arguments, apply.getArguments());
			SyntaxException resolutionError = assertThrows(SyntaxException.class, apply::getEvaluatesTo);
			assertEquals(validationError.getClass(), resolutionError.getClass());
			assertEquals(validationError.getMessage(), resolutionError.getMessage());
			assertEquals(StatusCodeId.SYNTAX_ERROR, resolutionError.getStatus().getStatusCode().getValue());
			EvaluationException invocationError = assertThrows(EvaluationException.class, () -> apply.evaluate(context));
			assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, invocationError.getStatus().getStatusCode().getValue());
		}
	}

	private void assertValue(ValueExp expected, String operation, Expression... arguments) throws Exception
	{
		FunctionSpec spec = higher(operation);
		spec.validateParametersAndThrow(Arrays.asList(arguments));
		Apply apply = Apply.builder(spec).param(arguments).build();
		assertEquals(expected.getEvaluatesTo(), apply.getEvaluatesTo());
		assertEquals(expected, apply.evaluate(context));
		verify(context, atLeastOnce()).isValidateFuncParamsAtRuntime();
	}

	private void assertInvalid(String operation, Expression... arguments) throws Exception
	{
		FunctionSpec spec = higher(operation);
		List<Expression> params = Arrays.asList(arguments);
		assertFalse(spec.validateParameters(params), "Invalid parameters accepted for " + operation);
		SyntaxException validationError = assertThrows(SyntaxException.class, () -> spec.validateParametersAndThrow(params));
		assertEquals(StatusCodeId.SYNTAX_ERROR, validationError.getStatus().getStatusCode().getValue());
		Apply apply = Apply.builder(spec).param(arguments).build();
		EvaluationException invocationError = assertThrows(EvaluationException.class, () -> apply.evaluate(context));
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, invocationError.getStatus().getStatusCode().getValue());
		Expression predicate = operation.equals("map")
				? Apply.builder(FUNCTIONS.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-is-in").get())
						.param(string("a"), apply).build() : apply;
		status = ArgumentCaptor.forClass(Status.class);
		long previousStatuses = mockingDetails(context).getInvocations().stream()
				.filter(invocation -> invocation.getMethod().getName().equals("setEvaluationStatus")).count();
		assertEquals(ConditionResult.INDETERMINATE, new Condition(predicate).evaluate(context));
		verify(context, times((int)previousStatuses + 1)).setEvaluationStatus(status.capture());
		verify(context, atLeastOnce()).isValidateFuncParamsAtRuntime();
		assertTrue(!status.getAllValues().isEmpty(), "Indeterminate must carry an evaluation status");
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, status.getValue().getStatusCode().getValue());
	}

	private static FunctionSpec higher(String operation)
	{
		return FUNCTIONS.getFunction("urn:oasis:names:tc:xacml:3.0:function:" + operation).get();
	}

	private static FunctionReference ref(String operation)
	{
		return ref(operation, "1.0");
	}

	private static FunctionReference ref(String operation, String version)
	{
		return new FunctionReference(FUNCTIONS.getFunction("urn:oasis:names:tc:xacml:" + version + ":function:" + operation).get());
	}

	private static StringVal string(String value)
	{
		return StringVal.of(value);
	}

	private static Value integer(long value)
	{
		return XacmlTypes.INTEGER.ofAny(value);
	}

	private static BagOfValues strings(String... values)
	{
		return XacmlTypes.STRING.bagBuilder().value((Object[]) values).build();
	}

	private static BagOfValues integers(Integer... values)
	{
		return XacmlTypes.INTEGER.bagBuilder().value((Object[]) values).build();
	}

	private static FunctionReference orderedStrings() throws Exception
	{
		return testRef("orderedStrings", StringVal.class, StringVal.class, StringVal.class);
	}

	private static FunctionReference testRef(String name, Class<?>... types) throws Exception
	{
		return new FunctionReference(new JavaMethodToFunctionSpecConverter(FunctionInvocationFactory.defaultFactory())
				.createFunctionSpec(Predicates.class.getMethod(name, types)));
	}

	public static class Predicates
	{
		@XacmlFuncSpec(id="urn:xacml4j:test:ordered-strings")
		@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
		public static BooleanVal orderedStrings(
				@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal prefix,
				@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal suffix,
				@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal element)
		{
			return BooleanVal.of(prefix.get().equals("prefix") && suffix.get().equals("suffix") && element.get().equals("match"));
		}

		@XacmlFuncSpec(id="urn:xacml4j:test:non-empty")
		@XacmlFuncReturnType(typeId="http://www.w3.org/2001/XMLSchema#boolean")
		public static BooleanVal nonEmpty(
				@XacmlFuncParam(typeId="http://www.w3.org/2001/XMLSchema#string") StringVal value)
		{
			return BooleanVal.of(!value.get().isEmpty());
		}
	}
}
