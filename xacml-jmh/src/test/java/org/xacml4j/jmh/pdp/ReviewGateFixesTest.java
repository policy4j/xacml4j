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

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xacml4j.jmh.harness.LookupQuery;
import org.xacml4j.jmh.harness.PdpFixture;
import org.xacml4j.jmh.harness.PolicyCorpus;
import org.xacml4j.jmh.testing.TestStores;
import org.xacml4j.jmh.testing.TestCorpora;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.Versionable;
import org.xacml4j.v30.spi.repository.PolicyRepository;

/**
 * The fixes of the review gate of task 3.7: rv004 and rv005 (Claude Fable
 * 5.1), rv006 (Codex GPT-6.1 Sol), and rv007 (Agy, Gemini 4 Argon).
 */
public class ReviewGateFixesTest
{
	@BeforeEach
	public void resetStores()
	{
		TestStores.ImportCounting.reset();
		TestStores.Drifting.reset();
		TestStores.CloseFailing.reset();
		TestStores.ImportFailing.reset();
	}

	/** rv006 finding 1: the oracle must not read {@code *} as one digit, as the engine does (policy4j-w842). */
	@Test
	public void wildcardReferenceExpectsTheHighestMatchingVersion() throws Exception
	{
		List<CompositeDecisionRule> rules = PdpFixture.parse(wildcardCorpus().policies());

		LookupQuery reference = referenceQuery(LookupQuery.of(rules));

		assertThat(((Versionable) reference.expected()).getVersion().getValue()).isEqualTo("1.10");
	}

	/** rv006 finding 1: the engine's wildcard defect now fails the lookup check instead of hiding in it. */
	@Test
	public void inMemoryStoreFailsTheWildcardLookupBecauseOfTheEngineDefect() throws Exception
	{
		IllegalStateException e = assertThrows(IllegalStateException.class,
				() -> PdpFixture.open("in-memory", wildcardCorpus()));

		assertThat(e).hasMessageThat().contains("reference Policy");
		assertThat(e).hasMessageThat().contains("version \"1.9\"");
		assertThat(e).hasMessageThat().contains("version \"1.10\"");
	}

	/** rv006 finding 3 and rv004: ids differ across JVM forks and stay usable as names. */
	@Test
	public void repositoryIdsCarryTheProcessAndUseOnlySafeCharacters(@TempDir Path dir) throws Exception
	{
		TestCorpora.writeDirectory(dir);

		try(PdpFixture fixture = PdpFixture.open("in-memory", "dir:" + dir)){
			String id = fixture.repository().getId();

			assertThat(id).contains("-" + Long.toHexString(ProcessHandle.current().pid()) + "-");
			assertThat(id).matches("[A-Za-z0-9._-]+");
			PolicyRepository other = fixture.newRepository();
			fixture.release(other);
			assertThat(other.getId()).matches("[A-Za-z0-9._-]+");
		}
	}

	/** rv006 finding 2: the concurrent check reads the store, not only the PDP's reference cache. */
	@Test
	public void concurrentCheckRunsTheLookupsOnTheStore() throws Exception
	{
		try(PdpFixture fixture = PdpFixture.open(TestStores.ThreadConfined.NAME, "rsa2008")){
			IllegalStateException e = assertThrows(IllegalStateException.class, () -> fixture.verifyConcurrent(2));

			assertThat(e).hasMessageThat().contains("concurrent");
			assertThat(e).hasMessageThat().contains("lookup");
		}
	}

	/** rv004 finding 1 and rv005 finding 1: one import tool, so one provider discovery, per operation. */
	@Test
	public void importXmlBuildsOneImportToolPerOperation() throws Exception
	{
		PolicyStoreLoadBenchmark benchmark = new PolicyStoreLoadBenchmark();
		benchmark.store = TestStores.ImportCounting.NAME;
		benchmark.corpus = "rsa2008";
		benchmark.setUp();
		try{
			TestStores.ImportCounting.reset();

			benchmark.importXml();

			assertThat(TestStores.ImportCounting.tools()).isEqualTo(1);
		}finally{
			benchmark.tearDown();
		}
	}

	/** rv007 finding 1: a store that drops policies added by the import tool fails setup, not the numbers. */
	@Test
	public void loadSetUpRejectsAStoreThatLosesImportedPolicies()
	{
		PolicyStoreLoadBenchmark benchmark = new PolicyStoreLoadBenchmark();
		benchmark.store = TestStores.ImportRefusing.NAME;
		benchmark.corpus = "rsa2008";

		IllegalStateException e = assertThrows(IllegalStateException.class, benchmark::setUp);

		assertThat(e).hasMessageThat().contains("importXml repository");
	}

	/** rv009: a checked exception in the load preflight still closes the fixture's own repository. */
	@Test
	public void loadSetUpClosesTheFixtureWhenAPreflightRepositoryThrowsACheckedException()
	{
		PolicyStoreLoadBenchmark benchmark = new PolicyStoreLoadBenchmark();
		benchmark.store = TestStores.CloseFailing.NAME;
		benchmark.corpus = "rsa2008";

		IllegalStateException e = assertThrows(IllegalStateException.class, benchmark::setUp);

		assertThat(e).hasCauseThat().hasMessageThat().isEqualTo("checked close failure");
		assertThat(TestStores.CloseFailing.open()).isEqualTo(0);
	}

	/**
	 * RVJ1: a checked exception from the import itself, not from a close, still closes the
	 * import repository and the fixture's own repository, and is the exception that setup throws.
	 */
	@Test
	public void loadSetUpClosesEveryRepositoryWhenTheImportThrowsACheckedException()
	{
		PolicyStoreLoadBenchmark benchmark = new PolicyStoreLoadBenchmark();
		benchmark.store = TestStores.ImportFailing.NAME;
		benchmark.corpus = "rsa2008";

		IOException e = assertThrows(IOException.class, benchmark::setUp);

		assertThat(e).hasMessageThat().isEqualTo(TestStores.ImportFailing.MESSAGE);
		assertThat(TestStores.ImportFailing.opened()).hasSize(3);
		assertThat(TestStores.ImportFailing.closed()).containsExactlyElementsIn(TestStores.ImportFailing.opened());
	}

	/** rv005 finding 4: a store that changes during the trial fails the run at tear-down. */
	@Test
	public void tearDownFailsWhenTheStoreDriftedDuringTheTrial() throws Exception
	{
		PdpDecideBenchmark benchmark = new PdpDecideBenchmark();
		benchmark.store = TestStores.Drifting.NAME;
		benchmark.corpus = "rsa2008";
		benchmark.request = "all";
		benchmark.setUp();

		TestStores.Drifting.drift();
		IllegalStateException e = assertThrows(IllegalStateException.class, benchmark::tearDown);

		assertThat(e).hasMessageThat().contains("lookup");
	}

	/** rv009 minor: a failing close keeps the drift diagnostic as the primary failure. */
	@Test
	public void tearDownKeepsTheCheckFailureWhenCloseAlsoFails() throws Exception
	{
		PdpDecideBenchmark benchmark = new PdpDecideBenchmark();
		benchmark.store = TestStores.Drifting.NAME;
		benchmark.corpus = "rsa2008";
		benchmark.request = "all";
		benchmark.setUp();

		TestStores.Drifting.drift();
		TestStores.Drifting.failClose();
		IllegalStateException e = assertThrows(IllegalStateException.class, benchmark::tearDown);

		assertThat(e).hasMessageThat().contains("lookup");
		assertThat(e.getSuppressed()).hasLength(1);
		assertThat(e.getSuppressed()[0]).hasMessageThat().contains("Cannot close the repository");
		assertThat(e.getSuppressed()[0]).hasCauseThat().hasMessageThat().isEqualTo("checked close failure");
	}

	private static PolicyCorpus wildcardCorpus() throws Exception
	{
		return new PolicyCorpus("wildcard",
				List.of(TestCorpora.source("root.xml", TestCorpora.rootPolicySet(TestCorpora.VERSIONS_ROOT_ID,
								TestCorpora.policyReference(TestCorpora.VERSIONED_ID, "1.*"))),
						TestCorpora.source("versioned-1.9.xml", TestCorpora.readPolicy(TestCorpora.VERSIONED_ID, "1.9", "nine")),
						TestCorpora.source("versioned-1.10.xml", TestCorpora.readPolicy(TestCorpora.VERSIONED_ID, "1.10", "ten"))),
				"root.xml",
				List.of(TestCorpora.request("read", TestCorpora.actionRequest("read"), Decision.PERMIT)));
	}

	private static LookupQuery referenceQuery(List<LookupQuery> queries)
	{
		for(LookupQuery query : queries){
			if(query.kind() == LookupQuery.Kind.POLICY_REFERENCE){
				return query;
			}
		}
		throw new AssertionError("no reference query in " + queries);
	}
}
