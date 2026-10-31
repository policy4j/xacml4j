package org.xacml4j.v30.policy;

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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.BagStorage;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.policy.function.FunctionInvocation;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.policy.function.FunctionSpecBuilder;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.Value;
import org.xacml4j.v30.types.ValueType;
import org.xacml4j.v30.types.XacmlTypes;

class MatchStorageFastPathTest
{
	private static final String PREFIX = "urn:oasis:names:tc:xacml:1.0:function:";
	private static final FunctionProvider FUNCTIONS = FunctionProviderBuilder.builder().build();

	static Stream<Arguments> comparisons() {
		return Stream.of(
				Arguments.of("integer-equal", XacmlTypes.INTEGER, 2L, List.of(1L, 2L, 1L)),
				Arguments.of("integer-greater-than", XacmlTypes.INTEGER, 2L, List.of(3L, 1L)),
				Arguments.of("integer-less-than", XacmlTypes.INTEGER, 2L, List.of(1L, 3L)),
				Arguments.of("integer-greater-than-or-equal", XacmlTypes.INTEGER, 2L, List.of(3L, 2L)),
				Arguments.of("integer-less-than-or-equal", XacmlTypes.INTEGER, 2L, List.of(1L, 2L)),
				Arguments.of("double-equal", XacmlTypes.DOUBLE, Double.NaN, List.of(Double.NaN)),
				Arguments.of("double-equal", XacmlTypes.DOUBLE, 0.0, List.of(-0.0)),
				Arguments.of("double-greater-than", XacmlTypes.DOUBLE, 2.0, List.of(Double.NaN, 1.0)),
				Arguments.of("double-less-than", XacmlTypes.DOUBLE, 2.0, List.of(Double.NaN, 3.0)),
				Arguments.of("double-greater-than-or-equal", XacmlTypes.DOUBLE, Double.NaN, List.of(Double.NaN)),
				Arguments.of("double-less-than-or-equal", XacmlTypes.DOUBLE, -0.0, List.of(0.0)),
				Arguments.of("boolean-equal", XacmlTypes.BOOLEAN, true, List.of(false, true, false)),
				Arguments.of("string-equal", XacmlTypes.STRING, "a", List.of("b", "a")),
				Arguments.of("urn:oasis:names:tc:xacml:3.0:function:string-equal-ignore-case",
						XacmlTypes.STRING, "\u0130", List.of("i")),
				Arguments.of("date-equal", XacmlTypes.DATE, "2002-03-22", List.of("2003-01-01", "2002-03-22")),
				Arguments.of("dateTime-equal", XacmlTypes.DATETIME, "2002-03-22T08:23:47-05:00", List.of("2002-03-22T08:23:47-05:00")),
				Arguments.of("time-equal", XacmlTypes.TIME, "08:23:47-05:00", List.of("08:23:47-05:00")),
				Arguments.of("dayTimeDuration-equal", XacmlTypes.DAYTIMEDURATION, "P1DT2H", List.of("P1DT2H")),
				Arguments.of("yearMonthDuration-equal", XacmlTypes.YEARMONTHDURATION, "P1Y2M", List.of("P1Y2M")),
				Arguments.of("anyURI-equal", XacmlTypes.ANYURI, "https://example.org/a", List.of("https://example.org/a")),
				Arguments.of("x500Name-equal", XacmlTypes.X500NAME, "cn=a, o=example", List.of("cn=a, o=example")),
				Arguments.of("rfc822Name-equal", XacmlTypes.RFC822NAME, "anne@example.com", List.of("anne@example.com")),
				Arguments.of("hexBinary-equal", XacmlTypes.HEXBINARY, "0FB7", List.of("0FB7")),
				Arguments.of("base64Binary-equal", XacmlTypes.BASE64BINARY, "AQID", List.of("AQID")),
				Arguments.of("entity-equal", XacmlTypes.ENTITY, List.of(), List.of(List.of())));
	}

	@ParameterizedTest(name = "{0}: {2} in {3}")
	@MethodSource("comparisons")
	void builtInStorageScanAgreesWithElementCalls(String id, ValueType type, Object raw, List<Object> inputs) {
		FunctionSpec spec = FUNCTIONS.getFunction(id.startsWith("urn:") ? id : PREFIX + id).orElseThrow();
		Value<?> literal = type.ofAny(raw);
		BagOfValues.Builder builder = type.bagBuilder();
		for (Object value : inputs) builder.attribute(type.ofAny(value));
		BagOfValues bag = builder.build();
		EvaluationContext context = mock(EvaluationContext.class);
		boolean expected = false;
		for (Value<?> value : bag) {
			BooleanVal result = spec.invoke(context, literal, value);
			if (result.get()) { expected = true; break; }
		}
		BagStorage.Scan scan = id.endsWith("string-equal-ignore-case") ? BagStorage.Scan.NONE
				: (expected ? BagStorage.Scan.MATCH : BagStorage.Scan.NO_MATCH);
		assertEquals(scan, spec.scan(context, literal, bag));
		AttributeDesignatorKey key = AttributeDesignatorKey.builder("test").category(CategoryId.RESOURCE)
				.dataType(type).build();
		AttributeDesignator ref = AttributeDesignator.builder().key(key).mustBePresent(false).build();
		when(context.resolve(key)).thenReturn(Optional.of(bag));
		Match match = Match.builder().predicate(spec).attribute(literal).attrRef(ref).build();
		for (boolean validate : List.of(false, true)) {
			when(context.isValidateFuncParamsAtRuntime()).thenReturn(validate);
			assertEquals(expected ? MatchResult.MATCH : MatchResult.NOMATCH, match.match(context));
		}
	}

	@Test
	void aCustomInvocationWithAStandardIdStillDecidesTheMatch() {
		FunctionInvocation invocation = mock(FunctionInvocation.class);
		FunctionSpec spec = FunctionSpecBuilder.builder(PREFIX + "integer-equal")
				.param(XacmlTypes.INTEGER).param(XacmlTypes.INTEGER).build(XacmlTypes.BOOLEAN, invocation);
		EvaluationContext context = mock(EvaluationContext.class);
		AttributeDesignatorKey key = AttributeDesignatorKey.builder("test").category(CategoryId.RESOURCE)
				.dataType(XacmlTypes.INTEGER).build();
		BagOfValues bag = XacmlTypes.INTEGER.bagBuilder().value(2L).build();
		when(context.resolve(key)).thenReturn(Optional.of(bag));
		when(invocation.invoke(any(), any(), any(List.class))).thenReturn(BooleanVal.of(true));
		Match match = Match.builder().predicate(spec).attribute(XacmlTypes.INTEGER.ofAny(1L))
				.attrRef(AttributeDesignator.builder().key(key).mustBePresent(false).build()).build();
		assertEquals(MatchResult.MATCH, match.match(context));
	}
}
