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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.ExpressionVisitor;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.ValueExpTypeInfo;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.FunctionInvocationException;
import org.xacml4j.v30.policy.FunctionReference;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.impl.PolicyToPlatformFunctionInvocation;
import org.xacml4j.v30.spi.observe.DecisionObservation;
import org.xacml4j.v30.spi.observe.DecisionObserver;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.Value;
import org.xacml4j.v30.types.ValueType;
import org.xacml4j.v30.types.XacmlTypes;

/** D52-ASTRA-1: a builder-owned parameter wrapper need not own immutable type metadata. */
class HigherOrderMetadataCompatibilityTest
{
    private static final String HIGHER = "urn:oasis:names:tc:xacml:3.0:function:";
    private static final FunctionProvider FUNCTIONS = FunctionProviderBuilder.builder().withDefaultFunctions().build();

    enum Shape {
        UNARY, BAG_FIRST, BAG_LAST;

        int count() { return this == UNARY ? 1 : 2; }
        int bagIndex() { return this == BAG_LAST ? 1 : 0; }
        List<String> order() { return this == UNARY ? List.of("bag") :
                this == BAG_FIRST ? List.of("bag", "scalar") : List.of("scalar", "bag"); }
    }

    static Stream<Arguments> cases() {
        return Stream.of("any-of", "all-of").flatMap(name -> Arrays.stream(Shape.values())
                .flatMap(shape -> Stream.of(false, true).map(validate -> Arguments.of(name, shape, validate))));
    }

    static Stream<Arguments> canonicalCases() {
        return cases().flatMap(row -> Stream.of(false, true).map(anyValue ->
                Arguments.of(row.get()[0], row.get()[1], row.get()[2], anyValue)));
    }

    static Stream<Arguments> mutableMetadataCases() {
        return cases().flatMap(row -> Stream.of(false, true).map(canonicalOperands ->
                Arguments.of(row.get()[0], row.get()[1], row.get()[2], canonicalOperands)));
    }

    static Stream<Arguments> operators() {
        return Stream.of("any-of", "all-of").flatMap(name ->
                Stream.of(false, true).map(validate -> Arguments.of(name, validate)));
    }

    static Stream<Arguments> binaryCases() {
        return canonicalCases().filter(row -> row.get()[1] != Shape.UNARY);
    }

    @ParameterizedTest(name = "{0} {1}, runtimeValidation={2}, canonicalOperands={3}")
    @MethodSource("mutableMetadataCases")
    void metadataChangedDuringEvaluationIsStillASyntaxError(
            String name, Shape shape, boolean validate, boolean canonicalOperands) {
        MutableType expected = new MutableType(canonicalOperands ? XacmlTypes.STRING.getTypeId() : "A");
        ValueType actual = canonicalOperands ? XacmlTypes.STRING : new MutableType("A");
        RecordingInvocation body = new RecordingInvocation(true);
        FunctionReference predicate = predicate(shape, expected, false, body);
        FunctionSpec operator = FUNCTIONS.getFunction(HIGHER + name).orElseThrow();
        List<String> evaluated = new ArrayList<>();
        BagOfValues empty = actual.emptyBag();
        Expression bag = tracked("bag", empty.getEvaluatesTo(), evaluated, () -> {
            expected.key = "B";
            return empty;
        });
        Apply apply = application(operator, predicate, shape, bag,
                tracked("scalar", XacmlTypes.STRING, evaluated, () -> StringVal.of("scalar")));
        assertTrue(apply.isTypeCorrect());
        assertTrue(operator.validateParameters(apply.getArguments()));
        assertEquals(List.of(), evaluated);

        List<Failure> failures = new ArrayList<>();
        EvaluationContext context = context(validate, failures);
        FunctionInvocationException failure = assertThrows(FunctionInvocationException.class, () -> apply.evaluate(context));

        assertEquals(StatusCodeId.SYNTAX_ERROR, failure.getStatus().getStatusCode().getValue());
        assertEquals("Invalid higher-order function arguments", failure.getMessage());
        assertEquals(java.util.Optional.of("Invalid higher-order function arguments"), failure.getStatus().getMessage());
        assertSame(operator, failure.getSpec());
        assertEquals(shape.order(), evaluated);
        assertEquals(List.of(), body.calls);
        assertEquals(1, failures.size());
        assertSame(context, failures.get(0).context());
        assertSame(operator, failures.get(0).function());
        assertSame(failure, failures.get(0).failure());
    }

    @ParameterizedTest(name = "{0} {1}, runtimeValidation={2}, anyValue={3}")
    @MethodSource("canonicalCases")
    void canonicalTypesAndAnyValueParametersKeepTheirPreparedCall(
            String name, Shape shape, boolean validate, boolean anyValue) throws Exception {
        RecordingInvocation body = new RecordingInvocation(name.equals("any-of"));
        FunctionReference predicate = predicate(shape, XacmlTypes.STRING, anyValue, body);
        StringVal entry = StringVal.of("entry");
        List<String> evaluated = new ArrayList<>();
        Apply apply = application(FUNCTIONS.getFunction(HIGHER + name).orElseThrow(), predicate, shape,
                tracked("bag", XacmlTypes.STRING.bagType(), evaluated,
                        () -> XacmlTypes.STRING.bagOf(entry, StringVal.of("unreached"))),
                tracked("scalar", XacmlTypes.STRING, evaluated, () -> StringVal.of("scalar")));
        // Outcomes alone cannot detect a correction that disables D52 altogether.
        Object plan = callSite(apply);
        assertNotNull(plan);
        assertEquals("org.xacml4j.v30.policy.HigherOrderCall", plan.getClass().getName());
        List<Failure> failures = new ArrayList<>();

        assertEquals(BooleanVal.of(name.equals("any-of")), apply.evaluate(context(validate, failures)));
        assertEquals(shape.order(), evaluated);
        assertEquals(List.of(tuple(shape, entry)), body.calls);
        assertEquals(List.of(), failures);
    }

    @ParameterizedTest(name = "{0} {1}, runtimeValidation={2}, anyValue={3}")
    @MethodSource("canonicalCases")
    void stableCustomTypesKeepTheirCheckedResults(
            String name, Shape shape, boolean validate, boolean anyValue) throws Exception {
        MutableType expected = new MutableType("A");
        MutableType actual = new MutableType("A");
        RecordingInvocation body = new RecordingInvocation(name.equals("any-of"));
        FunctionReference predicate = predicate(shape, expected, anyValue, body);
        Value<?> entry = actual.ofAny("entry");
        List<String> evaluated = new ArrayList<>();
        Apply apply = application(FUNCTIONS.getFunction(HIGHER + name).orElseThrow(), predicate, shape,
                tracked("bag", actual.bagType(), evaluated, () -> actual.bagOf(entry, actual.ofAny("unreached"))),
                tracked("scalar", XacmlTypes.STRING, evaluated, () -> StringVal.of("scalar")));
        assertTrue(apply.isTypeCorrect());
        List<Failure> failures = new ArrayList<>();

        assertEquals(BooleanVal.of(name.equals("any-of")), apply.evaluate(context(validate, failures)));
        assertEquals(shape.order(), evaluated);
        assertEquals(List.of(tuple(shape, entry)), body.calls);
        assertEquals(List.of(), failures);
        assertNull(callSite(apply), "Custom operand metadata must keep runtime signature checking");
    }

    @ParameterizedTest(name = "{0} {1}, runtimeValidation={2}, anyValue={3}")
    @MethodSource("binaryCases")
    void stableCustomScalarsKeepTheirCheckedResults(
            String name, Shape shape, boolean validate, boolean anyValue) throws Exception {
        MutableType expected = new MutableType("A");
        MutableType actual = new MutableType("A");
        RecordingInvocation body = new RecordingInvocation(name.equals("any-of"));
        FunctionSpecBuilder builder = FunctionSpecBuilder.builder("urn:test:d52:scalar-predicate");
        for (int i = 0; i < shape.count(); i++) {
            if (anyValue) builder.anyAttribute();
            else builder.param(i == shape.bagIndex() ? XacmlTypes.STRING : expected);
        }
        FunctionReference predicate = new FunctionReference(builder.build(XacmlTypes.BOOLEAN, body));
        StringVal entry = StringVal.of("entry");
        Value<?> scalar = actual.ofAny("scalar");
        List<String> evaluated = new ArrayList<>();
        Apply apply = application(FUNCTIONS.getFunction(HIGHER + name).orElseThrow(), predicate, shape,
                tracked("bag", XacmlTypes.STRING.bagType(), evaluated,
                        () -> XacmlTypes.STRING.bagOf(entry, StringVal.of("unreached"))),
                tracked("scalar", actual, evaluated, () -> scalar));
        assertTrue(apply.isTypeCorrect());
        List<Failure> failures = new ArrayList<>();

        assertEquals(BooleanVal.of(name.equals("any-of")), apply.evaluate(context(validate, failures)));
        assertEquals(shape.order(), evaluated);
        assertEquals(List.of(shape == Shape.BAG_FIRST ? List.of(entry, scalar) : List.of(scalar, entry)), body.calls);
        assertEquals(List.of(), failures);
        assertNull(callSite(apply), "Custom scalar metadata must keep runtime signature checking");
    }

    @ParameterizedTest(name = "{0}, runtimeValidation={1}")
    @MethodSource("operators")
    void aCustomOperatorFactoryKeepsItsInvocation(String name, boolean validate) throws Exception {
        List<String> calls = new ArrayList<>();
        FunctionInvocationFactory factory = new FunctionInvocationFactory() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> PolicyToPlatformFunctionInvocation<T> create(Object instance, Method method) {
                return args -> {
                    calls.add(method.getName());
                    return (T) BooleanVal.of(false);
                };
            }
        };
        FunctionSpec operator = FunctionProviderBuilder.builder(factory).withDefaultFunctions().build()
                .getFunction(HIGHER + name).orElseThrow();
        FunctionReference not = new FunctionReference(FUNCTIONS
                .getFunction("urn:oasis:names:tc:xacml:1.0:function:not").orElseThrow());
        List<String> evaluated = new ArrayList<>();
        Apply apply = Apply.builder(operator).param(not,
                tracked("bag", XacmlTypes.BOOLEAN.bagType(), evaluated,
                        () -> XacmlTypes.BOOLEAN.bagOf(BooleanVal.of(false)))).build();
        List<Failure> failures = new ArrayList<>();

        // The built-in body would return true; this result must come from the supplied factory.
        assertEquals(BooleanVal.of(false), apply.evaluate(context(validate, failures)));
        assertEquals(List.of(name.equals("any-of") ? "anyOf" : "allOf"), calls);
        assertEquals(List.of("bag"), evaluated);
        assertEquals(List.of(), failures);
        assertNull(callSite(apply));
    }

    private static FunctionReference predicate(Shape shape, ValueType bagElementType, boolean anyValue,
            RecordingInvocation invocation) {
        FunctionSpecBuilder builder = FunctionSpecBuilder.builder("urn:test:d52:predicate");
        for (int i = 0; i < shape.count(); i++) {
            if (anyValue) builder.anyAttribute();
            else builder.param(i == shape.bagIndex() ? bagElementType : XacmlTypes.STRING);
        }
        return new FunctionReference(builder.build(XacmlTypes.BOOLEAN, invocation));
    }

    private static Apply application(FunctionSpec operator, FunctionReference predicate, Shape shape,
            Expression bag, Expression scalar) {
        return Apply.builder(operator).params(switch (shape) {
            case UNARY -> List.of(predicate, bag);
            case BAG_FIRST -> List.of(predicate, bag, scalar);
            case BAG_LAST -> List.of(predicate, scalar, bag);
        }).build();
    }

    private static List<Expression> tuple(Shape shape, Value<?> entry) {
        return switch (shape) {
            case UNARY -> List.of(entry);
            case BAG_FIRST -> List.of(entry, StringVal.of("scalar"));
            case BAG_LAST -> List.of(StringVal.of("scalar"), entry);
        };
    }

    private static Object callSite(Apply apply) throws Exception {
        Field field = Apply.class.getDeclaredField("callSite");
        field.setAccessible(true);
        return field.get(apply);
    }

    private static Expression tracked(String name, ValueExpTypeInfo type, List<String> evaluated,
            Supplier<? extends Expression> value) {
        return new Expression() {
            @Override public ValueExpTypeInfo getEvaluatesTo() { return type; }
            @Override @SuppressWarnings("unchecked")
            public <T extends Expression> T evaluate(EvaluationContext context) {
                evaluated.add(name);
                return (T) value.get();
            }
            @Override public void accept(ExpressionVisitor visitor) {}
        };
    }

    private record Failure(EvaluationContext context, FunctionSpec function, Throwable failure) {}

    private static EvaluationContext context(boolean validate, List<Failure> failures) {
        EvaluationContext context = mock(EvaluationContext.class);
        when(context.isValidateFuncParamsAtRuntime()).thenReturn(validate);
        DecisionObserver<String> observer = new DecisionObserver<>() {
            @Override public String requestStarted(PolicyDecisionPoint pdp, RequestContext request) { return "request"; }
            @Override
            public void functionFailed(String current, EvaluationContext evaluation, FunctionSpec function, Throwable failure) {
                failures.add(new Failure(evaluation, function, failure));
            }
        };
        when(context.getDecisionObservation()).thenReturn(DecisionObservation.start(observer, "request",
                RequestContext.builder().build(), 1, 1));
        return context;
    }

    private static final class RecordingInvocation implements FunctionInvocation {
        private final boolean result;
        private final List<List<Expression>> calls = new ArrayList<>();

        RecordingInvocation(boolean result) { this.result = result; }

        @Override
        public ValueExp invoke(FunctionSpec spec, EvaluationContext context, Expression... arguments) {
            return invoke(spec, context, Arrays.asList(arguments));
        }

        @Override
        public ValueExp invoke(FunctionSpec spec, EvaluationContext context, List<Expression> arguments) {
            calls.add(List.copyOf(arguments));
            return BooleanVal.of(result);
        }
    }

    private static final class MutableType implements ValueType {
        private String key;

        MutableType(String key) { this.key = key; }

        @Override public String getTypeId() { return "urn:test:d52:" + key; }
        @Override public String getShortTypeId() { return key; }
        @Override public Set<String> getTypeIdAliases() { return Set.of(); }
        @Override @SuppressWarnings("unchecked")
        public <T extends Value<?>> T ofAny(Object value, Object... params) {
            return (T) new CustomValue(this, value.toString());
        }
        @Override public boolean equals(Object other) {
            // A caller-owned parameter descriptor may accept the canonical operand type until it changes.
            return other == XacmlTypes.STRING ? key.equals(XacmlTypes.STRING.getTypeId())
                    : other instanceof MutableType type && key.equals(type.key);
        }
        @Override public int hashCode() {
            return key.equals(XacmlTypes.STRING.getTypeId()) ? XacmlTypes.STRING.hashCode() : key.hashCode();
        }
    }

    private static final class CustomValue extends Value.OtherValue<String> {
        CustomValue(ValueType type, String value) { super(type, value); }
    }
}

