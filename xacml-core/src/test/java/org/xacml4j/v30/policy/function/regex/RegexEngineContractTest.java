package org.xacml4j.v30.policy.function.regex;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * The contract of the engines (change add-regex-engine-spi, design D2 and the test strategy): one
 * table of patterns and inputs, each with its outcome in the {@code legacy} profile and in the
 * {@code xf} profile, run against every engine on the class path that offers the profile. The
 * {@code jdk} engine offers both; {@code re2j} offers {@code xf} and must give the same outcomes
 * there.
 */
public class RegexEngineContractTest
{
	/** What a row gives: a result, or a failure of the engine by its kind */
	enum Expect { TRUE, FALSE, MALFORMED, UNSUPPORTED, LIMIT }

	record Row(String name, String pattern, String input, Expect legacy, Expect xf)
	{
	}

	static Stream<Row> rows()
	{
		return Stream.of(
				// Substring search, anchors on the whole string, the empty pattern
				new Row("substring", "read|write", "may write", Expect.TRUE, Expect.TRUE),
				new Row("full anchors", "^foo$", "foo", Expect.TRUE, Expect.TRUE),
				new Row("$ not before a final newline", "^foo$", "foo\n", Expect.FALSE, Expect.FALSE),
				new Row("^ not after a newline", "^foo", "\nfoo", Expect.FALSE, Expect.FALSE),
				new Row("anchor binds one branch", "foo|bar$", "foox", Expect.TRUE, Expect.TRUE),
				new Row("empty pattern", "", "x", Expect.TRUE, Expect.TRUE),
				new Row("empty pattern on the empty string", "", "", Expect.TRUE, Expect.TRUE),
				new Row("escaped dollar", "\\$", "$", Expect.TRUE, Expect.TRUE),
				new Row("escaped dot", "a\\.b", "a.b", Expect.TRUE, Expect.TRUE),
				new Row("one supplementary character", "^.$", "😀", Expect.TRUE, Expect.TRUE),
				// The dot: XF excludes the line feed only
				new Row("dot on LF", "a.c", "a\nc", Expect.FALSE, Expect.FALSE),
				new Row("dot on CR", "a.c", "a\rc", Expect.FALSE, Expect.TRUE),
				new Row("dot on U+0085", "a.c", "a\u0085c", Expect.FALSE, Expect.TRUE),
				new Row("dot on U+2028", "a.c", "a c", Expect.FALSE, Expect.TRUE),
				// Multi-character escapes
				new Row("\\d on a digit", "\\d", "7", Expect.TRUE, Expect.TRUE),
				new Row("\\d on U+0663", "\\d", "٣", Expect.FALSE, Expect.TRUE),
				new Row("\\w on _", "\\w", "_", Expect.TRUE, Expect.FALSE),
				new Row("\\w on +", "\\w", "+", Expect.FALSE, Expect.TRUE),
				new Row("\\w on a non-ASCII letter", "\\w", "é", Expect.FALSE, Expect.TRUE),
				new Row("\\s on a vertical tab", "\\s", "\u000B", Expect.TRUE, Expect.FALSE),
				new Row("\\c+ on a", "\\c+", "a", Expect.FALSE, Expect.TRUE),
				new Row("\\i on a", "\\i", "a", Expect.MALFORMED, Expect.TRUE),
				new Row("\\i on a digit", "\\i", "1", Expect.MALFORMED, Expect.FALSE),
				// Classes: subtraction, &&, the class edge cases of the design
				new Row("subtraction keeps a consonant", "[a-z-[aeiou]]", "b", Expect.TRUE, Expect.TRUE),
				new Row("subtraction drops a vowel", "[a-z-[aeiou]]", "a", Expect.FALSE, Expect.FALSE),
				new Row("negated subtraction on a vowel", "[^a-z-[aeiou]]", "a", Expect.TRUE, Expect.FALSE),
				new Row("negated subtraction on a digit", "[^a-z-[aeiou]]", "1", Expect.TRUE, Expect.TRUE),
				new Row("negated subtraction on a consonant", "[^a-z-[aeiou]]", "b", Expect.FALSE, Expect.FALSE),
				new Row("&& on an ampersand", "[a&&b]", "&", Expect.FALSE, Expect.TRUE),
				new Row("[$]", "[$]", "$", Expect.TRUE, Expect.TRUE),
				new Row("[a^] on ^", "[a^]", "^", Expect.TRUE, Expect.TRUE),
				new Row("[a^] on a", "[a^]", "a", Expect.TRUE, Expect.TRUE),
				new Row("[\\d_] on _", "[\\d_]", "_", Expect.TRUE, Expect.TRUE),
				new Row("[\\d_] on U+0663", "[\\d_]", "٣", Expect.FALSE, Expect.TRUE),
				new Row("[\\i-[:]] on a", "[\\i-[:]]", "a", Expect.MALFORMED, Expect.TRUE),
				new Row("[\\i-[:]] on a colon", "[\\i-[:]]", ":", Expect.MALFORMED, Expect.FALSE),
				new Row("] first in a class", "[]a]", "]", Expect.TRUE, Expect.MALFORMED),
				new Row("range from a hyphen", "[--z]", "a", Expect.TRUE, Expect.MALFORMED),
				// Groups, back-references, quantifiers
				new Row("capturing group", "(ab)+", "abab", Expect.TRUE, Expect.TRUE),
				new Row("back-reference matches", "(a)\\1", "aa", Expect.TRUE, Expect.TRUE),
				new Row("back-reference does not match", "(a)\\1", "ab", Expect.FALSE, Expect.FALSE),
				new Row("repeated mandatory capture", "^(a)+\\1$", "aaa", Expect.TRUE, Expect.TRUE),
				new Row("optional capture on empty", "^(a)?\\1$", "", Expect.FALSE, Expect.UNSUPPORTED),
				new Row("optional capture on one a", "^(a)?\\1$", "a", Expect.FALSE, Expect.UNSUPPORTED),
				new Row("optional capture on two a", "^(a)?\\1$", "aa", Expect.TRUE, Expect.UNSUPPORTED),
				new Row("reluctant", "a+?", "aaa", Expect.TRUE, Expect.TRUE),
				new Row("reluctant count", "a{2}?", "aa", Expect.TRUE, Expect.TRUE),
				new Row("at least", "a{2,}", "a", Expect.FALSE, Expect.FALSE),
				new Row("count at the xf bound", "a{1000}", "a".repeat(1000), Expect.TRUE, Expect.TRUE),
				new Row("count above the xf bound", "a{1001}", "a".repeat(1001), Expect.TRUE, Expect.LIMIT),
				new Row("nested counts above the xf program size", "(a{1000}){1000}", "", Expect.FALSE, Expect.LIMIT),
				// Java-only syntax: accepted by legacy, outside the xf grammar
				new Row("(?i) flag", "(?i)a", "A", Expect.TRUE, Expect.MALFORMED),
				new Row("non-capturing group", "(?:a)", "a", Expect.TRUE, Expect.MALFORMED),
				new Row("adjacent counts", "(?:){1000}{1001}", "", Expect.TRUE, Expect.MALFORMED),
				new Row("lookahead", "(?=a)a", "a", Expect.TRUE, Expect.MALFORMED),
				new Row("quoting", "\\Qa.b\\E", "a.b", Expect.TRUE, Expect.MALFORMED),
				new Row("hexadecimal escape", "\\x41", "A", Expect.TRUE, Expect.MALFORMED),
				new Row("word boundary", "\\bfoo\\b", "a foo b", Expect.TRUE, Expect.MALFORMED),
				new Row("category without braces", "\\pL", "a", Expect.TRUE, Expect.MALFORMED),
				// Categories and blocks of the JDK's Unicode data
				new Row("\\p{Lu}", "\\p{Lu}", "A", Expect.TRUE, Expect.TRUE),
				new Row("\\p{Lo} after Unicode 6.0", "\\p{Lo}", "ࡠ", Expect.TRUE, Expect.TRUE),
				new Row("\\p{C} on an unassigned code point", "\\p{C}", "͸", Expect.TRUE, Expect.TRUE),
				new Row("\\P{L}", "\\P{L}", "1", Expect.TRUE, Expect.TRUE),
				new Row("\\p{IsBasicLatin}", "\\p{IsBasicLatin}", "a", Expect.TRUE, Expect.TRUE),
				new Row("XML name start excludes U+200C", "^\\i$", "‌", Expect.MALFORMED, Expect.FALSE),
				new Row("XML name character excludes U+200C", "^\\c$", "‌", Expect.FALSE, Expect.FALSE),
				new Row("block names are case sensitive", "\\p{Isbasiclatin}", "a", Expect.TRUE, Expect.MALFORMED),
				new Row("\\p{IsGreek}", "\\p{IsGreek}", "α", Expect.TRUE, Expect.TRUE),
				new Row("\\p{IsPrivateUse}", "\\p{IsPrivateUse}", "", Expect.MALFORMED, Expect.TRUE),
				new Row("script name", "\\p{Greek}", "α", Expect.MALFORMED, Expect.MALFORMED),
				// Malformed in both profiles
				new Row("unmatched parenthesis", "(", "a", Expect.MALFORMED, Expect.MALFORMED),
				new Row("inverted count range", "a{3,2}", "aaa", Expect.MALFORMED, Expect.MALFORMED),
				new Row("quantifier without an atom", "*a", "a", Expect.MALFORMED, Expect.MALFORMED),
				new Row("doubled quantifier", "a**", "aa", Expect.MALFORMED, Expect.MALFORMED),
				new Row("empty negated class", "[^]", "a", Expect.MALFORMED, Expect.MALFORMED));
	}

	static Stream<Arguments> cases()
	{
		List<Arguments> cases = new ArrayList<>();
		for(String name : RegexEngines.names()){
			RegexEngine engine = RegexEngines.engine(name);
			for(RegexProfile profile : engine.profiles()){
				rows().forEach(row -> cases.add(Arguments.of(name + "/" + profile + ": " + row.name(), engine, profile, row)));
			}
		}
		return cases.stream();
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("cases")
	public void everyEngineGivesTheOutcomeOfTheProfile(String name, RegexEngine engine, RegexProfile profile, Row row)
	{
		Expect expected = profile == RegexProfile.LEGACY ? row.legacy() : row.xf();
		assertEquals(expected, outcome(engine, profile, row.pattern(), row.input()), name);
	}

	@Test
	public void theJdkEngineIsOnTheClassPathAndOffersBothProfiles()
	{
		assertTrue(RegexEngines.names().contains("jdk"));
		RegexEngine jdk = RegexEngines.engine("jdk");
		assertEquals("jdk", jdk.name());
		assertTrue(jdk.offers(RegexProfile.LEGACY));
		assertTrue(jdk.offers(RegexProfile.XF));
	}

	/** A compiled pattern is reused: two searches on one compiled pattern, each from the start of its input */
	@Test
	public void aCompiledPatternAnswersEverySearchFromTheStart()
	{
		RegexEngine jdk = RegexEngines.engine("jdk");
		CompiledRegex compiled = jdk.compile("^a+$", RegexProfile.XF, RegexLimits.defaults());
		assertTrue(compiled.find("aaa", RegexBudget.start(RegexLimits.defaults())));
		assertEquals(false, compiled.find("aab", RegexBudget.start(RegexLimits.defaults())));
		assertTrue(compiled.find("a", RegexBudget.start(RegexLimits.defaults())));
	}

	static Expect outcome(RegexEngine engine, RegexProfile profile, String pattern, String input)
	{
		try{
			CompiledRegex compiled = engine.compile(pattern, profile, RegexLimits.defaults());
			return compiled.find(input, RegexBudget.start(RegexLimits.defaults())) ? Expect.TRUE : Expect.FALSE;
		}catch(RegexCompileException e){
			return switch(e.kind()){
				case MALFORMED -> Expect.MALFORMED;
				case UNSUPPORTED -> Expect.UNSUPPORTED;
				case LIMIT -> Expect.LIMIT;
			};
		}catch(RegexLimitException e){
			return Expect.LIMIT;
		}
	}
}
