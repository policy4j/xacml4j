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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.xacml4j.jmh.harness.CorpusRequest;
import org.xacml4j.jmh.harness.PolicyCorpusProvider;
import org.xacml4j.jmh.testing.TestStores;
import org.xacml4j.jmh.testing.JmhParams;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;

/**
 * {@code PdpDecideBenchmark} outside a JMH run, through the surface of
 * design D13: request selection, the cursor (D8), and the thread check
 * (spec scenarios "Request selection" and "Several threads").
 */
public class PdpDecideBenchmarkTest
{
	private final List<PdpDecideBenchmark> setUp = new ArrayList<>();

	@BeforeEach
	public void resetStores()
	{
		TestStores.Counting.reset();
	}

	@AfterEach
	public void tearDownBenchmarks() throws Exception
	{
		for(PdpDecideBenchmark benchmark : setUp){
			benchmark.tearDown();
		}
	}

	@Test
	public void requestIdSelectsOnlyThatRequest() throws Exception
	{
		PdpDecideBenchmark benchmark = setUp("in-memory", "rsa2008", "01-01");

		assertThat(ids(benchmark.selected())).containsExactly("01-01");
	}

	@Test
	public void allSelectsEveryRequestInCorpusOrder() throws Exception
	{
		PdpDecideBenchmark benchmark = setUp("in-memory", "rsa2008", "all");

		assertThat(ids(benchmark.selected()))
				.containsExactlyElementsIn(ids(PolicyCorpusProvider.corpus("rsa2008").requests())).inOrder();
	}

	@Test
	public void unknownRequestIdFailsInSetUp()
	{
		PdpDecideBenchmark benchmark = configured("in-memory", "rsa2008", "99-99");

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, benchmark::setUp);

		assertThat(e).hasMessageThat().contains("99-99");
	}

	@Test
	public void setUpThatFailsReleasesEverySessionItOpened()
	{
		// JMH runs no trial teardown after a failed trial setup
		PdpDecideBenchmark benchmark = configured(TestStores.Counting.NAME, "rsa2008", "99-99");

		assertThrows(IllegalArgumentException.class, benchmark::setUp);

		assertThat(TestStores.Counting.closed()).containsExactlyElementsIn(TestStores.Counting.opened());
	}

	@Test
	public void unknownStoreFailsInSetUpWithTheKnownStores()
	{
		PdpDecideBenchmark benchmark = configured("missing", "rsa2008", "all");

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, benchmark::setUp);

		assertThat(e).hasMessageThat().contains("in-memory");
	}

	@ParameterizedTest(name = "thread {1} of {0}")
	@CsvSource({"1, 0", "2, 0", "2, 1", "3, 1", "3, 2", "4, 3"})
	public void cursorStartsAtTheShareOfItsThreadAndWrapsAround(int threads, int thread) throws Exception
	{
		PdpDecideBenchmark benchmark = setUp("in-memory", "rsa2008", "all");
		List<CorpusRequest> selected = benchmark.selected();
		int n = selected.size();
		List<Decision> mix = new ArrayList<>();
		for(CorpusRequest request : selected){
			mix.add(request.expected());
		}
		assertDecisionsTellEveryStartApart(mix);

		PdpDecideBenchmark.Cursor cursor = new PdpDecideBenchmark.Cursor();
		cursor.setUp(benchmark, JmhParams.thread(thread, threads));
		int start = thread * n / threads;
		List<Decision> expected = new ArrayList<>();
		List<Decision> actual = new ArrayList<>();
		for(int k = 0; k < 2 * n + 1; k++){
			expected.add(mix.get((start + k) % n));
			actual.add(decision(benchmark.decide(cursor)));
		}

		assertThat(actual).containsExactlyElementsIn(expected).inOrder();
	}

	@Test
	public void cursorOfOneSelectedRequestDecidesItOnEveryOperation() throws Exception
	{
		// XacmlResponse-01-02.xml: Deny
		PdpDecideBenchmark benchmark = setUp("in-memory", "rsa2008", "01-02");
		PdpDecideBenchmark.Cursor cursor = new PdpDecideBenchmark.Cursor();
		cursor.setUp(benchmark, JmhParams.thread(1, 2));

		for(int k = 0; k < 3; k++){
			assertThat(decision(benchmark.decide(cursor))).isEqualTo(Decision.DENY);
		}
	}

	@Test
	public void twoThreadsFailForAStoreThatAnswersOnlyOnItsOwnThread() throws Exception
	{
		PdpDecideBenchmark benchmark = setUp(TestStores.ThreadConfined.NAME, "rsa2008", "all");

		RuntimeException e = assertThrows(RuntimeException.class,
				() -> benchmark.checkThreads(JmhParams.benchmark(PdpDecideBenchmark.class.getName() + ".decide", 2)));

		assertThat(e).hasMessageThat().contains(TestStores.ThreadConfined.NAME);
		assertThat(e).hasMessageThat().containsMatch("(?i)concurrent");
	}

	@Test
	public void twoThreadsPassForTheInMemoryStoreAfterTheConcurrentCheck() throws Exception
	{
		PdpDecideBenchmark benchmark = setUp("in-memory", "rsa2008", "all");

		benchmark.checkThreads(JmhParams.benchmark(PdpDecideBenchmark.class.getName() + ".decide", 2));
	}

	@Test
	public void oneThreadPassesForAStoreThatAnswersOnlyOnItsOwnThread() throws Exception
	{
		PdpDecideBenchmark benchmark = setUp(TestStores.ThreadConfined.NAME, "rsa2008", "all");

		benchmark.checkThreads(JmhParams.benchmark(PdpDecideBenchmark.class.getName() + ".decide", 1));
	}

	private PdpDecideBenchmark setUp(String store, String corpus, String request) throws Exception
	{
		PdpDecideBenchmark benchmark = configured(store, corpus, request);
		benchmark.setUp();
		setUp.add(benchmark);
		return benchmark;
	}

	private static PdpDecideBenchmark configured(String store, String corpus, String request)
	{
		PdpDecideBenchmark benchmark = new PdpDecideBenchmark();
		benchmark.store = store;
		benchmark.corpus = corpus;
		benchmark.request = request;
		return benchmark;
	}

	/**
	 * The cursor test reads the position of the cursor from the decisions,
	 * so no two start positions may give the same sequence: no rotation of
	 * the decision mix but the full turn equals the mix.
	 */
	private static void assertDecisionsTellEveryStartApart(List<Decision> mix)
	{
		for(int r = 1; r < mix.size(); r++){
			List<Decision> rotated = new ArrayList<>(mix);
			Collections.rotate(rotated, -r);
			assertWithMessage("decision mix rotated by %s", r).that(rotated).isNotEqualTo(mix);
		}
	}

	private static Decision decision(ResponseContext response)
	{
		Collection<Result> results = response.getResults();
		assertThat(results).hasSize(1);
		return results.iterator().next().getDecision();
	}

	private static List<String> ids(List<CorpusRequest> requests)
	{
		List<String> ids = new ArrayList<>();
		for(CorpusRequest request : requests){
			ids.add(request.id());
		}
		return ids;
	}
}
