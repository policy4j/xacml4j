package org.xacml4j.v30.policy.function.regex.jdk;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

/**
 * The repetition scanner and the quoting of the {@code legacy} profile of the {@code jdk} engine:
 * the rules of rv087 (bead policy4j-the.4.58), moved with the code from {@code RegExpLimitsTest}
 * (change add-regex-engine-spi, task 3.2). The scanner reads a pattern as {@code java.util.regex}
 * does; its matches are the last test here.
 */
public class LegacySyntaxTest
{
	@Test
	public void translationKeepsSubstringMatchingAndRewritesSubtractionAndAnchors()
	{
		// XF 7.6.2 is a substring match: no ".*" padding and no empty-pattern rewrite.
		assertEquals("[0-9]{3}-[0-9]{3}-[0-9]{4}", LegacySyntax.toJavaSyntax("[0-9]{3}-[0-9]{3}-[0-9]{4}"));
		assertEquals("[a-z&&[^aeiou]]", LegacySyntax.toJavaSyntax("[a-z-[aeiou]]"));
		assertEquals("", LegacySyntax.toJavaSyntax(""));
		assertEquals("a\\$", LegacySyntax.toJavaSyntax("a\\$"));
		assertEquals("[$]", LegacySyntax.toJavaSyntax("[$]"));
		assertEquals("^foo\\z", LegacySyntax.toJavaSyntax("^foo$"));
		assertEquals("\\p{InBasicLatin}\\P{InGreek}", LegacySyntax.toJavaSyntax("\\p{IsBasicLatin}\\P{IsGreek}"));
	}

	/** rv087 round 2: constructs that hid a repetition from the first scanner */
	@Test
	public void repetitionsCanNotBeHiddenFromTheScanner()
	{
		assertEquals(1_001_000, LegacySyntax.repetitionWeight("\\c[(?:(?:){1000}){1001}"));
		assertEquals(1_001_000, LegacySyntax.repetitionWeight("(?:(?:){1000})\\Q\\E{1001}"));
		assertEquals(1_001_000, LegacySyntax.repetitionWeight("({1000}){1001}"));
		assertEquals(1_001_000, LegacySyntax.repetitionWeight("(?:a|{1000}){1001}"));
		assertEquals(1_001_000, LegacySyntax.repetitionWeight("(?:(?:){1000}[]a(]{0}){1001}"));
		assertEquals(1_001_000, LegacySyntax.repetitionWeight("(?:(?:){1000}[^]a(]{0}){1001}"));
		assertEquals(1_001_000, LegacySyntax.repetitionWeight("(?:(?:){1000}[a&&[]b(]]{0}){1001}"));
	}

	/** rv087 round 2: a "]" directly after "[" is a literal, so the braces here are in the class. */
	@Test
	public void bracesInACharacterClassAreNotARepetition()
	{
		assertEquals(1, LegacySyntax.repetitionWeight("[]a{1000001}]"));
		assertEquals(1, LegacySyntax.repetitionWeight("[^]a{1000001}]"));
		assertEquals(1, LegacySyntax.repetitionWeight("[\\]{1000001}]"));
		assertEquals(1, LegacySyntax.repetitionWeight("[\\Q]\\E{1000001}]"));
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
			String unquoted = LegacySyntax.withoutQuoting(pattern);
			for(String input : inputs){
				assertEquals(Pattern.compile(pattern).matcher(input).find(),
						Pattern.compile(unquoted).matcher(input).find(),
						() -> pattern + " as " + unquoted + " on " + input);
			}
		}
		assertEquals("abc", LegacySyntax.withoutQuoting("abc"));
		assertEquals("x{3}", LegacySyntax.withoutQuoting("x\\Q\\E{3}"));
	}

	/** rv087 round 3: a counted quantifier after a quantifier repeats an empty atom of its own; the two counts do not multiply. */
	@Test
	public void quantifiersNextToEachOtherDoNotMultiply()
	{
		assertEquals(1_001, LegacySyntax.repetitionWeight("(?:){1000}{1001}"));
		assertEquals(1_001, LegacySyntax.repetitionWeight("(?:){1000}?{1001}"));
		assertEquals(3, LegacySyntax.repetitionWeight("a{2}{3}"));
		assertEquals(3_000, LegacySyntax.repetitionWeight("(?:(?:){1000}{2}){3}"));
		assertTrue(Pattern.compile("(?:){1000}{1001}").matcher("").find());
	}

	/** rv087 round 4: the largest weight means "does not fit in a long" or "can not be scanned". */
	@Test
	public void weightThatDoesNotFitIsTheLargestWeight()
	{
		assertEquals(Long.MAX_VALUE, LegacySyntax.repetitionWeight("(?:(?:(?:){2147483647}){2147483647}){3}"));
		assertEquals(Long.MAX_VALUE, LegacySyntax.repetitionWeight("(?x)a"));
	}

	@Test
	public void repetitionWeightIsTheLargestProductOfNestedMinimumCounts()
	{
		assertEquals(1, LegacySyntax.repetitionWeight("a+b*c?"));
		assertEquals(4, LegacySyntax.repetitionWeight("[0-9]{4}-[0-9]{2}-[0-9]{2}"));
		assertEquals(1_000_000, LegacySyntax.repetitionWeight("(?:(?:){1000}){1000}"));
		assertEquals(6, LegacySyntax.repetitionWeight("(a{2}){3}|b{5}"));
		// The minimum counts, not the maximum.
		assertEquals(2, LegacySyntax.repetitionWeight("(a{0,1000}){2,9}"));
		// Braces that are not a quantifier.
		assertEquals(1, LegacySyntax.repetitionWeight("\\p{IsBasicLatin}\\x{41}\\{9}[{9}]\\Q{9}\\E"));
		assertEquals(3, LegacySyntax.repetitionWeight("\\d{3}"));
		assertEquals(9, LegacySyntax.repetitionWeight("[a-z&&[^aeiou]]{9}"));
		assertEquals(Long.MAX_VALUE,
				LegacySyntax.repetitionWeight("(((((){2147483647}){2147483647}){2147483647}){2147483647})"));
		// Group prefixes are not quantifiers.
		assertEquals(3, LegacySyntax.repetitionWeight("(?<year>[0-9]{3})(?=a)(?!b)(?i)"));
	}

	/**
	 * In comments mode a "#" hides the rest of a line, so a bracket in a
	 * comment could hide a quantifier from the scanner. A pattern that
	 * turns the mode on gets the largest weight.
	 */
	@Test
	public void commentsModeGetsTheLargestWeight()
	{
		assertEquals(Long.MAX_VALUE, LegacySyntax.repetitionWeight("(?x)(?:(?:){100000} # [\n){100000}"));
		assertEquals(Long.MAX_VALUE, LegacySyntax.repetitionWeight("(?ix:a)"));
		assertEquals(1, LegacySyntax.repetitionWeight("(?i-x:a)"));
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
}
