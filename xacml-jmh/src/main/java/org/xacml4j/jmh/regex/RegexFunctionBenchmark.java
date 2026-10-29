package org.xacml4j.jmh.regex;

/*
 * #%L
 * Xacml4J JMH Benchmarks
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.xacml4j.v30.policy.function.impl.RegExpFunctions;

/**
 * Measures the XACML regular-expression functions through the entry points that change
 * add-regex-engine-spi keeps (design D7): {@code compile} translates and compiles the next pattern,
 * {@code match} searches the next input with its compiled pattern, and {@code call} and
 * {@code callMiss} make the whole call of {@link RegExpFunctions#matches(String, String)}. The calls
 * of {@code call} repeat the few patterns of the set, as a pattern cache would find them; a call of
 * {@code callMiss} repeats its pattern only after {@value #MISSED_PATTERNS} others, more than a cache
 * of 256 patterns holds. Setup checks every pattern and input against the result that the input was
 * built for, so a wrong engine fails the trial instead of reporting numbers.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms2g", "-Xmx2g", "-XX:+UseG1GC"})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
public class RegexFunctionBenchmark
{
	/** The fewest distinct patterns that {@code callMiss} uses before it repeats one */
	static final int MISSED_PATTERNS = 1024;

	/** U+4E00, the first of the 20,992 CJK unified ideographs, which are literals in every pattern syntax */
	private static final char VARIANTS = (char)0x4E00;

	/**
	 * The patterns of the OASIS conformance cases that use string-regexp-match (IIB008, IIB009,
	 * IIC056, IIC057, IIC165 to IIC169), each with one of its shortest inputs
	 */
	private static final List<Case> CONFORMANCE = List.of(
			new Case("read|write", '~', "read"),
			new Case("J.* Hibbert", '~', "J Hibbert"),
			new Case("B.* Simpson", '~', "B Simpson"),
			new Case("J.* K.* Hibbert", '~', "J K Hibbert"),
			new Case("B.* O.* Simpson", '~', "B O Simpson"),
			new Case(" *This  is.* IT!  ", '~', "This  is IT!  "),
			new Case(" *This .*is not IT! *", '~', "This is not IT!"),
			new Case("     This .*is IT!  ", '~', "     This is IT!  "),
			new Case("   This  is n*o*t* *IT!  ", '~', "   This  is IT!  "),
			new Case("   This  is IT!  ", '~', "   This  is IT!  "),
			new Case("   This is not a match to IT!  ", '~', "   This is not a match to IT!  "),
			new Case("   This  is .*IT!  ", '~', "   This  is IT!  "));

	/**
	 * Patterns that a backtracking search misses in time quadratic in the input, each with one of its
	 * shortest inputs: trailing white space, a file type, and an address without anchors
	 */
	private static final List<Case> ADVERSARIAL = List.of(
			new Case("\\s+$", ' ', " "),
			new Case(".*\\.pdf$", 'a', ".pdf"),
			new Case("[a-z0-9._]+@example\\.com", 'a', "a@example.com"));

	@Benchmark
	public Pattern compile(Patterns patterns, Cursor cursor){
		return Pattern.compile(RegExpFunctions.covertXacmlToJavaSyntax(
				patterns.cases[cursor.next(patterns.cases.length)].pattern()));
	}

	@Benchmark
	public boolean match(Inputs inputs, Cursor cursor){
		int i = cursor.next(inputs.items.length);
		return inputs.compiled[i].matcher(inputs.items[i].input()).find();
	}

	@Benchmark
	public boolean call(Inputs inputs, Cursor cursor){
		Item item = inputs.items[cursor.next(inputs.items.length)];
		return RegExpFunctions.matches(item.pattern(), item.input());
	}

	@Benchmark
	public boolean callMiss(Misses misses, Cursor cursor){
		Item item = misses.items[cursor.next(misses.items.length)];
		return RegExpFunctions.matches(item.pattern(), item.input());
	}

	/** One set of patterns, each checked on its shortest input and on that input missed at its end */
	@State(Scope.Benchmark)
	public static class Patterns
	{
		@Param({"conformance", "adversarial"})
		public String patterns;

		Case[] cases;

		@Setup(Level.Trial)
		public void setUp(){
			cases = switch(patterns){
				case "conformance" -> CONFORMANCE.toArray(Case[]::new);
				case "adversarial" -> ADVERSARIAL.toArray(Case[]::new);
				default -> throw new IllegalArgumentException(
						"Unknown patterns \"" + patterns + "\"; known: conformance, adversarial");
			};
			for(Case c : cases){
				c.check(c.pattern());
			}
		}
	}

	/** Two inputs of one length for every pattern of the set: one that it finds at the end, and one that it misses */
	@State(Scope.Benchmark)
	public static class Inputs
	{
		@Param({"16", "256", "4096"})
		public int inputLength;

		Item[] items;
		Pattern[] compiled;

		@Setup(Level.Trial)
		public void setUp(Patterns patterns){
			List<Item> all = new ArrayList<>();
			for(Case c : patterns.cases){
				all.add(new Item(c, c.pattern(), c.found(inputLength), c.finds(inputLength)));
				all.add(new Item(c, c.pattern(), c.missed(inputLength), false));
			}
			items = all.toArray(Item[]::new);
			compiled = new Pattern[items.length];
			// The searches without a deadline run first: a cold quadratic search can outlast the deadline of a call
			for(int i = 0; i < items.length; i++){
				compiled[i] = Pattern.compile(RegExpFunctions.covertXacmlToJavaSyntax(items[i].pattern()));
				require(items[i], compiled[i].matcher(items[i].input()).find());
			}
			for(Item item : items){
				require(item, RegExpFunctions.matches(item.pattern(), item.input()));
			}
		}
	}

	/** The inputs in turn, each time with another variant of its pattern; every variant is checked on its case */
	@State(Scope.Benchmark)
	public static class Misses
	{
		Item[] items;

		@Setup(Level.Trial)
		public void setUp(Inputs inputs){
			int rounds = (MISSED_PATTERNS + inputs.items.length - 1) / inputs.items.length;
			items = new Item[rounds * inputs.items.length];
			for(int i = 0; i < items.length; i++){
				Item item = inputs.items[i % inputs.items.length];
				String variant = variant(item.pattern(), i);
				item.source().check(variant);
				items[i] = new Item(item.source(), variant, item.input(), item.finds());
			}
		}
	}

	/** The next operation of one benchmark thread */
	@State(Scope.Thread)
	public static class Cursor
	{
		private int next;

		int next(int length){
			int i = next;
			next = i + 1 < length ? i + 1 : 0;
			return i;
		}
	}

	/**
	 * A pattern, the character that its inputs repeat before the subject, and the subject: one of the
	 * shortest inputs that the pattern finds, and that it misses when the last character is {@code ~}
	 */
	record Case(String pattern, char filler, String subject)
	{
		/** The filler and then the subject; a subject longer than the input loses its start, and the pattern does not find it */
		String found(int length){
			return length < subject.length() ? subject.substring(subject.length() - length)
					: String.valueOf(filler).repeat(length - subject.length()) + subject;
		}

		boolean finds(int length){
			return subject.length() <= length;
		}

		/** The found input with {@code ~} as its last character */
		String missed(int length){
			return found(length).substring(0, length - 1) + '~';
		}

		/** Checks that a pattern finds the subject and misses it with {@code ~} as its last character */
		void check(String candidate){
			int length = subject.length();
			require(new Item(this, candidate, found(length), true), RegExpFunctions.matches(candidate, found(length)));
			require(new Item(this, candidate, missed(length), false), RegExpFunctions.matches(candidate, missed(length)));
		}
	}

	/** One call: a pattern of a case, an input, and whether the pattern finds the input */
	record Item(Case source, String pattern, String input, boolean finds) {}

	/** The pattern and a suffix that matches only the empty string, a literal repeated zero times; distinct for every number */
	static String variant(String pattern, int number){
		return pattern + (char)(VARIANTS + number) + "{0}";
	}

	private static void require(Item item, boolean found){
		if(found != item.finds()){
			throw new IllegalStateException("Pattern \"" + item.pattern() + "\" on an input of " + item.input().length()
					+ " characters gave " + found + ", expected " + item.finds());
		}
	}
}
