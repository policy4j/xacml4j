package org.xacml4j.v30.spi.pip;

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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Runs the attribute functions of one call of a parallel resolver (design D26). A task takes a
 * permit of the information point before it starts and gives it back when it ends, so a task that
 * ignores its cancellation keeps its permit until it really ends. The calling thread waits for the
 * tasks up to the time limit, cancels the others, and returns at once: it never waits for a
 * cancelled task to end. On JDK 28 the body of {@link #runAll} becomes a
 * {@code StructuredTaskScope}; nothing else changes.
 */
final class StructuredResolverRunner
{
	/** Runs the tasks one after another on the calling thread, without a time limit */
	static final StructuredResolverRunner DIRECT =
			new StructuredResolverRunner(Runnable::run, Integer.MAX_VALUE, Optional.empty());

	private final Executor executor;
	private final Semaphore permits;
	private final Optional<Duration> timeout;

	StructuredResolverRunner(Executor executor, int maxConcurrent, Optional<Duration> timeout){
		this.executor = Objects.requireNonNull(executor, "executor");
		this.permits = new Semaphore(maxConcurrent);
		this.timeout = Objects.requireNonNull(timeout, "timeout");
	}

	/**
	 * Runs the given tasks and returns their futures, in the order of the tasks. Every future is
	 * done when this method returns: a task that did not end in time is cancelled.
	 *
	 * @param tasks the tasks to run
	 * @return the futures of the tasks
	 */
	<T> List<Future<T>> runAll(List<? extends Callable<T>> tasks){
		List<Future<T>> futures = new ArrayList<>(tasks.size());
		for(Callable<T> task : tasks){
			FutureTask<T> future = new FutureTask<>(()->{
				// A free permit is taken without a wait, so the direct runner keeps the interrupt of its caller
				if(!permits.tryAcquire()){
					permits.acquire();
				}
				try{
					return task.call();
				}finally{
					permits.release();
				}
			});
			try{
				executor.execute(future);
				futures.add(future);
			}catch(RejectedExecutionException e){
				futures.add(CompletableFuture.failedFuture(e));
			}
		}
		long deadline = System.nanoTime() + timeout.map(Duration::toNanos).orElse(0L);
		for(Future<T> future : futures){
			try{
				if(timeout.isPresent()){
					future.get(deadline - System.nanoTime(), TimeUnit.NANOSECONDS);
				}else{
					future.get();
				}
			}catch(ExecutionException | CancellationException e){
				// The task failed or was not run: its future tells which
			}catch(TimeoutException e){
				break;
			}catch(InterruptedException e){
				Thread.currentThread().interrupt();
				break;
			}
		}
		for(Future<T> future : futures){
			future.cancel(true);
		}
		return futures;
	}
}
