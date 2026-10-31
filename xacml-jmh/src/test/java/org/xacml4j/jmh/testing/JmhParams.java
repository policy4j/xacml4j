package org.xacml4j.jmh.testing;

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

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.BenchmarkParams;
import org.openjdk.jmh.infra.IterationParams;
import org.openjdk.jmh.infra.ThreadParams;
import org.openjdk.jmh.runner.IterationType;
import org.openjdk.jmh.runner.WorkloadParams;
import org.openjdk.jmh.runner.options.TimeValue;

/**
 * JMH infrastructure objects for calling setup methods outside a JMH run,
 * built as JMH 1.37 builds them for a benchmark without thread groups
 * ({@code BenchmarkHandler.distributeThreads}: each thread is its own group).
 */
public final class JmhParams
{
	private JmhParams(){
	}

	/**
	 * @param thread the thread index, from 0
	 * @param threads the number of benchmark threads
	 * @return the thread parameters JMH gives to that thread
	 */
	public static ThreadParams thread(int thread, int threads){
		return new ThreadParams(thread, threads, thread, threads, 0, 1, 0, 1, 0, 1);
	}

	/**
	 * @param benchmark the benchmark method name
	 * @param threads the number of benchmark threads
	 * @return benchmark parameters of a one-fork average-time run
	 */
	public static BenchmarkParams benchmark(String benchmark, int threads){
		TimeValue iteration = TimeValue.milliseconds(100);
		return new BenchmarkParams(benchmark, benchmark + "_jmhTest", false,
				threads, new int[]{1}, List.of(), 1, 0,
				new IterationParams(IterationType.WARMUP, 0, iteration, 1),
				new IterationParams(IterationType.MEASUREMENT, 1, iteration, 1),
				Mode.AverageTime, new WorkloadParams(), TimeUnit.MICROSECONDS, 1,
				"java", List.of(), Runtime.version().toString(), "test", "test", "1.37",
				TimeValue.minutes(1));
	}
}
