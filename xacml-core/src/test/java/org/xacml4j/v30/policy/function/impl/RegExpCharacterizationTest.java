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
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.ValueExp;
import org.xacml4j.v30.policy.FunctionSpec;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.StringVal;

/**
 * The record of what {@code string-regexp-match} gave before the regular-expression engine SPI
 * (change add-regex-engine-spi, task 2.1): the result, or the status code and message, for the
 * conformance patterns, the rows of the translation table of research RX1, the drift cases D1
 * to D8, the limit cases of {@link RegExpLimitsTest}, and an {@code Error} of the engine. Every
 * row passed on the code before the SPI (run of 2026-10-28, 116 rows). Design D5 changes two
 * kinds of row on purpose, a malformed pattern and an engine {@code Error}: such a row keeps its
 * recorded outcome and carries the approved one, which is asserted. Every other row keeps
 * today's outcome with the {@code jdk} engine and the
 * {@code legacy} profile.
 */
public class RegExpCharacterizationTest
{
	private static final String STRING_REGEXP_MATCH = "urn:oasis:names:tc:xacml:1.0:function:string-regexp-match";
	private static final String LIMIT = "Regular expression evaluation limit exceeded";
	private static final String INVALID = "Regular expression pattern is invalid";

	private static final FunctionSpec FUNCTION = FunctionProviderBuilder.builder().withDefaultFunctions().build()
			.getFunction(STRING_REGEXP_MATCH).orElseThrow();

	@ParameterizedTest(name = "{0}")
	@MethodSource("corpus")
	public void todaysOutcome(String name, Case c)
	{
		assertEquals(c.approved().orElse(c.today()), evaluate(c.pattern(), c.input()), name);
	}

	static Stream<Arguments> corpus()
	{
		return cases().stream().map(c -> Arguments.of(c.name(), c));
	}

	static List<Case> cases()
	{
		String spaces = "   This  is IT!  ";
		return List.of(
				// The conformance patterns (research RX3): ASCII literals, ".", "*" and "|"
				match("IIB008 read|write on write", "read|write", "write"),
				noMatch("IIB008 read|write on delete", "read|write", "delete"),
				match("IIC056 J.* Hibbert on Julius Hibbert", "J.* Hibbert", "Julius Hibbert"),
				noMatch("IIC056 J.* Hibbert on Bart Simpson", "J.* Hibbert", "Bart Simpson"),
				match("IIC056 B.* Simpson on Bart Simpson", "B.* Simpson", "Bart Simpson"),
				noMatch("IIC057 J.* K.* Hibbert on Julius Hibbert", "J.* K.* Hibbert", "Julius Hibbert"),
				match("IIC057 J.* K.* Hibbert on Julius K. Hibbert", "J.* K.* Hibbert", "Julius K. Hibbert"),
				match("IIC165 leading star", " *This  is.* IT!  ", spaces),
				match("IIC166 not", " *This .*is not IT! *", "   This  is not IT!  "),
				noMatch("IIC166 five spaces", "     This .*is IT!  ", spaces),
				match("IIC167 optional not on IT", "   This  is n*o*t* *IT!  ", spaces),
				match("IIC167 optional not on not IT", "   This  is n*o*t* *IT!  ", "   This  is not IT!  "),
				match("IIC167 literal spaces", "   This  is IT!  ", spaces),
				noMatch("IIC168 no match", "   This is not a match to IT!  ", spaces),
				match("IIC169 dot star", "   This  is .*IT!  ", spaces),
				// RX1 rows 1 to 3: substring search, ^ and $ anchor the whole string
				match("row 1 substring", "b", "abc"),
				match("row 1 empty pattern", "", "x"),
				match("row 1 empty pattern on empty input", "", ""),
				match("row 1 empty pattern on a newline", "", "\n"),
				match("row 2 ^ at the start", "^foo", "foo\n"),
				noMatch("row 2 ^ not after a newline", "^foo", "\nfoo"),
				noMatch("row 3 $ not before a final newline", "^foo$", "foo\n"),
				match("row 3 $ at the end", "foo$", "foo"),
				match("row 3 anchors bind one branch", "^foo|bar", "xbar"),
				// RX1 row 4: single-character escapes
				match("row 4 escaped dot", "a\\.b", "a.b"),
				noMatch("row 4 escaped dot is no wildcard", "a\\.b", "axb"),
				match("row 4 escaped dollar", "a\\$", "a$"),
				match("row 4 dollar in a class", "[$]", "$"),
				match("row 4 escaped braces", "\\{2\\}", "{2}"),
				match("row 4 escaped bar", "a\\|b", "a|b"),
				// RX1 row 5 and drift D4: "." excludes CR, U+0085, U+2028 and U+2029 in Java, LF only in XF
				match("row 5 dot on a letter", "a.c", "axc"),
				noMatch("row 5 dot on LF", "a.c", "a\nc"),
				noMatch("row 5 dot on CR", "a.c", "a\rc"),
				noMatch("row 5 dot on U+0085", "a.c", "a\u0085c"),
				noMatch("D4 dot on U+2028", "a.c", "a c"),
				noMatch("row 5 dot on U+2029", "a.c", "a c"),
				match("row 23 dot on a supplementary character", "^.$", "😀"),
				// RX1 rows 6 and 7: quantifiers, any count, reluctant
				match("row 6 exact count", "a{2}", "aa"),
				noMatch("row 6 at least", "a{2,}", "a"),
				match("row 6 count above 1000", "a{1001}", "a".repeat(1001)),
				malformed("row 6 inverted range is malformed", "a{3,2}", "aaa"),
				malformed("row 6 braces that are no quantifier are malformed", "a{x}", "a{x}"),
				match("row 7 reluctant", "a+?", "aaa"),
				malformed("row 7 doubled quantifier", "a**", "aa"),
				malformed("row 7 quantifier without an atom", "*a", "a"),
				// RX1 rows 8 and 9: groups and back-references
				match("row 8 non-capturing group", "(?:ab)+", "abab"),
				match("row 9 back-reference matches", "(a)\\1", "aa"),
				noMatch("row 9 back-reference does not match", "(a)\\1", "ab"),
				malformed("row 9 unmatched parenthesis is malformed", "(", "a"),
				// RX1 rows 10 to 13 and drifts D1 to D3: the multi-character escapes
				match("D3 \\s on a vertical tab", "\\s", "\u000B"),
				match("row 10 \\s on a space", "\\s", " "),
				noMatch("row 10 \\s on U+0085", "\\s", "\u0085"),
				noMatch("D3 \\d on an Arabic-Indic digit", "\\d", "٣"),
				match("row 11 \\d on a digit", "\\d", "7"),
				match("D3 \\w on an underscore", "\\w", "_"),
				noMatch("D3 \\w on a plus", "\\w", "+"),
				noMatch("row 12 \\w on a non-ASCII letter", "\\w", "é"),
				match("row 12 \\w on a letter", "\\w", "a"),
				noMatch("D1 \\c+ on a", "\\c+", "a"),
				match("D1 \\c+ is the control escape of k", "\\c+", "k"),
				malformed("D2 \\i is malformed", "\\i", "a"),
				malformed("row 13 \\I is malformed", "\\I", "a"),
				malformed("row 13 \\C is malformed", "\\C", "a"),
				// RX1 rows 14 to 16 and drift D7: categories and blocks
				match("row 14 \\p{Lu}", "\\p{Lu}", "A"),
				match("row 14 \\p{Cn} on an unassigned code point", "\\p{Cn}", "͸"),
				match("row 14 \\p{C} holds Cn", "\\p{C}", "͸"),
				match("row 14 \\P{L}", "\\P{L}", "1"),
				match("row 14 \\p{Lo} on a code point assigned after Unicode 6.0", "\\p{Lo}", "ࡠ"),
				match("row 15 \\pL without braces", "\\pL", "a"),
				malformed("row 15 a script name is malformed", "\\p{Greek}", "α"),
				match("row 16 \\p{IsBasicLatin}", "\\p{IsBasicLatin}", "a"),
				match("row 16 \\p{IsGreek}", "\\p{IsGreek}", "α"),
				match("row 16 \\p{IsCombiningMarksforSymbols}", "\\p{IsCombiningMarksforSymbols}", "⃐"),
				malformed("D7 \\p{IsPrivateUse} is malformed", "\\p{IsPrivateUse}", ""),
				malformed("row 16 an unknown block is malformed", "\\p{IsNoSuchBlock}", "a"),
				// RX1 rows 17 to 19 and drifts D5, D6: classes, subtraction, &&
				match("row 17 nested class is a union", "[a[b]]", "b"),
				match("row 17 ] first in a class is a literal", "[]a]", "]"),
				match("row 17 range from a hyphen", "[--z]", "a"),
				malformed("row 17 [^] is malformed", "[^]", "a"),
				match("row 17 hyphen last", "[a-]", "-"),
				malformed("row 17 unclosed class is malformed", "[a", "a"),
				match("row 18 subtraction keeps a consonant", "[a-z-[aeiou]]", "b"),
				noMatch("row 18 subtraction drops a vowel", "[a-z-[aeiou]]", "a"),
				match("D5 negated subtraction on a vowel", "[^a-z-[aeiou]]", "a"),
				noMatch("D5 negated subtraction on a consonant", "[^a-z-[aeiou]]", "b"),
				noMatch("D6 && is an intersection, not an ampersand", "[a&&b]", "&"),
				noMatch("D6 empty intersection", "[a&&b]", "a"),
				match("row 19 intersection", "[ab&&bc]", "b"),
				// RX1 rows 20 to 22 and drift D8: Java-only syntax
				match("D8 (?i) flag", "(?i)a", "A"),
				match("row 20 lookahead", "(?=a)a", "a"),
				match("row 20 named group", "(?<n>a)\\k<n>", "aa"),
				malformed("row 20 (? alone is malformed", "(?", "a"),
				match("row 21 \\A", "\\Aa", "a"),
				match("row 21 \\z", "a\\z", "a"),
				match("row 21 word boundary", "\\bfoo\\b", "a foo b"),
				match("row 21 quoting", "\\Qa.b\\E", "a.b"),
				match("row 21 hexadecimal escape", "\\x41", "A"),
				match("row 21 unicode escape", "\\u0041", "A"),
				// The limit cases of RegExpLimitsTest, with the default limits
				match("limit: pattern at the length limit", "a".repeat(10_000), "a".repeat(10_000)),
				limit("limit: pattern over the length limit", "a".repeat(10_001), "a"),
				match("limit: input at the length limit", "a$", "a".repeat(100_000)),
				limit("limit: input over the length limit", "a", "a".repeat(100_001)),
				match("limit: nested counts at the weight limit", "(?:(?:){1000}){1000}", ""),
				limit("limit: nested counts over the weight limit", "(?:(?:(?:){1000}){1000}){1000}", ""),
				match("limit: quantifiers next to each other do not multiply", "(?:){1000}{1001}", ""),
				limit("limit: largest counts", "(?:(?:){2147483647}){2147483647}", ""),
				limit("limit: counted anchors", "(?:\\A{100000}){100000}", ""),
				limit("limit: weight beyond a long", "(?:(?:(?:){2147483647}){2147483647}){3}", ""),
				limit("limit: repetition hidden after a control escape", "\\c[(?:(?:){2147483647}){2147483647}", "\u001B"),
				limit("limit: repetition hidden after an empty quote", "(?:(?:){1000})\\Q\\E{2147483647}", ""),
				limit("limit: repetition of an empty atom", "({2147483647}){2147483647}", ""),
				limit("limit: repetition hidden in a class with a literal ]", "(?:(?:){100000}[]a(]{0}){100000}", ""),
				match("limit: braces in a class are no repetition", "[]a{1000001}]", "a"),
				limit("limit: comments mode", "(?x)a", "a"),
				limit("limit: catastrophic backtracking stops at the deadline", "(.*a){20}$", "a".repeat(40) + "!"),
				// An Error of the engine: java.util.regex recurses once per repetition of a group
				// and overflows the stack on a long input (JDK-5050507)
				engineError("engine Error: stack overflow while matching", "(a|b)*", "ab".repeat(50_000)));
	}

	private static final Outcome MATCH = new Outcome(Optional.of(true), Optional.empty(), Optional.empty());
	private static final Outcome NO_MATCH = new Outcome(Optional.of(false), Optional.empty(), Optional.empty());
	/** Before the SPI, a malformed pattern and an Error of the engine: processing-error without a message */
	private static final Outcome PROCESSING_ERROR =
			new Outcome(Optional.empty(), Optional.of(StatusCodeId.STATUS_PROCESSING_ERROR), Optional.empty());
	/** A limit, before and after: processing-error with the fixed message */
	private static final Outcome LIMIT_EXCEEDED =
			new Outcome(Optional.empty(), Optional.of(StatusCodeId.STATUS_PROCESSING_ERROR), Optional.of(LIMIT));
	/** Design D5: a malformed pattern is syntax-error with the fixed message */
	private static final Outcome SYNTAX_ERROR =
			new Outcome(Optional.empty(), Optional.of(StatusCodeId.SYNTAX_ERROR), Optional.of(INVALID));

	private static Case match(String name, String pattern, String input)
	{
		return new Case(name, pattern, input, MATCH, Optional.empty());
	}

	private static Case noMatch(String name, String pattern, String input)
	{
		return new Case(name, pattern, input, NO_MATCH, Optional.empty());
	}

	private static Case limit(String name, String pattern, String input)
	{
		return new Case(name, pattern, input, LIMIT_EXCEEDED, Optional.empty());
	}

	/** A malformed pattern: processing-error without a message before the SPI, syntax-error with the fixed message by D5 */
	private static Case malformed(String name, String pattern, String input)
	{
		return new Case(name, pattern, input, PROCESSING_ERROR, Optional.of(SYNTAX_ERROR));
	}

	/** An Error of the engine: processing-error without a message before the SPI, the limit message by D5 */
	private static Case engineError(String name, String pattern, String input)
	{
		return new Case(name, pattern, input, PROCESSING_ERROR, Optional.of(LIMIT_EXCEEDED));
	}

	private static Outcome evaluate(String pattern, String input)
	{
		try{
			ValueExp result = FUNCTION.invoke(mock(EvaluationContext.class), StringVal.of(pattern), StringVal.of(input));
			return new Outcome(Optional.of(((BooleanVal)result).get()), Optional.empty(), Optional.empty());
		}catch(EvaluationException e){
			return new Outcome(Optional.empty(), Optional.of(e.getStatus().getStatusCode().getValue()),
					e.getStatus().getMessage());
		}
	}

	/** A result, or the status code and message of an Indeterminate */
	record Outcome(Optional<Boolean> result, Optional<StatusCodeId> statusCode, Optional<String> message)
	{
	}

	/** One row: the outcome before the SPI and, for a row design D5 changes, the approved outcome */
	record Case(String name, String pattern, String input, Outcome today, Optional<Outcome> approved)
	{
	}
}
