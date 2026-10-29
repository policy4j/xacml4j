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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.xacml4j.v30.policy.function.impl.RegExpFunctions;

/**
 * The regex function benchmark of change add-regex-engine-spi (task 2.2): every operation gives the
 * result that its input was built for, {@code compile} compiles every pattern of the set in turn, and
 * the patterns of {@code call} and {@code callMiss} are what a pattern cache of 256 would find and
 * miss.
 */
public class RegexBenchmarkTest
{
	@ParameterizedTest(name = "{0}, {1} characters")
	@CsvSource({"conformance, 16", "conformance, 256", "conformance, 4096", "adversarial, 16", "adversarial, 256",
			"adversarial, 4096"})
	public void everyOperationGivesTheResultItsInputWasBuiltFor(String set, int length)
	{
		RegexFunctionBenchmark.Inputs inputs = inputs(set, length);
		RegexFunctionBenchmark.Misses misses = misses(inputs);
		RegexFunctionBenchmark benchmark = new RegexFunctionBenchmark();
		RegexFunctionBenchmark.Cursor matchCursor = new RegexFunctionBenchmark.Cursor();
		RegexFunctionBenchmark.Cursor callCursor = new RegexFunctionBenchmark.Cursor();
		RegexFunctionBenchmark.Cursor missCursor = new RegexFunctionBenchmark.Cursor();
		List<Boolean> expected = new ArrayList<>();
		List<List<Boolean>> actual = List.of(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

		for(RegexFunctionBenchmark.Item item : inputs.items){
			expected.add(item.finds());
			actual.get(0).add(benchmark.match(inputs, matchCursor));
			actual.get(1).add(benchmark.call(inputs, callCursor));
			actual.get(2).add(benchmark.callMiss(misses, missCursor));
		}

		assertThat(actual).containsExactly(expected, expected, expected);
	}

	@ParameterizedTest
	@CsvSource({"conformance", "adversarial"})
	public void compileCompilesEveryPatternOfTheSetInTurn(String set)
	{
		RegexFunctionBenchmark.Patterns patterns = patterns(set);
		RegexFunctionBenchmark benchmark = new RegexFunctionBenchmark();
		RegexFunctionBenchmark.Cursor cursor = new RegexFunctionBenchmark.Cursor();
		List<String> compiled = new ArrayList<>();

		for(int i = 0; i < 2 * patterns.cases.length; i++){
			compiled.add(benchmark.compile(patterns, cursor).pattern());
		}

		List<String> translated = Arrays.stream(patterns.cases)
				.map(c -> RegExpFunctions.covertXacmlToJavaSyntax(c.pattern())).toList();
		assertThat(compiled.subList(0, translated.size())).containsExactlyElementsIn(translated).inOrder();
		assertThat(compiled.subList(translated.size(), compiled.size())).containsExactlyElementsIn(translated).inOrder();
	}

	@ParameterizedTest
	@CsvSource({"conformance, 12", "adversarial, 3"})
	public void callRepeatsTheFewPatternsOfTheSetAndCallMissNoneWithinFourTimesACacheOf256(String set, long patterns)
	{
		RegexFunctionBenchmark.Inputs inputs = inputs(set, 16);
		RegexFunctionBenchmark.Misses misses = misses(inputs);

		assertThat(Arrays.stream(inputs.items).map(RegexFunctionBenchmark.Item::pattern).distinct().count())
				.isEqualTo(patterns);
		assertThat(misses.items.length).isAtLeast(4 * 256);
		assertThat(Arrays.stream(misses.items).map(RegexFunctionBenchmark.Item::pattern).distinct().count())
				.isEqualTo((long)misses.items.length);
	}

	@Test
	public void aPatternThatGivesAnotherResultFailsSetUp()
	{
		RegexFunctionBenchmark.Case wrong = new RegexFunctionBenchmark.Case("read|write", '~', "reed");

		IllegalStateException e = assertThrows(IllegalStateException.class, () -> wrong.check(wrong.pattern()));

		assertThat(e).hasMessageThat().contains("read|write");
	}

	private static RegexFunctionBenchmark.Patterns patterns(String set)
	{
		RegexFunctionBenchmark.Patterns patterns = new RegexFunctionBenchmark.Patterns();
		patterns.patterns = set;
		patterns.setUp();
		return patterns;
	}

	private static RegexFunctionBenchmark.Inputs inputs(String set, int length)
	{
		RegexFunctionBenchmark.Inputs inputs = new RegexFunctionBenchmark.Inputs();
		inputs.inputLength = length;
		inputs.setUp(patterns(set));
		return inputs;
	}

	private static RegexFunctionBenchmark.Misses misses(RegexFunctionBenchmark.Inputs inputs)
	{
		RegexFunctionBenchmark.Misses misses = new RegexFunctionBenchmark.Misses();
		misses.setUp(inputs);
		return misses;
	}
}
