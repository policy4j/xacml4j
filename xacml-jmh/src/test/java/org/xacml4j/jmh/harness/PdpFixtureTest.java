package org.xacml4j.jmh.harness;

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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.xacml4j.jmh.testing.TestStores;
import org.xacml4j.jmh.testing.FailureLines;
import org.xacml4j.jmh.testing.TestCorpora;
import org.xacml4j.jmh.testing.TestPdps;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.policy.PolicySetIDReference;
import org.xacml4j.v30.policy.PolicyVisitorSupport;
import org.xacml4j.v30.spi.repository.PolicyRepository;

/**
 * Spec requirements "Decisions and lookups are checked before measurement"
 * and "Store repositories are released", and the scenario "Store from another
 * jar": every built-in store with every built-in corpus, and test stores
 * that lose or misreport policies.
 */
public class PdpFixtureTest
{
	/**
	 * A policy set that the RSA 2008 root refers to through its first child,
	 * which has an empty target ({@code XacmlPolicySet-01-top-level.xml}
	 * lines 14-21), so every request reaches the reference.
	 */
	private static final String EMERGENCY = "urn:va:xacml:2.0:interop:rsa8:policysetid:emergency";

	@BeforeEach
	public void resetStores()
	{
		TestStores.Counting.reset();
		TestStores.Probe.reset();
	}

	@Test
	public void inMemoryStoreDecidesEveryRsa2008RequestAsExpected() throws Exception
	{
		try(PdpFixture fixture = PdpFixture.open("in-memory", "rsa2008")){
			assertThat(fixture.corpus().policies()).hasSize(9);
			assertThat(fixture.corpus().requests()).hasSize(17);
			assertThat(fixture.lookups()).isNotEmpty();
			for(CorpusRequest request : fixture.corpus().requests()){
				Collection<Result> results = fixture.pdp().decide(request.request()).getResults();
				assertThat(results).hasSize(1);
				assertThat(results.iterator().next().getDecision()).isEqualTo(request.expected());
			}
			for(LookupQuery query : fixture.lookups()){
				assertThat(query.find(fixture.repository()).isPresent()).isTrue();
			}
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {"rsa2008", "synthetic:100"})
	public void targetIndexKeepsTheCorpusResponses(String corpus) throws Exception
	{
		try(PdpFixture linear = PdpFixture.open("in-memory", corpus, false);
				PdpFixture indexed = PdpFixture.open("in-memory", corpus, true)){
			for(CorpusRequest request : linear.corpus().requests()){
				List<Result> expected = List.copyOf(linear.pdp().decide(request.request()).getResults());
				List<Result> actual = List.copyOf(indexed.pdp().decide(request.request()).getResults());
				assertWithMessage("response for %s", request.id()).that(actual).isEqualTo(expected);
				for(int i = 0; i < expected.size(); i++){
					assertThat(List.copyOf(actual.get(i).getObligations())).isEqualTo(List.copyOf(expected.get(i).getObligations()));
					assertThat(List.copyOf(actual.get(i).getAssociatedAdvice())).isEqualTo(List.copyOf(expected.get(i).getAssociatedAdvice()));
					assertThat(List.copyOf(actual.get(i).getPolicyIdentifiers())).isEqualTo(List.copyOf(expected.get(i).getPolicyIdentifiers()));
				}
			}
		}
	}

	@Test
	public void requestSelection() throws Exception
	{
		try(PdpFixture fixture = PdpFixture.open("in-memory", "rsa2008")){
			assertThat(fixture.requests("all")).hasSize(17);
			assertThat(fixture.requests("01-01")).hasSize(1);
			IllegalArgumentException unknown = org.junit.jupiter.api.Assertions.assertThrows(
					IllegalArgumentException.class, () -> fixture.requests("99-99"));
			assertThat(unknown).hasMessageThat().contains("99-99");
		}
	}

	static Stream<Arguments> builtInStoresAndCorpora()
	{
		Stream.Builder<Arguments> cases = Stream.builder();
		for(String store : List.of("in-memory", "snapshot")){
			for(String corpus : List.of("rsa2008", "synthetic:1", "synthetic:2", "synthetic:100")){
				cases.add(Arguments.of(store, corpus));
			}
		}
		return cases.build();
	}

	@ParameterizedTest(name = "{0} with {1}")
	@MethodSource("builtInStoresAndCorpora")
	public void everyBuiltInStoreAndCorpusPassesItsChecks(String store, String corpus) throws Exception
	{
		try(PdpFixture fixture = PdpFixture.open(store, corpus)){
			assertThat(fixture.store().name()).isEqualTo(store);
			assertThat(fixture.corpus().name()).isEqualTo(corpus);
			assertDecidesAndFindsEverything(fixture);
		}
	}

	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = {"in-memory", "snapshot"})
	public void everyBuiltInStorePassesTheChecksOfADirectoryCorpus(String store, @TempDir Path dir) throws Exception
	{
		TestCorpora.writeDirectory(dir);

		try(PdpFixture fixture = PdpFixture.open(store, "dir:" + dir)){
			assertThat(fixture.corpus().requests()).hasSize(3);
			assertDecidesAndFindsEverything(fixture);
		}
	}

	@Test
	public void storeRegisteredOutsideTheMainCodeIsUsedByName() throws Exception
	{
		// TestStores.Probe is registered in src/test/resources/META-INF/services, a
		// classpath entry outside the main code of the module
		try(PdpFixture fixture = PdpFixture.open(TestStores.Probe.NAME, "rsa2008")){
			assertThat(fixture.store()).isInstanceOf(TestStores.Probe.class);
			assertThat(TestStores.Probe.repositoryIds()).containsExactly(fixture.repository().getId());
		}
	}

	@Test
	public void fixtureClosesItsSessionWhenItClosesAndWhenItsDecisionCheckFails() throws Exception
	{
		PolicyCorpus rsa2008 = PolicyCorpusProvider.corpus("rsa2008");
		// XacmlResponse-01-01.xml: Permit
		PolicyCorpus wrong = withExpectation(rsa2008, "01-01", Decision.DENY);

		try(PdpFixture fixture = PdpFixture.open(TestStores.Counting.NAME, rsa2008)){
			assertThat(TestStores.Counting.closed()).isEmpty();
		}
		IllegalStateException e = assertThrows(IllegalStateException.class,
				() -> PdpFixture.open(TestStores.Counting.NAME, wrong));

		assertThat(FailureLines.of(e).requests()).hasSize(1);
		assertThat(FailureLines.of(e).request("01-01").isPresent()).isTrue();
		assertThat(TestStores.Counting.opened()).hasSize(2);
		assertThat(TestStores.Counting.opened()).containsNoDuplicates();
		assertThat(TestStores.Counting.closed()).containsExactlyElementsIn(TestStores.Counting.opened());
	}

	@Test
	public void fixtureClosesItsSessionWhenTheStoreRejectsAPolicy() throws Exception
	{
		PolicyCorpus documents = TestCorpora.documents("documents");
		List<PolicySource> policies = new ArrayList<>(documents.policies());
		policies.add(TestCorpora.source("documents-again.xml", TestCorpora.documentsPolicy()));
		PolicyCorpus twice = new PolicyCorpus("twice", policies, documents.root(), documents.requests());

		IllegalStateException e = assertThrows(IllegalStateException.class,
				() -> PdpFixture.open(TestStores.Counting.NAME, twice));

		assertThat(e).hasMessageThat().contains(TestCorpora.DOCUMENTS_ID);
		assertThat(TestStores.Counting.opened()).hasSize(1);
		assertThat(TestStores.Counting.closed()).containsExactlyElementsIn(TestStores.Counting.opened());
	}

	@Test
	public void everyNewRepositoryHasItsOwnIdAndTheStoreArgument() throws Exception
	{
		try(PdpFixture fixture = PdpFixture.open("probe:x", TestCorpora.documents("documents"))){
			PolicyRepository first = fixture.newRepository();
			PolicyRepository second = fixture.newRepository();
			fixture.release(first);
			fixture.release(second);

			assertThat(first.getId()).isNotEqualTo(second.getId());
			assertThat(first.getId()).isNotEqualTo(fixture.repository().getId());
			assertThat(second.getId()).isNotEqualTo(fixture.repository().getId());
			assertThat(TestStores.Probe.arguments()).containsExactly(Optional.of("x"), Optional.of("x"), Optional.of("x"));
		}
	}

	@Test
	public void storeThatLosesAReferencedPolicySetFailsEveryRequestWhoseDecisionChanged() throws Exception
	{
		PolicyCorpus rsa2008 = PolicyCorpusProvider.corpus("rsa2008");
		assertRootRefersTo(rsa2008, EMERGENCY);
		List<TestPdps.Outcome> outcomes = outcomes(new TestStores.Losing(), EMERGENCY, rsa2008);
		assertWithMessage("requests whose decision changes without %s", EMERGENCY)
				.that(failed(outcomes)).isNotEmpty();

		IllegalStateException e = assertThrows(IllegalStateException.class,
				() -> PdpFixture.open(TestStores.Losing.NAME + ":" + EMERGENCY, "rsa2008"));

		assertNamesExactlyTheFailedRequests(FailureLines.of(e), outcomes);
	}

	@Test
	public void storeThatThrowsOnAReferencedPolicySetNamesTheExceptionOfEveryRequestThatThrew() throws Exception
	{
		PolicyCorpus rsa2008 = PolicyCorpusProvider.corpus("rsa2008");
		assertRootRefersTo(rsa2008, EMERGENCY);
		List<TestPdps.Outcome> outcomes = outcomes(new TestStores.Throwing(), EMERGENCY, rsa2008);
		List<String> threw = new ArrayList<>();
		for(TestPdps.Outcome outcome : outcomes){
			outcome.thrown().ifPresent(type -> threw.add(outcome.id()));
		}
		assertWithMessage("requests whose evaluation throws %s", TestStores.LostPolicyException.class.getSimpleName())
				.that(threw).isNotEmpty();

		IllegalStateException e = assertThrows(IllegalStateException.class,
				() -> PdpFixture.open(TestStores.Throwing.NAME + ":" + EMERGENCY, "rsa2008"));

		assertNamesExactlyTheFailedRequests(FailureLines.of(e), outcomes);
		for(String id : threw){
			assertThat(FailureLines.of(e).request(id).get()).contains(TestStores.LostPolicyException.class.getName());
		}
	}

	@Test
	public void storeThatLosesTheRootFailsWithTheRootId() throws Exception
	{
		PolicyCorpus rsa2008 = PolicyCorpusProvider.corpus("rsa2008");
		String root = PdpFixture.parse(rsa2008.policies()).get(rsa2008.rootIndex()).getId();

		IllegalStateException e = assertThrows(IllegalStateException.class,
				() -> PdpFixture.open(TestStores.Losing.NAME + ":" + root, rsa2008));

		assertThat(e).hasMessageThat().contains(root);
	}

	@Test
	public void versionedCorpusPassesWithACorrectStore() throws Exception
	{
		try(PdpFixture fixture = PdpFixture.open("in-memory", TestCorpora.versioned("versioned"))){
			assertDecidesAndFindsEverything(fixture);
		}
	}

	@Test
	public void storeThatLosesAPolicyNoRequestReachesFailsOnlyItsLookup() throws Exception
	{
		IllegalStateException e = assertThrows(IllegalStateException.class,
				() -> PdpFixture.open(TestStores.Losing.NAME + ":" + TestCorpora.SPARE_ID,
						TestCorpora.versioned("versioned")));

		FailureLines lines = FailureLines.of(e);
		assertThat(lines.requests()).isEmpty();
		assertThat(lines.lookups()).hasSize(1);
		assertThat(lines.lookups().get(0)).contains(TestCorpora.SPARE_ID);
	}

	@Test
	public void storeThatReturnsAnOlderVersionAsTheLatestFailsTheReferenceLookup() throws Exception
	{
		IllegalStateException e = assertThrows(IllegalStateException.class,
				() -> PdpFixture.open(TestStores.StaleLatest.NAME, TestCorpora.versioned("versioned")));

		// both versions permit read, so only the lookup check sees the wrong version
		FailureLines lines = FailureLines.of(e);
		assertThat(lines.requests()).isEmpty();
		assertThat(lines.lookups()).hasSize(1);
		assertThat(lines.lookups().get(0)).contains(TestCorpora.VERSIONED_ID);
		assertThat(lines.lookups().get(0)).contains("1.0");
		assertThat(lines.lookups().get(0)).contains("2.0");
	}

	@Test
	public void requestWithTwoResultsFailsTheCheck() throws Exception
	{
		PolicyCorpus documents = TestCorpora.documents("documents");
		PolicyCorpus corpus = new PolicyCorpus("two-results", documents.policies(), documents.root(),
				List.of(TestCorpora.request("two-resources", TestCorpora.twoResourceRequest(), Decision.PERMIT)));

		IllegalStateException e = assertThrows(IllegalStateException.class,
				() -> PdpFixture.open("in-memory", corpus));

		Optional<String> line = FailureLines.of(e).request("two-resources");
		assertThat(line.isPresent()).isTrue();
		assertThat(line.get()).contains("2");
	}

	@Test
	public void wrongExpectationOfADirectoryCorpusNamesTheRequestAndBothDecisions(@TempDir Path dir) throws Exception
	{
		TestCorpora.writeDirectory(dir);
		TestCorpora.writeManifest(dir, TestCorpora.MANIFEST.replace("expect.a-read.xml=Permit", "expect.a-read.xml=Deny"));

		IllegalStateException e = assertThrows(IllegalStateException.class,
				() -> PdpFixture.open("in-memory", "dir:" + dir));

		FailureLines lines = FailureLines.of(e);
		assertThat(lines.requests()).hasSize(1);
		assertThat(lines.requests().get(0)).contains("a-read");
		assertThat(lines.requests().get(0)).contains(String.valueOf(Decision.DENY));
		assertThat(lines.requests().get(0)).contains(String.valueOf(Decision.PERMIT));
	}

	@Test
	public void expectedIndeterminateMatchesTheExtendedIndeterminateOfThePdp() throws Exception
	{
		// Request "audit" expects Indeterminate, as a response carries it. Its
		// Permit rule has an Indeterminate condition, so the rule is
		// Indeterminate{P} (XACML 3.0 core §7.11 Table 4), and deny-overrides
		// keeps Indeterminate{P} up to the root (Appendix C.2)
		try(PdpFixture fixture = PdpFixture.open("in-memory", TestCorpora.documents("documents"))){
			CorpusRequest audit = fixture.requests("audit").get(0);
			Decision decision = fixture.pdp().decide(audit.request()).getResults().iterator().next().getDecision();

			assertThat(audit.expected()).isEqualTo(Decision.INDETERMINATE);
			assertThat(decision).isEqualTo(Decision.INDETERMINATE_P);
		}
	}

	@Test
	public void concurrentCheckDecidesEveryRequestFromSeveralThreads() throws Exception
	{
		try(PdpFixture fixture = PdpFixture.open("in-memory", "rsa2008")){
			fixture.verifyConcurrent(1);
			fixture.verifyConcurrent(4);
		}
	}

	@Test
	public void concurrentCheckNeedsAThread() throws Exception
	{
		try(PdpFixture fixture = PdpFixture.open("in-memory", TestCorpora.documents("documents"))){
			assertThrows(IllegalArgumentException.class, () -> fixture.verifyConcurrent(0));
		}
	}

	private static void assertDecidesAndFindsEverything(PdpFixture fixture)
	{
		for(CorpusRequest request : fixture.corpus().requests()){
			Collection<Result> results = fixture.pdp().decide(request.request()).getResults();
			assertWithMessage("results of request %s", request.id()).that(results).hasSize(1);
			Decision decision = results.iterator().next().getDecision();
			assertWithMessage("request %s: expected %s, got %s", request.id(), request.expected(), decision)
					.that(request.matches(decision)).isTrue();
		}
		int exact = 0;
		for(LookupQuery query : fixture.lookups()){
			Optional<CompositeDecisionRule> found = query.find(fixture.repository());
			assertWithMessage("lookup %s", query.label()).that(found.isPresent()).isTrue();
			assertWithMessage("lookup %s returned %s", query.label(), Rules.describe(found.get()))
					.that(Rules.sameIdentity(query.expected(), found.get())).isTrue();
			if(query.kind() == LookupQuery.Kind.EXACT){
				exact++;
			}
		}
		assertWithMessage("exact lookups").that(exact).isEqualTo(fixture.policies().size());
	}

	/**
	 * Decides every corpus request with a PDP of its own over a repository of
	 * the store: what the fixture must report.
	 */
	private static List<TestPdps.Outcome> outcomes(PolicyStore store, String lost, PolicyCorpus corpus) throws Exception
	{
		List<CompositeDecisionRule> rules = PdpFixture.parse(corpus.policies());
		List<TestPdps.Outcome> outcomes = new ArrayList<>();
		PolicyRepository repository = TestStores.openLoaded(store, Optional.of(lost), "oracle-" + store.name(), rules);
		try(PolicyDecisionPoint pdp = TestPdps.build(repository, rules.get(corpus.rootIndex()))){
			for(CorpusRequest request : corpus.requests()){
				outcomes.add(TestPdps.decide(pdp, request));
			}
		}finally{
			store.close(repository);
		}
		return outcomes;
	}

	private static List<String> failed(List<TestPdps.Outcome> outcomes)
	{
		List<String> ids = new ArrayList<>();
		for(TestPdps.Outcome outcome : outcomes){
			if(outcome.failed()){
				ids.add(outcome.id());
			}
		}
		return ids;
	}

	private static void assertNamesExactlyTheFailedRequests(FailureLines lines, List<TestPdps.Outcome> outcomes)
	{
		for(TestPdps.Outcome outcome : outcomes){
			Optional<String> line = lines.request(outcome.id());
			if(!outcome.failed()){
				assertWithMessage("request %s decides as expected; the failure names it", outcome.id())
						.that(line.isPresent()).isFalse();
				continue;
			}
			assertWithMessage("failure names request %s", outcome.id()).that(line.isPresent()).isTrue();
			outcome.thrown().ifPresent(type -> assertThat(line.get()).contains(type.getName()));
			outcome.wrongResult().ifPresent(result -> assertThat(line.get()).contains(result));
		}
		assertThat(lines.requests()).hasSize(failed(outcomes).size());
	}

	private static void assertRootRefersTo(PolicyCorpus corpus, String id) throws Exception
	{
		CompositeDecisionRule root = PdpFixture.parse(corpus.policies()).get(corpus.rootIndex());
		List<String> references = new ArrayList<>();
		root.accept(new PolicyVisitorSupport(){
			@Override
			public void visitEnter(PolicySetIDReference ref){
				references.add(ref.getId());
			}
		});
		assertWithMessage("policy sets the root of %s refers to", corpus.name()).that(references).contains(id);
	}

	private static PolicyCorpus withExpectation(PolicyCorpus corpus, String id, Decision expected)
	{
		List<CorpusRequest> requests = new ArrayList<>();
		for(CorpusRequest request : corpus.requests()){
			requests.add(request.id().equals(id) ? new CorpusRequest(id, request.request(), expected) : request);
		}
		return new PolicyCorpus(corpus.name(), corpus.policies(), corpus.root(), requests);
	}
}
