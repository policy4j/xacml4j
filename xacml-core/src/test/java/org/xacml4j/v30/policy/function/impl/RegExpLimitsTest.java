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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.LongSupplier;

import org.junit.jupiter.api.Test;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.policy.function.FunctionProviderBuilder;
import org.xacml4j.v30.policy.function.regex.CompiledRegex;
import org.xacml4j.v30.policy.function.regex.RegexCompileException;
import org.xacml4j.v30.policy.function.regex.RegexEngine;
import org.xacml4j.v30.policy.function.regex.RegexLimits;
import org.xacml4j.v30.policy.function.regex.RegexProfile;
import org.xacml4j.v30.types.BooleanVal;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * Maintainer ruling 2026-10-07 (run 061 D17, bead policy4j-the.4.51): a
 * regular-expression match has a limit on the pattern length, on the input
 * length, and on the time of one match, and the optional pattern cache is
 * bounded. Beyond a limit the function is Indeterminate with status
 * processing-error. The limits are those of the {@code jdk} engine's
 * {@code legacy} profile (change add-regex-engine-spi, design D7); the rules of
 * its repetition scanner are in {@code LegacySyntaxTest}.
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
		assertEquals(10_000, RegexLimits.DEFAULT_MAX_PATTERN_LENGTH);
		assertEquals(100_000, RegexLimits.DEFAULT_MAX_INPUT_LENGTH);
		assertEquals(1_000, RegexLimits.DEFAULT_MAX_MATCH_MILLIS);
		assertEquals(1_000_000, RegexLimits.DEFAULT_MAX_REPETITION);
		assertEquals(256, RegexLimits.DEFAULT_CACHE_SIZE);
		assertEquals(RegexLimits.defaults(), RegExpFunctions.limitsFrom(name -> null));
	}

	@Test
	public void matchWithinTheLimitsIsUnchanged()
	{
		RegexLimits limits = limits(10, 10, 1_000);

		assertTrue(RegExpFunctions.matches("a+b", "xaab", limits));
		assertFalse(RegExpFunctions.matches("a+b$", "xaabc", limits));
		// Exactly at both length limits.
		assertTrue(RegExpFunctions.matches("0123456789", "0123456789", limits));
	}

	@Test
	public void catastrophicBacktrackingIsStoppedByTheTimeLimit()
	{
		RegexLimits limits = limits(100, 100, 50);

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
		RegexLimits limits = limits(100, 100, 60_000);

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
		RegexLimits limits = limits(100, 100, 60_000);
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
	}

	/** rv087 round 2: a "]" directly after "[" is a literal, so the braces here are in the class. */
	@Test
	public void bracesInACharacterClassAreNotARepetition()
	{
		RegexLimits limits = limits(100, 100, 60_000);

		assertTrue(RegExpFunctions.matches("[]a{1000001}]", "a", limits));
		assertTrue(RegExpFunctions.matches("[^]a{1000001}]", "b", limits));
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
		RegexLimits limits = limits(100, 3_000, 1_000);
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
		RegexLimits limits = limits(100, 100, 60_000);

		assertTrue(RegExpFunctions.matches("(?:){1000}{1001}", "", limits));
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
		RegexLimits configured = RegExpFunctions.limitsFrom(properties::get);
		RegexLimits defaults = RegExpFunctions.limitsFrom(name -> null);

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
				() -> RegExpFunctions.matches("a".repeat(RegexLimits.DEFAULT_MAX_PATTERN_LENGTH + 1), "a", defaults)));
		assertLimitExceeded(assertThrows(EvaluationException.class,
				() -> RegExpFunctions.matches("a{1000001}", "a", defaults)));
		long defaultNanos = Duration.ofMillis(RegexLimits.DEFAULT_MAX_MATCH_MILLIS).toNanos();
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
		RegexLimits largest = RegexLimits.builder().maxPatternLength(100).maxInputLength(100)
				.maxMatchMillis(60_000).maxRepetition(Long.MAX_VALUE).build();
		String overflow = "(?:(?:(?:){2147483647}){2147483647}){3}";
		String commentsMode = "(?x)a";

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
					() -> RegExpFunctions.limitsFrom(Map.of(name, value)::get), value);
			assertTrue(e.getMessage().startsWith(name), e::getMessage);
			e = assertThrows(IllegalArgumentException.class,
					() -> RegExpFunctions.cacheSize(Map.of(cacheSize, value)::get), value);
			assertTrue(e.getMessage().startsWith(cacheSize), e::getMessage);
		}
		assertEquals(7, RegExpFunctions.cacheSize(Map.of(cacheSize, "7")::get));
		assertEquals(RegexLimits.DEFAULT_CACHE_SIZE, RegExpFunctions.cacheSize(name2 -> null));
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
		String largestCache = String.valueOf(RegexLimits.LARGEST_CACHE_SIZE);
		String largestPattern = String.valueOf(RegexLimits.LARGEST_MAX_PATTERN_LENGTH);

		assertEquals(Integer.MAX_VALUE - 1, RegexLimits.LARGEST_CACHE_SIZE);
		assertEquals(Integer.MAX_VALUE / 8, RegexLimits.LARGEST_MAX_PATTERN_LENGTH);
		assertEquals(RegexLimits.LARGEST_CACHE_SIZE,
				RegExpFunctions.cacheSize(Map.of(prefix + ".cacheSize", largestCache)::get));
		assertThrows(IllegalArgumentException.class,
				() -> RegExpFunctions.cacheSize(Map.of(prefix + ".cacheSize", "2147483647")::get));
		assertThrows(IllegalArgumentException.class,
				() -> RegexLimits.builder().cacheSize(Integer.MAX_VALUE).build());

		assertNotNull(RegExpFunctions.limitsFrom(Map.of(prefix + ".maxPatternLength", largestPattern)::get));
		assertThrows(IllegalArgumentException.class, () -> RegExpFunctions.limitsFrom(
				Map.of(prefix + ".maxPatternLength", String.valueOf(RegexLimits.LARGEST_MAX_PATTERN_LENGTH + 1))::get));
		assertThrows(IllegalArgumentException.class,
				() -> limits(RegexLimits.LARGEST_MAX_PATTERN_LENGTH + 1, 1, 1));
		// The other limits take the whole range: no arithmetic is done on them.
		assertNotNull(RegExpFunctions.limitsFrom(Map.of(
				prefix + ".maxInputLength", "2147483647",
				prefix + ".maxMatchMillis", "2147483647",
				prefix + ".maxRepetition", "9223372036854775807")::get));
	}

	/**
	 * In comments mode a "#" hides the rest of a line, so a bracket in a
	 * comment could hide a quantifier from the scanner. A pattern that
	 * turns the mode on is rejected.
	 */
	@Test
	public void commentsModeIsRejectedBecauseItCanHideAQuantifier()
	{
		String hidden = "(?x)(?:(?:){100000} # [\n){100000}";

		assertLimitExceeded(assertThrows(EvaluationException.class,
				() -> RegExpFunctions.matches(hidden, "", limits(100, 100, 60_000))));
	}

	@Test
	public void inputLongerThanTheLimitIsIndeterminate()
	{
		RegexLimits limits = limits(100, 10, 1_000);

		assertLimitExceeded(assertThrows(EvaluationException.class,
				() -> RegExpFunctions.matches("a", "01234567890", limits)));
	}

	@Test
	public void patternLongerThanTheLimitIsIndeterminate()
	{
		RegexLimits limits = limits(10, 100, 1_000);

		assertLimitExceeded(assertThrows(EvaluationException.class,
				() -> RegExpFunctions.matches("01234567890", "a", limits)));
	}

	@Test
	public void limitsMustBePositive()
	{
		assertThrows(IllegalArgumentException.class, () -> limits(0, 1, 1));
		assertThrows(IllegalArgumentException.class, () -> limits(1, 0, 1));
		assertThrows(IllegalArgumentException.class, () -> limits(1, 1, 0));
		assertThrows(IllegalArgumentException.class, () -> RegexLimits.builder().maxRepetition(0).build());
		assertThrows(IllegalArgumentException.class, () -> RegexLimits.builder().maxRepeatCount(0).build());
		assertThrows(IllegalArgumentException.class, () -> RegexLimits.builder().maxProgramSize(0).build());
		assertThrows(IllegalArgumentException.class, () -> RegexLimits.builder().cacheSize(0).build());
	}

	@Test
	public void registeredFunctionIsIndeterminateBeyondTheInputLimit()
	{
		FunctionProvider functions = FunctionProviderBuilder.builder().withDefaultFunctions().build();
		EvaluationContext context = mock(EvaluationContext.class);
		String tooLong = "a".repeat(RegexLimits.DEFAULT_MAX_INPUT_LENGTH + 1);
		String atTheLimit = "a".repeat(RegexLimits.DEFAULT_MAX_INPUT_LENGTH);

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

	/**
	 * Design D6: compiled patterns are reused through a cache per engine instance of
	 * {@code cacheSize} entries, the least recently used evicted first; a kept pattern answers
	 * without compiling again. The eviction order is pinned through decisions in
	 * {@code RegexCompileOnceTest} of the PDP module; here the cache of one instance keeps a
	 * pattern that its engine compiles only once.
	 */
	@Test
	public void compiledPatternsAreReusedPerInstance()
	{
		RegexEngine once = new RegexEngine(){
			private final Set<String> compiled = new HashSet<>();

			@Override
			public String name(){
				return "once";
			}

			@Override
			public Set<RegexProfile> profiles(){
				return Set.of(RegexProfile.LEGACY);
			}

			@Override
			public CompiledRegex compile(String pattern, RegexProfile profile, RegexLimits limits){
				if(!compiled.add(pattern)){
					throw RegexCompileException.malformed("compiled again");
				}
				return (input, budget) -> true;
			}
		};
		RegExpFunctions.Matching matching = new RegExpFunctions.Matching(once, RegexProfile.LEGACY,
				RegexLimits.builder().cacheSize(2).build());

		assertTrue(matching.matches("a", ""));
		assertTrue(matching.matches("a", ""));
		assertTrue(matching.matches("b", ""));
		assertTrue(matching.matches("a", ""));
		// Another instance over the same engine compiles the pattern again, and the engine rejects it
		EvaluationException e = assertThrows(EvaluationException.class, () -> new RegExpFunctions.Matching(once,
				RegexProfile.LEGACY, RegexLimits.builder().cacheSize(2).build()).matches("a", ""));
		assertEquals(StatusCodeId.SYNTAX_ERROR, e.getStatus().getStatusCode().getValue());
	}

	private static RegexLimits limits(int maxPatternLength, int maxInputLength, long maxMatchMillis)
	{
		return RegexLimits.builder().maxPatternLength(maxPatternLength).maxInputLength(maxInputLength)
				.maxMatchMillis(maxMatchMillis).build();
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
