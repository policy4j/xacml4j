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
import org.xacml4j.jmh.harness.LookupQuery;
import org.xacml4j.jmh.harness.PdpFixture;
import org.xacml4j.jmh.harness.StoreCapability;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.spi.repository.PolicyRepository;

/** Measures checked corpus lookups and the cursor cost separately. */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms2g", "-Xmx2g", "-XX:+UseG1GC"})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
public class PolicyStoreLookupBenchmark
{
	@Param({"in-memory", "snapshot"})
	public String store;

	@Param({"rsa2008", "synthetic:100"})
	public String corpus;

	private PdpFixture fixture;

	@Setup(Level.Trial)
	public void setUp() throws Exception {
		if(fixture == null){
			fixture = PdpFixture.open(store, corpus);
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
				if(!fixture.store().capabilities().contains(StoreCapability.CONCURRENT_READS)){
					throw new IllegalStateException("Store " + store + " does not support concurrent reads for " + threads + " threads");
				}
				fixture.verifyConcurrent(threads);
			}
		}catch(RuntimeException | Error failure){
			PdpFixture opened = fixture;
			fixture = null;
			try{
				opened.close();
			}catch(RuntimeException | Error closeFailure){
				failure.addSuppressed(closeFailure);
			}
			throw failure;
		}
	}

	/**
	 * One store call, dispatched on the query kind. It returns the store's
	 * answer as it is (the repository contract allows {@code null}), so the
	 * operation allocates nothing of its own that the {@code cursor} control
	 * lacks (review rv004).
	 */
	@Benchmark
	public CompositeDecisionRule lookup(Cursor cursor){
		LookupQuery query = cursor.next();
		PolicyRepository repository = fixture.repository();
		return switch(query.kind()){
			case EXACT -> repository.get(query.id(), query.exact().get());
			case POLICY_REFERENCE -> repository.getPolicy(query.id(), query.version().orElse(null),
					query.earliest().orElse(null), query.latest().orElse(null));
			case POLICY_SET_REFERENCE -> repository.getPolicySet(query.id(), query.version().orElse(null),
					query.earliest().orElse(null), query.latest().orElse(null));
		};
	}

	@Benchmark
	public LookupQuery cursor(Cursor cursor){
		return cursor.next();
	}

	@TearDown(Level.Trial)
	public void tearDown(){
		if(fixture != null){
			PdpFixture opened = fixture;
			fixture = null;
			// A store or corpus that changed during the trial fails the run (review rv005); a
			// close failure is added as suppressed and keeps the check's diagnostic (review rv009).
			try(opened){
				opened.recheck();
			}
		}
	}

	/** A per-thread round-robin cursor over corpus lookups. */
	@State(Scope.Thread)
	public static class Cursor
	{
		private LookupQuery[] items;
		private int index;

		@Setup(Level.Trial)
		public void setUp(PolicyStoreLookupBenchmark benchmark, ThreadParams thread){
			items = benchmark.fixture.lookups().toArray(LookupQuery[]::new);
			index = (int)((long)thread.getThreadIndex() * items.length / thread.getThreadCount());
		}

		public LookupQuery next(){
			LookupQuery item = items[index];
			if(++index == items.length){
				index = 0;
			}
			return item;
		}
	}
}
