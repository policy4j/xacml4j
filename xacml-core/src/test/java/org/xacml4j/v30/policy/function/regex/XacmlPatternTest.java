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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.xacml4j.v30.policy.function.regex.XacmlPattern.Alternation;
import org.xacml4j.v30.policy.function.regex.XacmlPattern.Anchor;
import org.xacml4j.v30.policy.function.regex.XacmlPattern.BackReference;
import org.xacml4j.v30.policy.function.regex.XacmlPattern.CharClass;
import org.xacml4j.v30.policy.function.regex.XacmlPattern.Group;
import org.xacml4j.v30.policy.function.regex.XacmlPattern.Literal;
import org.xacml4j.v30.policy.function.regex.XacmlPattern.Sequence;
import org.xacml4j.v30.policy.function.regex.jdk.JdkRegexEngine;

/**
 * The grammar of the {@code xf} profile, XF 1.0 section 7.6.1 of 23 January 2007 (change
 * add-regex-engine-spi, designs D2 and D4): what it accepts and rejects, the flat code-point set
 * of every class, and the size estimate.
 */
public class XacmlPatternTest
{
	@ParameterizedTest(name = "accepts <{0}>")
	@ValueSource(strings = {
			"", "a", "abc", "a|b", "a|", "|", "()", "(a)(b)", "^a$", "a*", "a+", "a?", "a{2}", "a{2,}", "a{2,5}",
			"a{0}", "a{1001}", "a*?", "a{2,5}?", "\\n\\r\\t\\\\\\|\\.\\?\\*\\+\\{\\}\\(\\)\\[\\]\\^\\-\\$",
			"\\s\\S\\i\\I\\c\\C\\d\\D\\w\\W", ".", "\\p{L}\\p{Lu}\\P{Lo}\\p{Nd}\\p{C}\\p{Cn}\\p{Zs}\\p{Sm}\\p{Pc}\\p{Me}",
			"\\p{IsBasicLatin}", "\\p{IsGreek}", "\\P{IsLatin-1Supplement}", "\\p{IsPrivateUse}",
			"\\p{IsCombiningMarksforSymbols}", "[a]", "[abc]", "[a-z]", "[-a]", "[a-]", "[-]", "[a^]", "[$]", "[^a]",
			"[^a-z]", "[\\d_]", "[\\--a]", "[\\[-a]", "[a-z-[aeiou]]", "[^a-z-[aeiou]]", "[\\i-[:]]", "[a-[b-[c]]]",
			"(a)\\1", "(a)(b)\\2", "((a))\\2\\1", "[\\s\\p{L}]", "[.]", "[*?+(){}|]"})
	public void grammarAccepts(String pattern)
	{
		assertEquals(pattern, XacmlPattern.parse(pattern).source());
	}

	@ParameterizedTest(name = "rejects <{0}>")
	@ValueSource(strings = {
			"(", ")", "a)", "(a", "*a", "+a", "?a", "a**", "a+*", "a?+", "a{2}??", "a{2,1}", "a{x}", "a{", "a{2",
			"a{,2}", "{2}", "}", "]", "[", "[]", "[^]", "[a", "[a-", "[[a]]", "[a]]", "[--z]", "[a-b-c]", "[%--]",
			"[z-a]", "[a-\\-]", "[\\d-z]", "[a-\\d]", "\\", "a\\", "\\a", "\\e", "\\0", "\\x41", "\\u0041", "\\Qa\\E",
			"\\A", "\\z", "\\b", "\\B", "\\k<n>", "\\pL", "\\p", "\\p{", "\\p{L", "\\p{}", "\\p{Cs}", "\\p{Greek}",
			"\\p{IsNoSuchBlock}", "\\p{Is}", "\\p{Is Basic Latin}", "\\1", "(a\\1)", "(a)\\2", "(?:a)", "(?i)a",
			"(?=a)", "(?<n>a)", "(?", "a{99999999999}?*"})
	public void grammarRejects(String pattern)
	{
		RegexCompileException e = assertThrows(RegexCompileException.class, () -> XacmlPattern.parse(pattern));
		assertEquals(RegexException.Kind.MALFORMED, e.kind());
	}

	@Test
	public void treeKeepsAnchorsGroupsAndBackReferences()
	{
		XacmlPattern pattern = XacmlPattern.parse("^a(b|c)\\1$");

		Sequence root = assertInstanceOf(Sequence.class, pattern.root());
		assertEquals(5, root.pieces().size());
		assertEquals(Anchor.START, root.pieces().get(0));
		assertEquals(new Literal('a'), root.pieces().get(1));
		Group group = assertInstanceOf(Group.class, root.pieces().get(2));
		assertEquals(1, group.number());
		assertInstanceOf(Alternation.class, group.body());
		assertEquals(new BackReference(1), root.pieces().get(3));
		assertEquals(Anchor.END, root.pieces().get(4));
		assertEquals(1, pattern.groups());
		assertTrue(pattern.hasBackReferences());
	}

	@Test
	public void aBackReferenceInAClassIsUnsupported()
	{
		RegexCompileException failure = assertThrows(RegexCompileException.class,
				() -> XacmlPattern.parse("(a)[\\1]"));
		assertEquals(RegexException.Kind.UNSUPPORTED, failure.kind());
	}

	@Test
	public void nestedRepetitionsOfEmptyGroupsAreLimitedBeforeMatching()
	{
		RegexCompileException failure = assertThrows(RegexCompileException.class,
				() -> new JdkRegexEngine().compile("(((){1000}){1000}){1000}", RegexProfile.XF, RegexLimits.defaults()));
		assertEquals(RegexException.Kind.LIMIT, failure.kind());
	}

	/** XF 1.0: further digits belong to a back-reference only while the number stays within the groups opened so far */
	@Test
	public void backReferenceDigitsBelongToItWhileTheGroupExists()
	{
		Sequence one = assertInstanceOf(Sequence.class, XacmlPattern.parse("(a)\\12").root());
		assertEquals(List.of(new BackReference(1), new Literal('2')), one.pieces().subList(1, 3));

		String twelve = "(a)".repeat(12) + "\\12";
		Sequence many = assertInstanceOf(Sequence.class, XacmlPattern.parse(twelve).root());
		assertEquals(new BackReference(12), many.pieces().get(12));
		assertEquals(12, XacmlPattern.parse(twelve).groups());
	}

	@Test
	public void classesAreFlatCodePointSetsOfTheXmlSchemaRules()
	{
		CodePointSet consonants = codePoints("[a-z-[aeiou]]");
		assertEquals(5, consonants.rangeCount());
		assertTrue(consonants.contains('b'));
		assertFalse(consonants.contains('a'));
		assertFalse(consonants.contains('A'));

		// XML Schema subtracts after it negates: everything but a to z
		CodePointSet notLetters = codePoints("[^a-z-[aeiou]]");
		assertFalse(notLetters.contains('a'));
		assertFalse(notLetters.contains('b'));
		assertTrue(notLetters.contains('A'));
		assertTrue(notLetters.contains('1'));
		assertTrue(notLetters.contains(0x10FFFF));

		CodePointSet digitsAndUnderscore = codePoints("[\\d_]");
		assertTrue(digitsAndUnderscore.contains('_'));
		assertTrue(digitsAndUnderscore.contains('7'));
		assertTrue(digitsAndUnderscore.contains(0x0663));
		assertFalse(digitsAndUnderscore.contains('a'));

		CodePointSet nameStartButColon = codePoints("[\\i-[:]]");
		assertTrue(nameStartButColon.contains('a'));
		assertTrue(nameStartButColon.contains('_'));
		assertFalse(nameStartButColon.contains(':'));
		assertFalse(nameStartButColon.contains('0'));

		assertEquals(CodePointSet.of('$'), codePoints("[$]"));
		assertEquals(CodePointSet.builder().add('a').add('^').build(), codePoints("[a^]"));
		assertEquals(CodePointSet.of(0, 0x7F), codePoints("\\p{IsBasicLatin}"));
		assertEquals(CodePointSet.of(0xE000, 0xF8FF), codePoints("\\p{IsPrivateUse}"));
		assertEquals(CodePointSet.of(0x20D0, 0x20FF), codePoints("\\p{IsCombiningMarksforSymbols}"));
	}

	@Test
	public void multiCharacterEscapesAndTheDotFollowXfAndXmlSchema()
	{
		// XF 1.0 section 7.6.1: "." is every character but the line feed
		CodePointSet dot = codePoints(".");
		assertFalse(dot.contains('\n'));
		assertTrue(dot.contains('\r'));
		assertTrue(dot.contains(0x85));
		assertTrue(dot.contains(0x2028));
		assertEquals(2, dot.rangeCount());

		assertEquals(CodePointSet.builder().add(0x20).add(0x09).add(0x0A).add(0x0D).build(), codePoints("\\s"));
		assertFalse(codePoints("\\s").contains(0x0B));

		CodePointSet word = codePoints("\\w");
		assertTrue(word.contains('a'));
		assertTrue(word.contains(0x0663));
		assertTrue(word.contains('+'));
		assertFalse(word.contains('_'));
		assertFalse(word.contains(' '));
		assertFalse(word.contains(0x0378));

		// Appendix F: C is Cc, Cf, Co and Cn; the surrogates are not characters
		CodePointSet other = codePoints("\\p{C}");
		assertTrue(other.contains(0x0378));
		assertTrue(other.contains(0x01));
		assertFalse(other.contains(0xD800));
		assertFalse(other.contains('a'));

		assertTrue(codePoints("\\p{Lo}").contains(0x0860));
		assertFalse(codePoints("\\P{L}").contains('a'));
		assertTrue(codePoints("\\P{L}").contains('1'));
		assertTrue(codePoints("\\c").contains('-'));
		assertFalse(codePoints("\\i").contains('-'));
		assertTrue(codePoints("\\C").contains(' '));
	}

	@ParameterizedTest(name = "<{0}> estimates {1}")
	@CsvSource(delimiter = ';', value = {
			"a;1", "abc;3", "a|bc;3", "(ab);3", "a{1000};2001", "a{2,5};11", "a{3,};9", "a*;3", "a+;5", "a?;3",
			"(a{1000}){10};20031", "[a-z];1", "[a-z-[aeiou]];5", "\\s;3", ".;2", "(a{1000}){1000};2003001", "a{0};1"})
	public void sizeEstimateAddsPiecesAndMultipliesCounts(String pattern, long estimate)
	{
		assertEquals(estimate, XacmlPattern.parse(pattern).sizeEstimate());
	}

	@Test
	public void sizeEstimateSaturatesAndTheLargestCountIsKept()
	{
		String deep = "(".repeat(7) + "a" + "){1000}".repeat(7);
		XacmlPattern pattern = XacmlPattern.parse(deep);
		assertEquals(Long.MAX_VALUE, pattern.sizeEstimate());
		assertEquals(1000, pattern.largestCount());
		assertEquals(7, XacmlPattern.parse("a{3,7}").largestCount());
		assertEquals(3, XacmlPattern.parse("a{3,}").largestCount());
		assertEquals(0, XacmlPattern.parse("a*").largestCount());
		assertEquals(Integer.MAX_VALUE, XacmlPattern.parse("a{99999999999}").largestCount());
	}

	static Stream<String> blockNames()
	{
		return Stream.of("BasicLatin", "Latin-1Supplement", "Greek", "Cyrillic", "Hiragana", "PrivateUse",
				"CombiningMarksforSymbols");
	}

	@ParameterizedTest(name = "block {0}")
	@MethodSource("blockNames")
	public void blocksOfAppendixFAreKnown(String name)
	{
		assertEquals(1, codePoints("\\p{Is" + name + "}").rangeCount());
	}

	private static CodePointSet codePoints(String pattern)
	{
		XacmlPattern parsed = XacmlPattern.parse(pattern);
		return ((CharClass)((Sequence)parsed.root()).pieces().get(0)).codePoints();
	}
}
