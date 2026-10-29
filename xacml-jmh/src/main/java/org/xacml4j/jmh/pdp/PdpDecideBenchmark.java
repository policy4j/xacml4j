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

import java.util.List;
import java.util.concurrent.TimeUnit;

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
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.ThreadParams;
import org.xacml4j.jmh.corpus.PipCorpus;
import org.xacml4j.jmh.corpus.PipWorkload;
import org.xacml4j.jmh.harness.CorpusRequest;
import org.xacml4j.jmh.harness.PdpFixture;
import org.xacml4j.v30.ResponseContext;

/** Measures one decision of a request from a checked corpus. */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms2g", "-Xmx2g", "-XX:+UseG1GC"})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
public class PdpDecideBenchmark
{
	@Param({"in-memory", "snapshot"})
	public String store;

	@Param({"rsa2008", "synthetic:100"})
	public String corpus;

	@Param({"all"})
	public String request;

	@Param({"false"})
	public boolean targetIndex;

	@Param({"0"})
	public long pipLatencyMicros;

	@Param({"serial"})
	public String pipMode;

	@Param({"off"})
	public String pipCache;

	@Param({"32"})
	public int pipMaxInFlight;

	private PdpFixture fixture;
	private PipWorkload workload;
	private List<CorpusRequest> selected = List.of();

	@Setup(Level.Trial)
	public void setUp() throws Exception {
		if(fixture != null){
			return;
		}
		PipWorkload configured = PipCorpus.isPip(corpus)
				? PipWorkload.open(PipCorpus.count(corpus), pipLatencyMicros, pipMode, pipCache, pipMaxInFlight) : null;
		PdpFixture opened;
		try{
			opened = configured == null ? PdpFixture.open(store, corpus, targetIndex)
					: PdpFixture.open(store, corpus, configured.pip(), targetIndex);
		}catch(Exception | Error failure){
			closeOnFailure(configured, failure);
			throw failure;
		}
		try{
			selected = opened.requests(request);
			if(configured != null) configured.verifyCache(opened);
			fixture = opened;
			workload = configured;
		}catch(RuntimeException | Error failure){
			closeOnFailure(opened, failure);
			closeOnFailure(configured, failure);
			throw failure;
		}
	}

	@Setup(Level.Trial)
	public void checkThreads(BenchmarkParams params){
		// JMH may invoke same-level setup methods in either order.
		if(fixture == null){
			try{
				setUp();
			}catch(RuntimeException | Error failure){
				throw failure;
			}catch(Exception failure){
				throw new IllegalStateException("Cannot open fixture for store " + store + " and corpus " + corpus, failure);
			}
		}
		try{
			int threads = params.getThreads();
			if(threads > 1){
				fixture.verifyConcurrent(threads);
			}
		}catch(RuntimeException | Error failure){
			PdpFixture opened = fixture;
			fixture = null;
			closeOnFailure(opened, failure);
			if(workload != null){
				closeOnFailure(workload, failure);
				workload = null;
			}
			throw failure;
		}
	}

	/** @return the requests selected during trial setup */
	public List<CorpusRequest> selected(){
		return selected;
	}

	@Benchmark
	public ResponseContext decide(Cursor cursor){
		return fixture.pdp().decide(cursor.next().request());
	}

	@TearDown(Level.Trial)
	public void tearDown(){
		if(fixture != null){
			PdpFixture opened = fixture;
			PipWorkload configured = workload;
			fixture = null;
			workload = null;
			// A store or corpus that changed during the trial fails the run (review rv005); a
			// close failure is added as suppressed and keeps the check's diagnostic (review rv009).
			try(configured; opened){
				opened.recheck();
			}
		}
	}

	private static void closeOnFailure(PdpFixture opened, Throwable failure){
		try{
			opened.close();
		}catch(RuntimeException | Error closeFailure){
			failure.addSuppressed(closeFailure);
		}
	}

	private static void closeOnFailure(PipWorkload workload, Throwable failure){
		if(workload == null) return;
		try{
			workload.close();
		}catch(RuntimeException | Error closeFailure){
			failure.addSuppressed(closeFailure);
		}
	}

	/** A per-thread round-robin cursor over the selected requests. */
	@State(Scope.Thread)
	public static class Cursor
	{
		private CorpusRequest[] items;
		private int index;

		@Setup(Level.Trial)
		public void setUp(PdpDecideBenchmark benchmark, ThreadParams thread){
			items = benchmark.selected().toArray(CorpusRequest[]::new);
			index = (int)((long)thread.getThreadIndex() * items.length / thread.getThreadCount());
		}

		public CorpusRequest next(){
			CorpusRequest item = items[index];
			if(++index == items.length){
				index = 0;
			}
			return item;
		}
	}
}
