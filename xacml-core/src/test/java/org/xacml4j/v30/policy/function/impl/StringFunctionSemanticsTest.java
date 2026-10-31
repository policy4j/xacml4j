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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.Expression;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.policy.Apply;
import org.xacml4j.v30.policy.Condition;
import org.xacml4j.v30.policy.ConditionResult;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.ModelFixtures;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.IntegerVal;
import org.xacml4j.v30.types.StringVal;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * Truth tables derived from XACML 3.0 A.3.9, not the implementation.
 * https://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html
 * The Jul/Juliet and URI controls follow the OASIS IIC300/IIC302 examples.
 */
public class StringFunctionSemanticsTest
{
	private static final String PREFIX = "urn:oasis:names:tc:xacml:3.0:function:";
	private static final FunctionProvider FUNCTIONS =
			FunctionProviderBuilder.builder().withDefaultFunctions().build();
	private static final String[] SEARCH = {"starts-with", "ends-with", "contains"};

	private record SearchCase(String needle, String text, boolean starts, boolean ends, boolean contains) {}
	private record Slice(String text, long start, long end, String expected, boolean uri) {}

	static Stream<Arguments> searches()
	{
		List<SearchCase> table = List.of(
				new SearchCase("", "", true, true, true),
				new SearchCase("", "Juliet", true, true, true),
				new SearchCase("Jul", "", false, false, false),
				new SearchCase("Jul", "Juliet", true, false, true),
				new SearchCase("iet", "Juliet", false, true, true),
				new SearchCase("lie", "Juliet", false, false, true),
				new SearchCase("Juliet", "Jul", false, false, false),
				new SearchCase("Juliet", "Juliet", true, true, true),
				new SearchCase("jul", "Juliet", false, false, false),
				new SearchCase("Jul", "juliet", false, false, false),
				new SearchCase("IET", "Juliet", false, false, false),
				new SearchCase("x", "Juliet", false, false, false),
				new SearchCase("abc", "abcabc", true, true, true),
				new SearchCase("bc", "abcabc", false, true, true),
				new SearchCase("^.*", "abc", false, false, false),
				new SearchCase(" ", "a b", false, false, true),
				new SearchCase("http://medico.com/", "http://medico.com/record", true, false, true),
				new SearchCase("/record", "http://medico.com/record", false, true, true),
				new SearchCase(".com/", "http://medico.com/record", false, false, true),
				new SearchCase("HTTP", "http://medico.com/record", false, false, false),
				new SearchCase("%20", "foo%20bar", false, false, true),
				new SearchCase(" ", "foo%20bar", false, false, false));
		List<Arguments> rows = new ArrayList<>();
		for (SearchCase row : table) {
			boolean[] expected = {row.starts(), row.ends(), row.contains()};
			for (int operation = 0; operation < SEARCH.length; operation++) {
				for (boolean validate : new boolean[]{false, true}) {
					rows.add(Arguments.of("string-" + SEARCH[operation], row.needle(), row.text(), expected[operation], validate));
					// A URI literal cannot contain an unescaped space; the string-only row remains tested.
					if (!row.text().contains(" ")) {
						rows.add(Arguments.of("anyURI-" + SEARCH[operation], row.needle(), row.text(), expected[operation], validate));
						rows.add(Arguments.of("uri-" + SEARCH[operation], row.needle(), row.text(), expected[operation], validate));
					}
				}
			}
		}
		return rows.stream();
	}

	@ParameterizedTest(name = "{0}({1}, {2}), runtimeValidation={4}")
	@MethodSource("searches")
	public void searchedValueIsMatchedAgainstCandidate(String function, String needle, String text,
			boolean expected, boolean validate)
	{
		Expression[] arguments = function.startsWith("string-")
				? new Expression[]{StringVal.of(needle), StringVal.of(text)}
				: function.startsWith("anyURI-")
						? new Expression[]{StringVal.of(needle), uri(text)}
						// Nonstandard uri-* compatibility entry points retain their (anyURI, string) signature.
						: new Expression[]{uri(text), StringVal.of(needle)};
		Apply apply = call(function, arguments);
		assertEquals(XacmlTypes.BOOLEAN, apply.getEvaluatesTo());
		assertEquals(BooleanVal.of(expected), apply.evaluate(ModelFixtures.context(validate)));
	}

	static Stream<Arguments> slices()
	{
		List<Slice> table = List.of(
				new Slice("abcdef", 0, 0, "", true),
				new Slice("abcdef", 0, -1, "abcdef", true),
				new Slice("abcdef", 1, 4, "bcd", true),
				new Slice("abcdef", 2, -1, "cdef", true),
				new Slice("abcdef", 6, -1, "", true),
				new Slice("abcdef", 6, 6, "", true),
				new Slice("abcdef", 0, 6, "abcdef", true),
				new Slice("abcdef", 3, 3, "", true),
				new Slice("", 0, -1, "", true),
				new Slice("", 0, 0, "", true),
				new Slice("A\uD83D\uDE00B", 1, 2, "\uD83D\uDE00", true),
				new Slice("A\uD83D\uDE00B", 2, -1, "B", true),
				// OASIS IIC330: zero-based start and exclusive end.
				new Slice("This is the initial test string.", 8, 15, "the ini", false),
				new Slice("This is the initial test string.", 8, -1, "the initial test string.", false),
				// OASIS IIC333; position 14 is '/' and position 24 is the exclusive end.
				new Slice("http://this/is/the/initial/uri", 14, 24, "/the/initi", true),
				new Slice("http://example.test/path", 7, -1, "example.test/path", true));
		List<Arguments> rows = new ArrayList<>();
		for (Slice row : table) {
			for (boolean validate : new boolean[]{false, true}) {
				rows.add(Arguments.of("string-substring", row.text(), row.start(), row.end(), row.expected(), validate));
				if (row.uri()) {
					rows.add(Arguments.of("anyURI-substring", row.text(), row.start(), row.end(), row.expected(), validate));
				}
			}
		}
		return rows.stream();
	}

	@ParameterizedTest(name = "{0}({1}, {2}, {3}), runtimeValidation={5}")
	@MethodSource("slices")
	public void substringUsesCharacterIndexesAndExclusiveEnd(String function, String text, long start,
			long end, String expected, boolean validate)
	{
		Apply apply = slice(function, text, start, end);
		assertEquals(XacmlTypes.STRING, apply.getEvaluatesTo());
		assertEquals(StringVal.of(expected), apply.evaluate(ModelFixtures.context(validate)));
	}

	static Stream<Arguments> invalidSlices()
	{
		long[][] table = {{-1, 2}, {0, -2}, {7, -1}, {0, 7}, {4, 3},
				{Long.MAX_VALUE, -1}, {0, Long.MAX_VALUE}, {4294967297L, -1},
				{0, 4294967297L}, {Long.MIN_VALUE, -1}};
		List<Arguments> rows = new ArrayList<>();
		for (String function : List.of("string-substring", "anyURI-substring")) {
			for (long[] row : table) {
				for (boolean validate : new boolean[]{false, true}) {
					rows.add(Arguments.of(function, "abcdef", row[0], row[1], validate));
				}
			}
			for (boolean validate : new boolean[]{false, true}) {
				rows.add(Arguments.of(function, "", 1L, -1L, validate));
			}
		}
		return rows.stream();
	}

	@ParameterizedTest(name = "{0} invalid [{2}, {3}), runtimeValidation={4}")
	@MethodSource("invalidSlices")
	public void outOfRangeSubstringIsIndeterminateWithProcessingError(String function, String text,
			long start, long end, boolean validate)
	{
		assertProcessingError(slice(function, text, start, end), validate);
	}

	static Stream<Arguments> invalidUriSlices()
	{
		return Stream.of(
				Arguments.of("http://host/path", 0L, 5L, "http:", false),
				Arguments.of("http://host/path", 0L, 5L, "http:", true),
				Arguments.of("http://[::1]/path", 7L, 8L, "[", false),
				Arguments.of("http://[::1]/path", 7L, 8L, "[", true),
				Arguments.of("http://a/%20", 9L, 10L, "%", false),
				Arguments.of("http://a/%20", 9L, 10L, "%", true));
	}

	@ParameterizedTest(name = "URI slice {3} is invalid, runtimeValidation={4}")
	@MethodSource("invalidUriSlices")
	public void uriSubstringAlsoRejectsInvalidUriSyntax(String text, long start, long end,
			String expectedString, boolean validate)
	{
		assertEquals(StringVal.of(expectedString), slice("string-substring", text, start, end)
				.evaluate(ModelFixtures.context(validate)));
		assertProcessingError(slice("anyURI-substring", text, start, end), validate);
	}

	@ParameterizedTest
	@ValueSource(strings = {"string-starts-with", "string-ends-with", "string-contains",
			"anyURI-starts-with", "anyURI-ends-with", "anyURI-contains",
			"uri-starts-with", "uri-ends-with", "uri-contains", "string-substring", "anyURI-substring"})
	public void signaturesRejectWrongArityAndTypes(String function)
	{
		FunctionSpec spec = spec(function);
		Expression[] valid = function.endsWith("substring")
				? new Expression[]{function.startsWith("anyURI-") ? uri("abc") : StringVal.of("abc"),
						IntegerVal.of(0), IntegerVal.of(-1)}
				: function.startsWith("uri-")
						? new Expression[]{uri("abc"), StringVal.of("a")}
						: new Expression[]{StringVal.of("a"), function.startsWith("anyURI-") ? uri("abc") : StringVal.of("abc")};
		spec.validateParametersAndThrow(Arrays.asList(valid));
		assertRejected(spec, Arrays.copyOf(valid, valid.length - 1));
		Expression[] extra = Arrays.copyOf(valid, valid.length + 1);
		extra[valid.length] = StringVal.of("extra");
		assertRejected(spec, extra);
		Expression[] wrong = valid.clone();
		wrong[0] = IntegerVal.of(1);
		assertRejected(spec, wrong);
		if (function.startsWith("anyURI-") && !function.endsWith("substring")) {
			assertRejected(spec, new Expression[]{uri("abc"), StringVal.of("a")});
		}
	}

	private static void assertRejected(FunctionSpec spec, Expression[] arguments)
	{
		assertFalse(spec.validateParameters(Arrays.asList(arguments)));
		SyntaxException rejected = assertThrows(SyntaxException.class,
				() -> spec.validateParametersAndThrow(Arrays.asList(arguments)));
		assertEquals(StatusCodeId.SYNTAX_ERROR, rejected.getStatus().getStatusCode().getValue());
	}

	private static void assertProcessingError(Apply apply, boolean validate)
	{
		EvaluationException failure = assertThrows(EvaluationException.class,
				() -> apply.evaluate(ModelFixtures.context(validate)));
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, failure.getStatus().getStatusCode().getValue());
		EvaluationContext context = ModelFixtures.context(validate);
		Apply predicate = Apply.builder(FUNCTIONS.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-equal").orElseThrow())
				.param(apply, StringVal.of("")).build();
		assertEquals(ConditionResult.INDETERMINATE, new Condition(predicate).evaluate(context));
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR,
				context.getEvaluationStatus().orElseThrow().getStatusCode().getValue());
	}

	private static Apply slice(String function, String text, long start, long end)
	{
		return call(function, function.startsWith("anyURI-") ? uri(text) : StringVal.of(text),
				IntegerVal.of(start), IntegerVal.of(end));
	}

	private static Expression uri(String text)
	{
		// URI input retains its lexical spelling, including an empty relative URI.
		return XacmlTypes.ANYURI.ofAny(URI.create(text));
	}

	private static Apply call(String function, Expression... arguments)
	{
		FunctionSpec spec = spec(function);
		spec.validateParametersAndThrow(Arrays.asList(arguments));
		return Apply.builder(spec).param(arguments).build();
	}

	private static FunctionSpec spec(String function)
	{
		return FUNCTIONS.getFunction(PREFIX + function).orElseThrow();
	}
}
