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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.VerboseMode;
import org.xacml4j.jmh.testing.CountingStore;
import org.xacml4j.jmh.testing.ReadOnlyStore;

/**
 * Spec scenario "Load operations": every operation of the load benchmark
 * opens and closes one session. The run is in process ({@code forks(0)}),
 * so the static record of {@link CountingStore} sees it; like
 * {@code BenchmarkSmokeTest}, it takes the JMH lock in {@code java.io.tmpdir}.
 */
public class PolicyStoreLoadBenchmarkTest
{
	private static final int OPERATIONS = 50;

	@BeforeEach
	public void resetStores()
	{
		CountingStore.reset();
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = {"loadParsed", "importXml"})
	public void fiftyLoadsCloseEverySessionTheyOpenAndNoRepositoryIdComesTwice(String method) throws Exception
	{
		// single-shot mode with a batch of 50 makes one measured operation of exactly 50 calls
		Options options = new OptionsBuilder()
				.include("^" + Pattern.quote(PolicyStoreLoadBenchmark.class.getName() + "." + method) + "$")
				.param("store", CountingStore.NAME)
				.param("corpus", "rsa2008")
				.mode(Mode.SingleShotTime)
				.forks(0)
				.threads(1)
				.warmupIterations(0)
				.measurementIterations(1)
				.measurementBatchSize(OPERATIONS)
				.shouldFailOnError(true)
				.verbosity(VerboseMode.SILENT)
				.build();

		Collection<RunResult> results = new Runner(options).run();

		assertThat(results).hasSize(1);
		List<String> opened = CountingStore.opened();
		assertThat(opened.size()).isAtLeast(OPERATIONS);
		assertThat(opened).containsNoDuplicates();
		assertThat(CountingStore.closed()).containsExactlyElementsIn(opened);
	}

	@Test
	public void storeWithoutLoadFailsInSetUp()
	{
		PolicyStoreLoadBenchmark benchmark = new PolicyStoreLoadBenchmark();
		benchmark.store = ReadOnlyStore.NAME;
		benchmark.corpus = "rsa2008";

		RuntimeException e = assertThrows(RuntimeException.class, benchmark::setUp);

		assertThat(e).hasMessageThat().contains(ReadOnlyStore.NAME);
		assertThat(e).hasMessageThat().containsMatch("(?i)load");
	}
}
