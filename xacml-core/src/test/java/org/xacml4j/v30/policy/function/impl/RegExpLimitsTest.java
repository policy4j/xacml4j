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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.function.LongSupplier;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * Maintainer ruling 2026-10-07 (run 061 D17, bead policy4j-the.4.51): a
 * regular-expression match has a limit on the pattern length, on the input
 * length, and on the time of one match, and the optional pattern cache is
 * bounded. Beyond a limit the function is Indeterminate with status
 * processing-error.
 */
public class RegExpLimitsTest
{
	/**
	 * Backtracks for more than three seconds on JDK 21.0.6 for this input
	 * (measured; the classic {@code (a+)+$} returns at once on that JDK).
	 */
	private static final String CATASTROPHIC_PATTERN = "(.*a){20}$";
	private static final String CATASTROPHIC_INPUT = "a".repeat(40) + "!";

	@Test
	public void defaultLimits()
	{
		assertEquals(10_000, RegExpFunctions.DEFAULT_MAX_PATTERN_LENGTH);
		assertEquals(100_000, RegExpFunctions.DEFAULT_MAX_INPUT_LENGTH);
		assertEquals(1_000, RegExpFunctions.DEFAULT_MAX_MATCH_MILLIS);
		assertEquals(1_000_000, RegExpFunctions.DEFAULT_MAX_REPETITION);
		assertEquals(256, RegExpFunctions.DEFAULT_CACHE_SIZE);
	}

	@Test
	public void matchWithinTheLimitsIsUnchanged()
	{
		RegExpFunctions.Limits limits = new RegExpFunctions.Limits(10, 10, 1_000);

		assertTrue(RegExpFunctions.matches("a+b", "xaab", limits));
		assertFalse(RegExpFunctions.matches("a+b$", "xaabc", limits));
		// Exactly at both length limits.
		assertTrue(RegExpFunctions.matches("0123456789", "0123456789", limits));
	}

	@Test
	public void catastrophicBacktrackingIsStoppedByTheTimeLimit()
	{
		RegExpFunctions.Limits limits = new RegExpFunctions.Limits(100, 100, 50);

		long start = System.nanoTime();
		EvaluationException e = assertTimeoutPreemptively(Duration.ofSeconds(20),
				() -> assertThrows(EvaluationException.class,
						() -> RegExpFunctions.matches(CATASTROPHIC_PATTERN, CATASTROPHIC_INPUT, limits)));
		long elapsedMillis = Duration.ofNanos(System.nanoTime() - start).toMillis();

		assertLimitExceeded(e);
		// The limit is 50 ms. Two seconds allow for a loaded build machine; without
		// the limit this match does not return within three seconds (regex-probe.txt).
		assertTrue(elapsedMillis < 2_000, () -> "elapsed " + elapsedMillis + " ms");
	}

	/**
	 * rv087 round 1: a group that matches the empty string is repeated its
	 * minimum count without a read of the input, so the time limit does not
	 * see it. The nested counted repetitions of a pattern are bounded
	 * instead, before the pattern is compiled.
	 */
	@Test
	public void nestedCountedRepetitionsAreBoundedBeforeTheMatch()
	{
		RegExpFunctions.Limits limits = new RegExpFunctions.Limits(100, 100, 60_000);

		long start = System.nanoTime();
		// Without the bound these matches do not return; the timeout turns that into a failure.
		assertTimeoutPreemptively(Duration.ofSeconds(20), () -> {
			assertLimitExceeded(assertThrows(EvaluationException.class,
					() -> RegExpFunctions.matches("(?:(?:){2147483647}){2147483647}", "", limits)));
			assertLimitExceeded(assertThrows(EvaluationException.class,
					() -> RegExpFunctions.matches("(?:(?:(?:){1000}){1000}){1000}", "", limits)));
			assertLimitExceeded(assertThrows(EvaluationException.class,
					() -> RegExpFunctions.matches("(?:\\A{100000}){100000}", "", limits)));
		});
		assertTrue(Duration.ofNanos(System.nanoTime() - start).toMillis() < 2_000);
		// At the limit of 1,000,000 the pattern is matched.
		assertTrue(RegExpFunctions.matches("(?:(?:){1000}){1000}", "", limits));
	}

	/**
	 * rv087 round 2: constructs that hid a repetition from the first
	 * scanner. Each pattern compiles on JDK 21 and repeats an empty atom
	 * (regex-probe.txt, probes 4 and 5).
	 */
	@Test
	public void repetitionsCanNotBeHiddenFromTheBound()
	{
		RegExpFunctions.Limits limits = new RegExpFunctions.Limits(100, 100, 60_000);
		String[][] hidden = {
				// "\c[" is one control-character escape, not the start of a class.
				{"\\c[(?:(?:){2147483647}){2147483647}", "\u001B"},
				// An empty quote disappears; the quantifier belongs to the group.
				{"(?:(?:){1000})\\Q\\E{2147483647}", ""},
				// A quantifier directly after "(" repeats an empty atom.
				{"({2147483647}){2147483647}", ""},
				// "]" directly after "[" is a literal, so the "(" is in the class.
				{"(?:(?:){100000}[]a(]{0}){100000}", ""},
				{"(?:(?:){100000}[^]a(]{0}){100000}", ""},
		};

		long start = System.nanoTime();
		// Without the bound these matches do not return; the timeout turns that into a failure.
		assertTimeoutPreemptively(Duration.ofSeconds(20), () -> {
			for(String[] h : hidden){
				assertLimitExceeded(assertThrows(EvaluationException.class,
						() -> RegExpFunctions.matches(h[0], h[1], limits), h[0]));
			}
		});
		assertTrue(Duration.ofNanos(System.nanoTime() - start).toMillis() < 2_000);

		assertEquals(1_001_000, RegExpFunctions.repetitionWeight("\\c[(?:(?:){1000}){1001}"));
		assertEquals(1_001_000, RegExpFunctions.repetitionWeight("(?:(?:){1000})\\Q\\E{1001}"));
		assertEquals(1_001_000, RegExpFunctions.repetitionWeight("({1000}){1001}"));
		assertEquals(1_001_000, RegExpFunctions.repetitionWeight("(?:a|{1000}){1001}"));
		assertEquals(1_001_000, RegExpFunctions.repetitionWeight("(?:(?:){1000}[]a(]{0}){1001}"));
		assertEquals(1_001_000, RegExpFunctions.repetitionWeight("(?:(?:){1000}[^]a(]{0}){1001}"));
		assertEquals(1_001_000, RegExpFunctions.repetitionWeight("(?:(?:){1000}[a&&[]b(]]{0}){1001}"));
	}

	/** rv087 round 2: a "]" directly after "[" is a literal, so the braces here are in the class. */
	@Test
	public void bracesInACharacterClassAreNotARepetition()
	{
		RegExpFunctions.Limits limits = new RegExpFunctions.Limits(100, 100, 60_000);

		assertEquals(1, RegExpFunctions.repetitionWeight("[]a{1000001}]"));
		assertEquals(1, RegExpFunctions.repetitionWeight("[^]a{1000001}]"));
		assertEquals(1, RegExpFunctions.repetitionWeight("[\\]{1000001}]"));
		assertEquals(1, RegExpFunctions.repetitionWeight("[\\Q]\\E{1000001}]"));
		assertTrue(RegExpFunctions.matches("[]a{1000001}]", "a", limits));
		assertTrue(RegExpFunctions.matches("[^]a{1000001}]", "b", limits));
	}

	/**
	 * The scanner reads a pattern without quoted text. The pattern
	 * without quoting must mean the same to java.util.regex as the
	 * pattern with it.
	 */
	@Test
	public void patternWithoutQuotingMeansTheSame()
	{
		String[] patterns = {
				"a\\Q.\\Eb", "x\\Q\\E{3}", "\\Q1\\E{2}", "[\\Q]\\E]+", "\\Q\\\\E", "\\Qa\\E\\Q(\\E",
				"\\\\Q", "a\\Q{2}", "\\Q[a\\E]", "\\d\\Q9\\E", "\\Q\u00e9*\\E", "a\\Q\\E", "\\Qa(b\\E\\)"};
		String[] inputs = {
				"", "a.b", "axb", "xxx", "xx", "11", "1", "]]", "\\", "a(", "\\Q", "a{2}", "aa", "[a]", "59",
				"\u00e9*", "a", "a(b)"};

		for(String pattern : patterns){
			String unquoted = RegExpFunctions.withoutQuoting(pattern);
			for(String input : inputs){
				assertEquals(Pattern.compile(pattern).matcher(input).find(),
						Pattern.compile(unquoted).matcher(input).find(),
						() -> pattern + " as " + unquoted + " on " + input);
			}
		}
		assertEquals("abc", RegExpFunctions.withoutQuoting("abc"));
		assertEquals("x{3}", RegExpFunctions.withoutQuoting("x\\Q\\E{3}"));
	}

	/**
	 * rv087 rounds 2 and 3: a match that works without a read of the input
	 * is not stopped; its result is not used when it comes after the time
	 * limit. The test gives the clock readings: for an empty input the
	 * clock is read at the start, after the compilation, and after the
	 * match.
	 */
	@Test
	public void resultThatComesAfterTheTimeLimitIsNotUsed()
	{
		RegExpFunctions.Limits limits = new RegExpFunctions.Limits(100, 3_000, 1_000);
		long limitNanos = Duration.ofMillis(1_000).toNanos();

		TestClock onTime = new TestClock(5, 5, 5 + limitNanos);
		assertTrue(RegExpFunctions.matches("a*", "", limits, onTime));
		assertEquals(3, onTime.reads);

		TestClock lateAfterTheMatch = new TestClock(5, 5, 5 + limitNanos + 1);
		assertLimitExceeded(assertThrows(EvaluationException.class,
				() -> RegExpFunctions.matches("a*", "", limits, lateAfterTheMatch)));
		assertEquals(3, lateAfterTheMatch.reads);

		TestClock lateAfterTheCompilation = new TestClock(5, 5 + limitNanos + 1);
		assertLimitExceeded(assertThrows(EvaluationException.class,
				() -> RegExpFunctions.matches("a*", "", limits, lateAfterTheCompilation)));
		assertEquals(2, lateAfterTheCompilation.reads);

		// 2,000 reads of the input: the clock is read at read 1,024, and the match stops there.
		TestClock lateWhileReading = new TestClock(5, 5, 5 + limitNanos + 1);
		assertLimitExceeded(assertThrows(EvaluationException.class,
				() -> RegExpFunctions.matches("b", "a".repeat(2_000), limits, lateWhileReading)));
		assertEquals(3, lateWhileReading.reads);

		// The same input on time: one clock read while reading, one after the match.
		TestClock onTimeWhileReading = new TestClock(5, 5, 5, 5);
		assertFalse(RegExpFunctions.matches("b", "a".repeat(2_000), limits, onTimeWhileReading));
		assertEquals(4, onTimeWhileReading.reads);
	}

	/**
	 * rv087 round 3: a counted quantifier after a quantifier repeats an
	 * empty atom of its own; the two counts do not multiply.
	 */
	@Test
	public void quantifiersNextToEachOtherDoNotMultiply()
	{
		RegExpFunctions.Limits limits = new RegExpFunctions.Limits(100, 100, 60_000);

		assertEquals(1_001, RegExpFunctions.repetitionWeight("(?:){1000}{1001}"));
		assertEquals(1_001, RegExpFunctions.repetitionWeight("(?:){1000}?{1001}"));
		assertEquals(3, RegExpFunctions.repetitionWeight("a{2}{3}"));
		assertEquals(3_000, RegExpFunctions.repetitionWeight("(?:(?:){1000}{2}){3}"));
		assertTrue(Pattern.compile("(?:){1000}{1001}").matcher("").find());
		assertTrue(RegExpFunctions.matches("(?:){1000}{1001}", "", limits));
	}

	/**
	 * The rules of the scanner, shown on java.util.regex itself by what a
	 * pattern matches. When a JDK reads one of these patterns in another
	 * way, this test fails and the scanner has to follow.
	 */
	@Test
	public void scannerRulesAreTheRulesOfJavaUtilRegex()
	{
		// A quantifier after a quantifier repeats an empty atom, not "(?:a){2}".
		assertTrue(Pattern.compile("^(?:a){2}{3}$").matcher("aa").find());
		assertFalse(Pattern.compile("^(?:a){2}{3}$").matcher("aaaaaa").find());
		// A quantifier directly after "(" repeats an empty atom; it is not the text "{3}".
		assertTrue(Pattern.compile("^({3})a$").matcher("a").find());
		assertFalse(Pattern.compile("^({3})a$").matcher("{3}a").find());
		// "\c[" is one control-character escape; the "(" after it opens a group.
		assertTrue(Pattern.compile("^\\c[(a){2}$").matcher("\u001Baa").find());
		// An empty quote disappears; the quantifier belongs to the group before it.
		assertTrue(Pattern.compile("^(?:ab)\\Q\\E{2}$").matcher("abab").find());
		assertFalse(Pattern.compile("^(?:ab)\\Q\\E{2}$").matcher("ab").find());
		// "]" directly after "[" or "[^" is a literal; the class ends at the next "]".
		assertTrue(Pattern.compile("^[]a(]{3}$").matcher("]a(").find());
		assertTrue(Pattern.compile("^[^]a(]{3}$").matcher("xyz").find());
		assertFalse(Pattern.compile("^[^]a(]{3}$").matcher("x]z").find());
		// A lone "\E" is not accepted, so it can not stand between a group and its quantifier.
		assertThrows(java.util.regex.PatternSyntaxException.class, () -> Pattern.compile("(?:a)\\E{2}"));
	}

	@Test
	public void limitsAreReadFromProperties()
	{
		String prefix = RegExpFunctions.class.getName();
		Map<String, String> properties = Map.of(
				prefix + ".maxPatternLength", "4",
				prefix + ".maxInputLength", " 5 ",
				prefix + ".maxMatchMillis", "60000",
				prefix + ".maxRepetition", "6");
		RegExpFunctions.Limits configured = RegExpFunctions.Limits.from(properties::get);
		RegExpFunctions.Limits defaults = RegExpFunctions.Limits.from(name -> null);

		// Each configured value exactly at its limit, then one beyond it.
		assertFalse(RegExpFunctions.matches("a{6}", "aaaaa", configured));
		assertTrue(RegExpFunctions.matches("abcd", "abcd", configured));
		assertLimitExceeded(assertThrows(EvaluationException.class,
				() -> RegExpFunctions.matches("abcde", "abcde", configured)));
		assertLimitExceeded(assertThrows(EvaluationException.class,
				() -> RegExpFunctions.matches("a", "aaaaaa", configured)));
		assertLimitExceeded(assertThrows(EvaluationException.class,
				() -> RegExpFunctions.matches("a{7}", "", configured)));
		long configuredNanos = Duration.ofMillis(60_000).toNanos();
		assertTrue(RegExpFunctions.matches("abcd", "abcd", configured, new TestClock(0, 0, configuredNanos)));
		assertLimitExceeded(assertThrows(EvaluationException.class,
				() -> RegExpFunctions.matches("abcd", "abcd", configured, new TestClock(0, 0, configuredNanos + 1))));

		assertTrue(RegExpFunctions.matches("abcde", "abcde", defaults));
		assertLimitExceeded(assertThrows(EvaluationException.class,
				() -> RegExpFunctions.matches("a".repeat(RegExpFunctions.DEFAULT_MAX_PATTERN_LENGTH + 1), "a", defaults)));
		assertLimitExceeded(assertThrows(EvaluationException.class,
				() -> RegExpFunctions.matches("a{1000001}", "a", defaults)));
		long defaultNanos = Duration.ofMillis(RegExpFunctions.DEFAULT_MAX_MATCH_MILLIS).toNanos();
		assertTrue(RegExpFunctions.matches("a", "a", defaults, new TestClock(0, 0, defaultNanos)));
		assertLimitExceeded(assertThrows(EvaluationException.class,
				() -> RegExpFunctions.matches("a", "a", defaults, new TestClock(0, 0, defaultNanos + 1))));
	}

	/**
	 * rv087 round 4: the largest weight means "does not fit in a long" or
	 * "can not be scanned". It is beyond the largest limit that can be
	 * configured too.
	 */
	@Test
	public void weightThatDoesNotFitIsBeyondEveryLimit()
	{
		RegExpFunctions.Limits largest = new RegExpFunctions.Limits(100, 100, 60_000, Long.MAX_VALUE);
		String overflow = "(?:(?:(?:){2147483647}){2147483647}){3}";
		String commentsMode = "(?x)a";

		assertEquals(Long.MAX_VALUE, RegExpFunctions.repetitionWeight(overflow));
		assertEquals(Long.MAX_VALUE, RegExpFunctions.repetitionWeight(commentsMode));
		// Without the bound the first match does not return; the timeout turns that into a failure.
		assertTimeoutPreemptively(Duration.ofSeconds(20), () -> {
			assertLimitExceeded(assertThrows(EvaluationException.class,
					() -> RegExpFunctions.matches(overflow, "", largest)));
			assertLimitExceeded(assertThrows(EvaluationException.class,
					() -> RegExpFunctions.matches(commentsMode, "a", largest)));
		});
		// A weight that fits is compared with the limit as before.
		assertTrue(RegExpFunctions.matches("(?:(?:){1000}){1000}", "", largest));
	}

	@Test
	public void propertyThatIsNotAPositiveNumberIsRejected()
	{
		String name = RegExpFunctions.class.getName() + ".maxMatchMillis";
		String cacheSize = RegExpFunctions.class.getName() + ".cacheSize";

		for(String value : new String[]{"0", "-1", "abc", "", "2147483648"}){
			IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
					() -> RegExpFunctions.Limits.from(Map.of(name, value)::get), value);
			assertTrue(e.getMessage().startsWith(name), e::getMessage);
			e = assertThrows(IllegalArgumentException.class,
					() -> RegExpFunctions.cacheSize(Map.of(cacheSize, value)::get), value);
			assertTrue(e.getMessage().startsWith(cacheSize), e::getMessage);
		}
		assertEquals(7, RegExpFunctions.cacheSize(Map.of(cacheSize, "7")::get));
		assertEquals(RegExpFunctions.DEFAULT_CACHE_SIZE, RegExpFunctions.cacheSize(name2 -> null));
	}

	/**
	 * rv087 round 5: a limit at the top of the int range must not switch a
	 * check off. The cache evicts when its size is above its limit, and a
	 * pattern grows when it is rewritten, so both limits end below
	 * Integer.MAX_VALUE.
	 */
	@Test
	public void largestValuesThatCanBeConfigured()
	{
		String prefix = RegExpFunctions.class.getName();
		String largestCache = String.valueOf(RegExpFunctions.LARGEST_CACHE_SIZE);
		String largestPattern = String.valueOf(RegExpFunctions.LARGEST_MAX_PATTERN_LENGTH);

		assertEquals(Integer.MAX_VALUE - 1, RegExpFunctions.LARGEST_CACHE_SIZE);
		assertEquals(Integer.MAX_VALUE / 8, RegExpFunctions.LARGEST_MAX_PATTERN_LENGTH);
		assertEquals(RegExpFunctions.LARGEST_CACHE_SIZE,
				RegExpFunctions.cacheSize(Map.of(prefix + ".cacheSize", largestCache)::get));
		assertThrows(IllegalArgumentException.class,
				() -> RegExpFunctions.cacheSize(Map.of(prefix + ".cacheSize", "2147483647")::get));
		assertThrows(IllegalArgumentException.class, () -> new RegExpFunctions.PatternCache(Integer.MAX_VALUE));
		assertThrows(IllegalArgumentException.class, () -> new RegExpFunctions.PatternCache(0));

		assertNotNull(RegExpFunctions.Limits.from(Map.of(prefix + ".maxPatternLength", largestPattern)::get));
		assertThrows(IllegalArgumentException.class, () -> RegExpFunctions.Limits.from(
				Map.of(prefix + ".maxPatternLength", String.valueOf(RegExpFunctions.LARGEST_MAX_PATTERN_LENGTH + 1))::get));
		assertThrows(IllegalArgumentException.class,
				() -> new RegExpFunctions.Limits(RegExpFunctions.LARGEST_MAX_PATTERN_LENGTH + 1, 1, 1));
		// The other limits take the whole range: no arithmetic is done on them.
		assertNotNull(RegExpFunctions.Limits.from(Map.of(
				prefix + ".maxInputLength", "2147483647",
				prefix + ".maxMatchMillis", "2147483647",
				prefix + ".maxRepetition", "9223372036854775807")::get));
	}

	@Test
	public void repetitionWeightIsTheLargestProductOfNestedMinimumCounts()
	{
		assertEquals(1, RegExpFunctions.repetitionWeight("a+b*c?"));
		assertEquals(4, RegExpFunctions.repetitionWeight("[0-9]{4}-[0-9]{2}-[0-9]{2}"));
		assertEquals(1_000_000, RegExpFunctions.repetitionWeight("(?:(?:){1000}){1000}"));
		assertEquals(6, RegExpFunctions.repetitionWeight("(a{2}){3}|b{5}"));
		// The minimum counts, not the maximum.
		assertEquals(2, RegExpFunctions.repetitionWeight("(a{0,1000}){2,9}"));
		// Braces that are not a quantifier.
		assertEquals(1, RegExpFunctions.repetitionWeight("\\p{IsBasicLatin}\\x{41}\\{9}[{9}]\\Q{9}\\E"));
		assertEquals(3, RegExpFunctions.repetitionWeight("\\d{3}"));
		assertEquals(9, RegExpFunctions.repetitionWeight("[a-z&&[^aeiou]]{9}"));
		assertEquals(Long.MAX_VALUE,
				RegExpFunctions.repetitionWeight("(((((){2147483647}){2147483647}){2147483647}){2147483647})"));
		// Group prefixes are not quantifiers.
		assertEquals(3, RegExpFunctions.repetitionWeight("(?<year>[0-9]{3})(?=a)(?!b)(?i)"));
	}

	/**
	 * In comments mode a "#" hides the rest of a line, so a bracket in a
	 * comment could hide a quantifier from the scanner. A pattern that
	 * turns the mode on gets the largest weight and is rejected.
	 */
	@Test
	public void commentsModeIsRejectedBecauseItCanHideAQuantifier()
	{
		String hidden = "(?x)(?:(?:){100000} # [\n){100000}";

		assertEquals(Long.MAX_VALUE, RegExpFunctions.repetitionWeight(hidden));
		assertEquals(Long.MAX_VALUE, RegExpFunctions.repetitionWeight("(?ix:a)"));
		assertEquals(1, RegExpFunctions.repetitionWeight("(?i-x:a)"));
		assertLimitExceeded(assertThrows(EvaluationException.class,
				() -> RegExpFunctions.matches(hidden, "", new RegExpFunctions.Limits(100, 100, 60_000))));
	}

	@Test
	public void inputLongerThanTheLimitIsIndeterminate()
	{
		RegExpFunctions.Limits limits = new RegExpFunctions.Limits(100, 10, 1_000);

		assertLimitExceeded(assertThrows(EvaluationException.class,
				() -> RegExpFunctions.matches("a", "01234567890", limits)));
	}

	@Test
	public void patternLongerThanTheLimitIsIndeterminate()
	{
		RegExpFunctions.Limits limits = new RegExpFunctions.Limits(10, 100, 1_000);

		assertLimitExceeded(assertThrows(EvaluationException.class,
				() -> RegExpFunctions.matches("01234567890", "a", limits)));
	}

	@Test
	public void limitsMustBePositive()
	{
		assertThrows(IllegalArgumentException.class, () -> new RegExpFunctions.Limits(0, 1, 1));
		assertThrows(IllegalArgumentException.class, () -> new RegExpFunctions.Limits(1, 0, 1));
		assertThrows(IllegalArgumentException.class, () -> new RegExpFunctions.Limits(1, 1, 0));
		assertThrows(IllegalArgumentException.class, () -> new RegExpFunctions.Limits(1, 1, 1, 0));
	}

	@Test
	public void registeredFunctionIsIndeterminateBeyondTheInputLimit()
	{
		FunctionProvider functions = FunctionProviderBuilder.builder().withDefaultFunctions().build();
		EvaluationContext context = mock(EvaluationContext.class);
		String tooLong = "a".repeat(RegExpFunctions.DEFAULT_MAX_INPUT_LENGTH + 1);
		String atTheLimit = "a".repeat(RegExpFunctions.DEFAULT_MAX_INPUT_LENGTH);

		EvaluationException e = assertThrows(EvaluationException.class,
				() -> functions.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-regexp-match")
						.orElseThrow()
						.invoke(context, XacmlTypes.STRING.ofAny("a"), XacmlTypes.STRING.ofAny(tooLong)));

		assertLimitExceeded(e);
		assertEquals(BooleanVal.of(true),
				functions.getFunction("urn:oasis:names:tc:xacml:1.0:function:string-regexp-match")
						.orElseThrow()
						.invoke(context, XacmlTypes.STRING.ofAny("a$"), XacmlTypes.STRING.ofAny(atTheLimit)));
	}

	@Test
	public void patternCacheKeepsTheMostRecentlyUsedPatterns()
	{
		RegExpFunctions.PatternCache cache = new RegExpFunctions.PatternCache(2);

		Pattern a = cache.get("a");
		cache.get("b");
		// "a" is used again, so "b" is the least recently used entry.
		assertSame(a, cache.get("a"));
		cache.get("c");

		assertEquals(2, cache.size());
		assertNotNull(cache.peek("a"));
		assertNotNull(cache.peek("c"));
		assertNull(cache.peek("b"));
	}

	/** A clock that gives the listed readings in turn, then the last one again, and counts its reads. */
	private static final class TestClock implements LongSupplier
	{
		private final long[] readings;
		private int reads;

		TestClock(long... readings){
			this.readings = readings;
		}

		@Override
		public long getAsLong(){
			return readings[Math.min(reads++, readings.length - 1)];
		}
	}

	/** Processing-error with the fixed message, which names neither the pattern nor the input. */
	private static void assertLimitExceeded(EvaluationException e)
	{
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, e.getStatus().getStatusCode().getValue());
		assertEquals(Optional.of(RegExpFunctions.LIMIT_EXCEEDED_MESSAGE), e.getStatus().getMessage());
	}
}
