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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.policy.function.regex.CompiledRegex;
import org.xacml4j.v30.policy.function.regex.RegexBudget;
import org.xacml4j.v30.policy.function.regex.RegexCompileException;
import org.xacml4j.v30.policy.function.regex.RegexEngine;
import org.xacml4j.v30.policy.function.regex.RegexLimits;
import org.xacml4j.v30.policy.function.regex.RegexProfile;
import org.xacml4j.v30.policy.function.regex.jdk.JdkRegexEngine;
import org.xacml4j.v30.types.StringVal;

/**
 * The status matrix of design D5 as the maintainer approved it on 2026-10-28 (change
 * add-regex-engine-spi): one case per row.
 */
public class RegexStatusMatrixTest
{
	private static final RegexEngine JDK = new JdkRegexEngine();

	/** XF 1.0 7.6.1 and XSD Appendix F [20]: invalid references and range endpoints are syntax errors. */
	@ParameterizedTest
	@CsvSource(delimiter = ';', value = {
			"[\\1];true", "(a[\\1]);true", "(a)[a-\\1];true", "(a)[\\1];false"})
	public void classBackReferencesKeepTheirStatusInOrdinaryAndBoundCalls(String pattern, boolean malformed)
	{
		FunctionSpec function = FunctionProviderBuilder.builder()
				.withRegexEngine(JDK, RegexProfile.XF, RegexLimits.defaults()).withDefaultFunctions().build()
				.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-regexp-match").orElseThrow();
		EvaluationContext context = mock(EvaluationContext.class);
		StringVal literal = StringVal.of(pattern);
		EvaluationException ordinary = assertThrows(EvaluationException.class,
				() -> function.invoke(context, literal, StringVal.of("aa")));
		var bound = function.bind(literal).orElseThrow();
		EvaluationException retained = assertThrows(EvaluationException.class,
				() -> bound.invoke(context, StringVal.of("aa")));

		StatusCodeId code = malformed ? StatusCodeId.SYNTAX_ERROR : StatusCodeId.STATUS_PROCESSING_ERROR;
		String message = malformed ? RegExpFunctions.INVALID_PATTERN_MESSAGE
				: "Regular expression construct back-reference in a character class is not supported by engine xf parser";
		assertEquals(code, ordinary.getStatus().getStatusCode().getValue());
		assertEquals(Optional.of(message), ordinary.getStatus().getMessage());
		assertEquals(code, retained.getStatus().getStatusCode().getValue());
		assertEquals(Optional.of(message), retained.getStatus().getMessage());
	}

	/** XACML 3.0 7.19.2 and B.8: a pattern invalid in its profile is a syntax error, with a fixed message */
	@Test
	public void aMalformedPatternIsSyntaxErrorWithTheFixedMessage()
	{
		for(RegexProfile profile : RegexProfile.values()){
			EvaluationException e = assertThrows(EvaluationException.class,
					() -> new RegExpFunctions.Matching(JDK, profile, RegexLimits.defaults()).matches("(", "a"), profile.id());
			assertEquals(StatusCodeId.SYNTAX_ERROR, e.getStatus().getStatusCode().getValue(), profile.id());
			assertEquals(Optional.of(RegExpFunctions.INVALID_PATTERN_MESSAGE), e.getStatus().getMessage(), profile.id());
		}
		// Through the registered function too
		FunctionSpec function = FunctionProviderBuilder.builder().withDefaultFunctions().build()
				.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-regexp-match").orElseThrow();
		EvaluationException e = assertThrows(EvaluationException.class,
				() -> function.invoke(mock(EvaluationContext.class), StringVal.of("("), StringVal.of("a")));
		assertEquals(StatusCodeId.SYNTAX_ERROR, e.getStatus().getStatusCode().getValue());
		assertEquals(Optional.of(RegExpFunctions.INVALID_PATTERN_MESSAGE), e.getStatus().getMessage());
	}

	/** 7.19.1: a construct the engine cannot express is a processing error whose message names the construct and the engine */
	@Test
	public void anUnsupportedConstructIsProcessingErrorNamingTheConstructAndTheEngine()
	{
		RegexEngine rejecting = engine("rejecting", (pattern, profile, limits) -> {
			throw RegexCompileException.unsupported("back-reference", "rejecting");
		});

		EvaluationException e = assertThrows(EvaluationException.class,
				() -> new RegExpFunctions.Matching(rejecting, RegexProfile.XF, RegexLimits.defaults()).matches("(a)\\1", "aa"));

		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, e.getStatus().getStatusCode().getValue());
		assertEquals(Optional.of("Regular expression construct back-reference is not supported by engine rejecting"),
				e.getStatus().getMessage());
	}

	@Test
	public void aLimitIsProcessingErrorWithTheFixedMessage()
	{
		for(RegexProfile profile : RegexProfile.values()){
			EvaluationException e = assertThrows(EvaluationException.class,
					() -> new RegExpFunctions.Matching(JDK, profile, RegexLimits.defaults())
							.matches("a".repeat(RegexLimits.DEFAULT_MAX_PATTERN_LENGTH + 1), "a"), profile.id());
			assertLimit(e);
		}
		// The limits of the xf profile only: the repeat count and the program size
		RegExpFunctions.Matching xf = new RegExpFunctions.Matching(JDK, RegexProfile.XF, RegexLimits.defaults());
		assertLimit(assertThrows(EvaluationException.class, () -> xf.matches("a{1001}", "a")));
		assertLimit(assertThrows(EvaluationException.class, () -> xf.matches("(a{1000}){1000}", "a")));
		assertTrue(xf.matches("a{1000}", "a".repeat(1000)));
		assertTrue(new RegExpFunctions.Matching(JDK, RegexProfile.LEGACY, RegexLimits.defaults())
				.matches("a{1001}", "a".repeat(1001)));
	}

	@Test
	public void inputLengthTakesPrecedenceOverAInvalidPattern()
	{
		RegexLimits limits = RegexLimits.builder().maxInputLength(1).build();
		for(RegexProfile profile : RegexProfile.values()){
			RegExpFunctions.Matching matching = new RegExpFunctions.Matching(JDK, profile, limits);
			assertLimit(assertThrows(EvaluationException.class, () -> matching.matches("(", "xx")));
		}
	}

	/** An Error of an engine, such as the stack overflow of JDK-5050507, is a limit; the engine is injected, so the test does not depend on the stack size */
	@Test
	public void anErrorOfTheEngineIsProcessingErrorWithTheLimitMessage()
	{
		RegexEngine overflowing = engine("overflowing", (pattern, profile, limits) -> (input, budget) -> {
			throw new StackOverflowError();
		});

		EvaluationException e = assertThrows(EvaluationException.class,
				() -> new RegExpFunctions.Matching(overflowing, RegexProfile.XF, RegexLimits.defaults()).matches("(a|b)*", "ab"));

		assertLimit(e);
	}

	private static void assertLimit(EvaluationException e)
	{
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, e.getStatus().getStatusCode().getValue());
		assertEquals(Optional.of(RegExpFunctions.LIMIT_EXCEEDED_MESSAGE), e.getStatus().getMessage());
	}

	interface Compile
	{
		CompiledRegex compile(String pattern, RegexProfile profile, RegexLimits limits);
	}

	/** An engine of one profile with the given compilation */
	private static RegexEngine engine(String name, Compile compile)
	{
		return new RegexEngine(){
			@Override
			public String name(){
				return name;
			}

			@Override
			public Set<RegexProfile> profiles(){
				return Set.of(RegexProfile.XF);
			}

			@Override
			public CompiledRegex compile(String pattern, RegexProfile profile, RegexLimits limits){
				return compile.compile(pattern, profile, limits);
			}
		};
	}
}
