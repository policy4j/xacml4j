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
import org.xacml4j.jmh.harness.PdpFixture;
import org.xacml4j.jmh.harness.PolicySource;
import org.xacml4j.v30.spi.repository.PolicyImportTool;
import org.xacml4j.v30.spi.repository.PolicyRepository;

/** Measures one complete store repository (open, load or import, close) for a corpus. */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Fork(value = 3, jvmArgsAppend = {"-Xms2g", "-Xmx2g", "-XX:+UseG1GC"})
// Two-second iterations: one import of a large corpus takes tens of milliseconds (review rv005).
@Warmup(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
public class PolicyStoreLoadBenchmark
{
	@Param({"in-memory", "snapshot"})
	public String store;

	@Param({"rsa2008", "synthetic:100"})
	public String corpus;

	private PdpFixture fixture;

	@Setup(Level.Trial)
	public void setUp() throws Exception {
		if(fixture != null){
			return;
		}
		PdpFixture opened = PdpFixture.open(store, corpus);
		try{
			// Both measured paths once, checked, before any measurement: importXml adds through
			// the production import tool, which ignores a rejected add (review rv007).
			PolicyRepository loaded = opened.newRepository();
			try(AutoCloseable release = () -> opened.release(loaded)){
				PdpFixture.load(loaded, opened.policies());
				opened.verifyLookups(loaded, "a loadParsed repository");
			}
			PolicyRepository imported = opened.newRepository();
			try(AutoCloseable release = () -> opened.release(imported)){
				importAll(opened, imported);
				opened.verifyLookups(imported, "an importXml repository");
			}
			fixture = opened;
		}catch(Throwable failure){
			// Throwable: importAll and AutoCloseable.close throw checked exceptions too (review rv009).
			closeOnFailure(opened, failure);
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
			throw failure;
		}
	}

	@Benchmark
	public PolicyRepository loadParsed() throws Exception {
		PolicyRepository repository = fixture.newRepository();
		try(AutoCloseable release = () -> fixture.release(repository)){
			PdpFixture.load(repository, fixture.policies());
			return repository;
		}
	}

	@Benchmark
	public PolicyRepository importXml() throws Exception {
		PolicyRepository repository = fixture.newRepository();
		try(AutoCloseable release = () -> fixture.release(repository)){
			importAll(fixture, repository);
			return repository;
		}
	}

	/**
	 * Imports every corpus document through one production import tool, so
	 * its provider discovery counts once per repository, not once per document
	 * (review rv004).
	 */
	private static void importAll(PdpFixture fixture, PolicyRepository repository) throws Exception {
		PolicyImportTool tool = repository.newImportTool();
		for(PolicySource source : fixture.corpus().policies()){
			tool.importPolicy(source.mediaType(), source::open);
		}
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

	private static void closeOnFailure(PdpFixture opened, Throwable failure){
		try{
			opened.close();
		}catch(RuntimeException | Error closeFailure){
			failure.addSuppressed(closeFailure);
		}
	}
}
