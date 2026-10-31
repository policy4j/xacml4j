package org.xacml4j.jmh.pdp;

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
import static com.google.common.truth.Truth.assertWithMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.Result;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.openjdk.jmh.runner.options.VerboseMode;
import org.xacml4j.jmh.regex.RegexFunctionBenchmark;

/**
 * Spec requirement "Benchmark set": every benchmark method with every
 * default parameter set runs in process with fail-on-error and gives a
 * finite score, and the GC profiler reports allocation per operation. A
 * functional test only: the runs are too short to be timing evidence. It
 * takes the JMH lock in {@code java.io.tmpdir}, so it never runs during a
 * recorded benchmark run.
 */
public class BenchmarkSmokeTest
{
	/** The benchmark methods of design D7. */
	private static final Map<String, List<String>> METHODS = Map.of(
			PdpDecideBenchmark.class.getName(), List.of("decide"),
			PolicyStoreLookupBenchmark.class.getName(), List.of("lookup", "cursor"),
			PolicyStoreLoadBenchmark.class.getName(), List.of("loadParsed", "importXml"));

	/** The default parameters of design D8. */
	private static final List<String> STORES = List.of("in-memory", "snapshot");
	private static final List<String> CORPORA = List.of("rsa2008", "synthetic:100");

	@Test
	public void everyBenchmarkMethodAndDefaultParameterSetEndsWithAFiniteScore() throws Exception
	{
		ChainedOptionsBuilder options = shortRun();
		for(String type : METHODS.keySet()){
			options.include("^" + Pattern.quote(type) + "\\.");
		}

		Collection<RunResult> results = new Runner(options.build()).run();

		SortedSet<String> actual = new TreeSet<>();
		for(RunResult result : results){
			String key = key(result.getParams());
			double score = result.getPrimaryResult().getScore();
			assertWithMessage("score of %s", key).that(Double.isFinite(score)).isTrue();
			actual.add(key);
		}
		assertThat(actual).containsExactlyElementsIn(expectedKeys());
	}

	@Test
	public void gcProfilerReportsAllocationPerDecision() throws Exception
	{
		ChainedOptionsBuilder options = shortRun()
				.include("^" + Pattern.quote(PdpDecideBenchmark.class.getName() + ".decide") + "$")
				.param("store", "in-memory")
				.param("corpus", "rsa2008")
				.param("request", "all")
				.addProfiler(GCProfiler.class);

		Collection<RunResult> results = new Runner(options.build()).run();

		assertThat(results).hasSize(1);
		Result<?> allocation = results.iterator().next().getSecondaryResults().get("gc.alloc.rate.norm");
		assertWithMessage("secondary metric gc.alloc.rate.norm").that(allocation).isNotNull();
		assertThat(allocation.getScoreUnit()).isEqualTo("B/op");
		assertThat(Double.isFinite(allocation.getScore())).isTrue();
		assertThat(allocation.getScore()).isGreaterThan(0.0);
	}

	/**
	 * The rules corpus is not a default parameter, so that the default runs stay comparable; it
	 * runs every case with and without the target index.
	 */
	@Test
	public void rulesCorpusRunsWithAndWithoutTheTargetIndex() throws Exception
	{
		ChainedOptionsBuilder options = shortRun()
				.include("^" + Pattern.quote(PdpDecideBenchmark.class.getName() + ".decide") + "$")
				.param("store", "in-memory")
				.param("corpus", "rules:8")
				.param("request", "all")
				.param("targetIndex", "false", "true");

		Collection<RunResult> results = new Runner(options.build()).run();

		assertThat(results).hasSize(2);
		for(RunResult result : results){
			assertWithMessage("score of %s", key(result.getParams()))
					.that(Double.isFinite(result.getPrimaryResult().getScore())).isTrue();
		}
	}

	/**
	 * The regex function benchmark of change add-regex-engine-spi has parameters of its own:
	 * {@code compile} runs for every pattern set, the other methods for every set and input length.
	 */
	@Test
	public void regexFunctionBenchmarkRunsEveryMethodWithEveryDefaultParameterSet() throws Exception
	{
		String type = RegexFunctionBenchmark.class.getName();

		Collection<RunResult> results = new Runner(shortRun().include("^" + Pattern.quote(type) + "\\.").build()).run();

		SortedSet<String> actual = new TreeSet<>();
		for(RunResult result : results){
			String key = key(result.getParams());
			assertWithMessage("score of %s", key).that(Double.isFinite(result.getPrimaryResult().getScore())).isTrue();
			actual.add(key);
		}
		SortedSet<String> expected = new TreeSet<>();
		for(String set : List.of("conformance", "adversarial")){
			expected.add(type + ".compile patterns=" + set);
			for(String method : List.of("match", "call", "callMiss")){
				for(String length : List.of("16", "256", "4096")){
					expected.add(type + "." + method + " inputLength=" + length + " patterns=" + set);
				}
			}
		}
		assertThat(actual).containsExactlyElementsIn(expected);
	}

	/** One 100 ms iteration in this JVM, no warmup, failing on the first benchmark error. */
	private static ChainedOptionsBuilder shortRun()
	{
		return new OptionsBuilder()
				.forks(0)
				.threads(1)
				.warmupIterations(0)
				.measurementIterations(1)
				.measurementTime(TimeValue.milliseconds(100))
				.shouldFailOnError(true)
				.verbosity(VerboseMode.SILENT);
	}

	private static SortedSet<String> expectedKeys()
	{
		SortedSet<String> keys = new TreeSet<>();
		for(Map.Entry<String, List<String>> type : METHODS.entrySet()){
			for(String method : type.getValue()){
				for(String store : STORES){
					for(String corpus : CORPORA){
						String params = "corpus=" + corpus
								+ (type.getKey().equals(PdpDecideBenchmark.class.getName())
										? " pipCache=off pipLatencyMicros=0 pipMaxInFlight=32 pipMode=serial request=all" : "")
								+ " store=" + store
								+ (type.getKey().equals(PdpDecideBenchmark.class.getName()) ? " targetIndex=false" : "");
						keys.add(type.getKey() + "." + method + " " + params);
					}
				}
			}
		}
		return keys;
	}

	/** @return the benchmark method and its parameters, sorted by name */
	private static String key(BenchmarkParams params)
	{
		List<String> pairs = new ArrayList<>();
		for(String name : new TreeSet<>(params.getParamsKeys())){
			pairs.add(name + "=" + params.getParam(name));
		}
		return params.getBenchmark() + " " + String.join(" ", pairs);
	}
}
