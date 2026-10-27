package org.xacml4j.jmh.parse;

/*
 * #%L
 * Xacml4J JMH Benchmarks
 * %%
 * Copyright (C) 2009 - 2026 Xacml4J.org
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
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.xacml4j.jmh.harness.PolicyCorpusProvider;
import org.xacml4j.jmh.harness.PolicyCorpus;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.RequestContext;

/**
 * The parse benchmarks of change improve-xacml-parsing (spec parse-benchmarks): every input is
 * parsed and compared in setup, the operations return what the corpus holds, and a short run
 * of every method and default parameter set ends with a finite score.
 */
public class ParseBenchmarkTest
{
	@ParameterizedTest
	@ValueSource(strings = {"rsa2008", "synthetic:100"})
	public void policySetUpChecksEveryDocumentAndTheOperationCyclesThroughThem(String corpus) throws Exception
	{
		PolicyParseBenchmark benchmark = new PolicyParseBenchmark();
		benchmark.corpus = corpus;
		benchmark.setUp();
		PolicyCorpus loaded = PolicyCorpusProvider.corpus(corpus);
		PolicyParseBenchmark.Cursor cursor = new PolicyParseBenchmark.Cursor();

		List<CompositeDecisionRule> parsed = new ArrayList<>();
		for(int i = 0; i < loaded.policies().size() + 1; i++){
			parsed.add(benchmark.parse(cursor));
		}

		assertThat(parsed.get(loaded.policies().size())).isEqualTo(parsed.get(0));
		assertThat(parsed.subList(0, loaded.policies().size())).isEqualTo(benchmark.expected());
	}

	@ParameterizedTest
	@CsvSource({"rsa2008, xml30", "rsa2008, json", "synthetic:10, xml30", "synthetic:10, json"})
	public void requestSetUpChecksEveryDocumentAndTheOperationReturnsTheCorpusRequests(String corpus, String format)
			throws Exception
	{
		RequestParseBenchmark benchmark = new RequestParseBenchmark();
		benchmark.corpus = corpus;
		benchmark.format = format;
		benchmark.setUp();
		PolicyCorpus loaded = PolicyCorpusProvider.corpus(corpus);
		RequestParseBenchmark.Cursor cursor = new RequestParseBenchmark.Cursor();

		for(int i = 0; i < loaded.requests().size(); i++){
			RequestContext parsed = benchmark.parse(cursor);
			assertThat(parsed).isEqualTo(loaded.requests().get(i).request());
		}
	}

	@Test
	public void unknownRequestFormatFailsSetUp()
	{
		RequestParseBenchmark benchmark = new RequestParseBenchmark();
		benchmark.corpus = "rsa2008";
		benchmark.format = "yaml";

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, benchmark::setUp);

		assertThat(e).hasMessageThat().contains("yaml");
	}

	@Test
	public void checkFailsOnTheFirstDocumentThatDiffers()
	{
		IllegalStateException e = assertThrows(IllegalStateException.class,
				() -> ParseChecks.requireSame("policy", List.of("a.xml", "b.xml"), List.of("A", "B"), List.of("A", "C")));

		assertThat(e).hasMessageThat().contains("b.xml");
	}

	@Test
	public void shortRunOfEveryParseMethodAndDefaultParameterSetEndsWithAFiniteScore() throws Exception
	{
		Collection<RunResult> results = new Runner(new OptionsBuilder()
				.include("^" + java.util.regex.Pattern.quote(PolicyParseBenchmark.class.getName()) + "\\.")
				.include("^" + java.util.regex.Pattern.quote(RequestParseBenchmark.class.getName()) + "\\.")
				.forks(0)
				.warmupIterations(0)
				.measurementIterations(1)
				.measurementTime(TimeValue.milliseconds(100))
				.shouldFailOnError(true)
				.build()).run();

		TreeSet<String> keys = new TreeSet<>();
		for(RunResult result : results){
			assertThat(Double.isFinite(result.getPrimaryResult().getScore())).isTrue();
			keys.add(result.getParams().getBenchmark() + " " + result.getParams().getParam("corpus")
					+ (result.getParams().getParamsKeys().contains("format") ? " " + result.getParams().getParam("format") : ""));
		}
		String policy = PolicyParseBenchmark.class.getName() + ".parse";
		String request = RequestParseBenchmark.class.getName() + ".parse";
		assertThat(keys).containsExactly(
				policy + " rsa2008", policy + " synthetic:100",
				request + " rsa2008 json", request + " rsa2008 xml30",
				request + " synthetic:100 json", request + " synthetic:100 xml30");
	}
}
