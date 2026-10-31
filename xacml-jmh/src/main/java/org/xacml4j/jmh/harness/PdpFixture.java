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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xacml4j.jmh.corpus.PipCorpus;
import org.xacml4j.jmh.corpus.PipWorkload;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.ResponseContext;
import org.xacml4j.v30.Result;
import org.xacml4j.v30.SyntaxException;
import org.xacml4j.v30.pdp.PolicyDecisionPointBuilder;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;
import org.xacml4j.v30.spi.pip.PolicyInformationPoint;
import org.xacml4j.v30.spi.repository.PolicyRepository;

/**
 * A corpus loaded into one repository of a store, with a PDP on top. Opening a
 * fixture checks every corpus decision and every corpus lookup before any
 * measurement; a fixture that opens is safe to measure.
 */
public final class PdpFixture implements AutoCloseable
{
	private static final Logger LOG = LoggerFactory.getLogger(PdpFixture.class);

	private static final FunctionProvider FUNCTIONS = FunctionProvider.builder().withDefaultFunctions().build();
	private static final DecisionCombiningAlgorithmProvider ALGORITHMS =
			DecisionCombiningAlgorithmProvider.builder().withDefaultAlgorithms().build();
	private static final AtomicLong REPOSITORIES = new AtomicLong();
	/**
	 * Unique per JVM: JMH forks start their sequence again, so the process id
	 * and start time keep repository ids apart across forks (review rv006).
	 */
	private static final String PROCESS = Long.toHexString(ProcessHandle.current().pid())
			+ "-" + Long.toHexString(System.currentTimeMillis());

	private final PolicyStore store;
	private final Optional<String> argument;
	private final PolicyCorpus corpus;
	private final List<CompositeDecisionRule> policies;
	private final PolicyRepository repository;
	private final PolicyDecisionPoint pdp;
	private final List<LookupQuery> lookups;
	private final Optional<PipWorkload> ownedPip;
	private final boolean pipCorpus;

	private PdpFixture(PolicyStore store, Optional<String> argument, PolicyCorpus corpus, List<CompositeDecisionRule> policies,
			PolicyRepository repository, PolicyDecisionPoint pdp, List<LookupQuery> lookups,
			Optional<PipWorkload> ownedPip, boolean pipCorpus){
		this.store = store;
		this.argument = argument;
		this.corpus = corpus;
		this.policies = policies;
		this.repository = repository;
		this.pdp = pdp;
		this.lookups = lookups;
		this.ownedPip = ownedPip;
		this.pipCorpus = pipCorpus;
	}

	/**
	 * @param store the {@code store} parameter: {@code name} or {@code name:argument}
	 * @param corpus the {@code corpus} parameter: {@code name} or {@code name:argument}
	 * @return an open, checked fixture
	 * @throws IllegalStateException if a check fails; the message names every failure
	 */
	public static PdpFixture open(String store, String corpus) throws IOException, SyntaxException {
		return open(store, PolicyCorpusProvider.corpus(corpus));
	}

	/** Opens a checked fixture with optional target indexing. */
	public static PdpFixture open(String store, String corpus, boolean targetIndex) throws IOException, SyntaxException {
		return open(store, PolicyCorpusProvider.corpus(corpus), targetIndex);
	}

	/**
	 * Parses the corpus, loads it into a new repository of the store, takes
	 * the root from the store, builds the PDP, and checks every lookup and
	 * decision. On failure it closes what it opened.
	 *
	 * @param store the store parameter: {@code name} or {@code name:argument}
	 * @param corpus the corpus
	 * @return an open, checked fixture
	 * @throws IllegalArgumentException if no store has that name
	 * @throws IllegalStateException if a check fails; the message names every failure
	 */
	public static PdpFixture open(String store, PolicyCorpus corpus) throws IOException, SyntaxException {
		return open(store, corpus, false);
	}

	private static PdpFixture open(String store, PolicyCorpus corpus, boolean targetIndex) throws IOException, SyntaxException {
		if(PipCorpus.isPip(corpus.name())){
			PipWorkload workload = PipWorkload.open(PipCorpus.count(corpus.name()), 0, "serial", "off", 32);
			try{
				return open(store, corpus, workload.pip(), Optional.of(workload), true, targetIndex);
			}catch(IOException | RuntimeException | Error failure){
				try{ workload.close(); }catch(RuntimeException closeFailure){ failure.addSuppressed(closeFailure); }
				throw failure;
			}
		}
		return open(store, corpus, PolicyInformationPoint.builder("jmh").withDefaultRegistry().noCacheProvider().build(),
				Optional.empty(), false, targetIndex);
	}

	/**
	 * Opens a checked fixture with a caller-supplied information point. The caller owns the PIP.
	 *
	 * @param store the store parameter
	 * @param corpus the corpus parameter
	 * @param pip the information point
	 * @return an open, checked fixture
	 */
	public static PdpFixture open(String store, String corpus, PolicyInformationPoint pip) throws IOException, SyntaxException {
		return open(store, PolicyCorpusProvider.corpus(corpus), pip);
	}

	/** Opens a checked fixture with a caller-owned PIP and optional target indexing. */
	public static PdpFixture open(String store, String corpus, PolicyInformationPoint pip, boolean targetIndex)
			throws IOException, SyntaxException {
		PolicyCorpus parsed = PolicyCorpusProvider.corpus(corpus);
		return open(store, parsed, java.util.Objects.requireNonNull(pip, "pip"),
				Optional.empty(), PipCorpus.isPip(parsed.name()), targetIndex);
	}

	/** Opens a checked corpus with a caller-owned information point. */
	public static PdpFixture open(String store, PolicyCorpus corpus, PolicyInformationPoint pip) throws IOException, SyntaxException {
		return open(store, corpus, java.util.Objects.requireNonNull(pip, "pip"),
				Optional.empty(), PipCorpus.isPip(corpus.name()), false);
	}

	private static PdpFixture open(String store, PolicyCorpus corpus, PolicyInformationPoint pip,
			Optional<PipWorkload> ownedPip, boolean pipCorpus, boolean targetIndex) throws IOException, SyntaxException {
		String[] nameAndArgument = Services.split(store);
		return open(PolicyStore.named(nameAndArgument[0]), Optional.ofNullable(nameAndArgument[1]), corpus, pip, ownedPip, pipCorpus, targetIndex);
	}

	private static PdpFixture open(PolicyStore store, Optional<String> argument, PolicyCorpus corpus,
			PolicyInformationPoint pip, Optional<PipWorkload> ownedPip, boolean pipCorpus, boolean targetIndex) throws IOException, SyntaxException {
		List<CompositeDecisionRule> policies = parse(corpus.policies());
		CompositeDecisionRule parsedRoot = policies.get(corpus.rootIndex());
		PolicyRepository repository = openRepository(store, argument, corpus);
		PolicyDecisionPoint pdp = null;
		boolean opened = false;
		try{
			load(repository, policies);
			CompositeDecisionRule root = repository.get(parsedRoot.getId(), Rules.version(parsedRoot));
			if(root == null){
				throw new IllegalStateException(header(store, corpus) + " did not return the root " + Rules.describe(parsedRoot));
			}
			PolicyDecisionPointBuilder builder = PolicyDecisionPointBuilder.builder("jmh")
					.policyRepository(repository)
					.pip(pip)
					.defaultRequestHandlers()
					.rootPolicy(root);
			if(targetIndex) builder.targetIndex();
			pdp = builder.build();
			PdpFixture fixture = new PdpFixture(store, argument, corpus, policies, repository, pdp,
					LookupQuery.of(policies), ownedPip, pipCorpus);
			fixture.check();
			opened = true;
			return fixture;
		}finally{
			if(!opened){
				closeQuietly(pdp, store, repository);
			}
		}
	}

	/**
	 * Adds every policy to a repository.
	 *
	 * @param repository the repository
	 * @param policies the policies
	 * @throws IllegalStateException if the repository does not add one
	 */
	public static void load(PolicyRepository repository, List<CompositeDecisionRule> policies){
		for(CompositeDecisionRule policy : policies){
			if(!repository.add(policy)){
				throw new IllegalStateException(String.format(
						"Repository \"%s\" did not add %s", repository.getId(), Rules.describe(policy)));
			}
		}
	}

	/**
	 * Parses policy documents with the policy unmarshaller of their media type, over the functions
	 * and algorithms of the fixture's repositories: the regular-expression engine the properties
	 * {@code org.xacml4j.regex.engine} and {@code org.xacml4j.regex.profile} select evaluates the
	 * regexp-match functions of the parsed policies (change add-regex-engine-spi, design D3).
	 *
	 * @param sources the documents
	 * @return the parsed rules, in the same order
	 */
	public static List<CompositeDecisionRule> parse(List<PolicySource> sources) throws IOException, SyntaxException {
		List<CompositeDecisionRule> rules = new ArrayList<>(sources.size());
		for(PolicySource source : sources){
			rules.add(Marshalling.policies(source.mediaType(), FUNCTIONS, ALGORITHMS).unmarshal(source.open()));
		}
		return List.copyOf(rules);
	}

	/** @return the store of this fixture */
	public PolicyStore store(){
		return store;
	}

	/** @return the repository that holds the corpus */
	public PolicyRepository repository(){
		return repository;
	}

	/**
	 * @return another empty repository of this fixture's store, with an id that no other
	 * repository has; close it with {@link #release}
	 */
	public PolicyRepository newRepository(){
		return openRepository(store, argument, corpus);
	}

	/**
	 * Closes a repository of this fixture's store through the store.
	 *
	 * @param repository a repository from {@link #newRepository}
	 * @throws IllegalStateException if the store cannot close it
	 */
	public void release(PolicyRepository repository){
		try{
			store.close(repository);
		}catch(Exception e){
			throw new IllegalStateException("Cannot close repository \"" + repository.getId() + "\" of " + header(store, corpus), e);
		}
	}

	public PolicyCorpus corpus(){
		return corpus;
	}

	/** @return the parsed corpus rules, in corpus order */
	public List<CompositeDecisionRule> policies(){
		return policies;
	}

	/** @return the lookups of the corpus, all checked when the fixture opened */
	public List<LookupQuery> lookups(){
		return lookups;
	}

	public PolicyDecisionPoint pdp(){
		return pdp;
	}

	/**
	 * @param selector {@code all}, or the id of one corpus request
	 * @return the selected requests, in corpus order
	 * @throws IllegalArgumentException if no request has that id
	 */
	public List<CorpusRequest> requests(String selector){
		if("all".equals(selector)){
			return corpus.requests();
		}
		List<String> ids = new ArrayList<>();
		for(CorpusRequest request : corpus.requests()){
			if(request.id().equals(selector)){
				return List.of(request);
			}
			ids.add(request.id());
		}
		throw new IllegalArgumentException(String.format(
				"Unknown request \"%s\" in corpus \"%s\"; known: all, %s", selector, corpus.name(), ids));
	}

	/**
	 * Runs every corpus lookup against the store and decides every corpus
	 * request from {@code threads} threads at once, each thread starting at
	 * another item, and checks every answer. The lookups run on the store
	 * itself: the PDP's reference cache would hide the store from the
	 * decisions (review rv006).
	 *
	 * @param threads the number of threads
	 * @throws IllegalStateException if a lookup or a decision is wrong or throws
	 */
	public void verifyConcurrent(int threads){
		if(threads < 1){
			throw new IllegalArgumentException("threads=" + threads);
		}
		List<CorpusRequest> requests = corpus.requests();
		Queue<String> failures = new ConcurrentLinkedQueue<>();
		CountDownLatch start = new CountDownLatch(1);
		List<Thread> workers = new ArrayList<>(threads);
		for(int t = 0; t < threads; t++){
			int requestOffset = t * requests.size() / threads;
			int lookupOffset = t * lookups.size() / threads;
			Thread worker = new Thread(() -> {
				try{
					start.await();
					for(int i = 0; i < lookups.size(); i++){
						check(lookups.get((lookupOffset + i) % lookups.size()), repository).ifPresent(failures::add);
					}
					for(int i = 0; i < requests.size(); i++){
						check(requests.get((requestOffset + i) % requests.size())).ifPresent(failures::add);
					}
				}catch(InterruptedException e){
					Thread.currentThread().interrupt();
					failures.add("interrupted");
				}
			}, "jmh-verify-" + t);
			worker.setDaemon(true);
			workers.add(worker);
			worker.start();
		}
		start.countDown();
		for(Thread worker : workers){
			try{
				worker.join();
			}catch(InterruptedException e){
				Thread.currentThread().interrupt();
				throw new IllegalStateException("Interrupted while checking concurrent decisions", e);
			}
		}
		if(!failures.isEmpty()){
			throw new IllegalStateException(header(store, corpus) + " failed " + failures.size()
					+ " concurrent check(s) with " + threads + " threads:\n  " + String.join("\n  ", failures));
		}
	}

	/**
	 * Runs every corpus lookup against another repository of this fixture's
	 * store, for example one that a load benchmark filled.
	 *
	 * @param repository the repository to check
	 * @param what names the repository in the failure message
	 * @throws IllegalStateException if a lookup returns no rule, the wrong rule, or throws
	 */
	public void verifyLookups(PolicyRepository repository, String what){
		List<String> failures = new ArrayList<>();
		for(LookupQuery query : lookups){
			check(query, repository).ifPresent(failures::add);
		}
		if(!failures.isEmpty()){
			throw new IllegalStateException(header(store, corpus) + ", " + what + ", failed " + failures.size()
					+ " lookup check(s):\n  " + String.join("\n  ", failures));
		}
	}

	/**
	 * Runs the lookup and decision checks of {@link #open} again, for example
	 * after a measurement, so that a store that changed during a trial fails
	 * the run instead of reporting numbers (review rv005).
	 *
	 * @throws IllegalStateException if a check fails
	 */
	public void recheck(){
		check();
	}

	/** Closes the PDP, then the repository. */
	@Override
	public void close(){
		IllegalStateException failure = null;
		try{
			pdp.close();
		}catch(IOException | RuntimeException e){
			failure = new IllegalStateException("Cannot close the PDP of " + header(store, corpus), e);
		}
		try{
			store.close(repository);
		}catch(Exception e){
			if(failure == null){
				failure = new IllegalStateException("Cannot close the repository of " + header(store, corpus), e);
			}else{
				failure.addSuppressed(e);
			}
		}
		try{
			ownedPip.ifPresent(PipWorkload::close);
		}catch(RuntimeException e){
			if(failure == null){
				failure = new IllegalStateException("Cannot close the PIP of " + header(store, corpus), e);
			}else{
				failure.addSuppressed(e);
			}
		}
		if(failure != null){
			throw failure;
		}
	}

	private void check(){
		List<String> failures = new ArrayList<>();
		for(LookupQuery query : lookups){
			check(query, repository).ifPresent(failures::add);
		}
		for(CorpusRequest request : corpus.requests()){
			check(request).ifPresent(failures::add);
		}
		if(!failures.isEmpty()){
			throw new IllegalStateException(header(store, corpus) + " failed " + failures.size()
					+ " check(s):\n  " + String.join("\n  ", failures));
		}
	}

	private static Optional<String> check(LookupQuery query, PolicyRepository repository){
		try{
			Optional<CompositeDecisionRule> found = query.find(repository);
			if(found.isEmpty()){
				return Optional.of("lookup " + query.label() + ": no rule, expected " + Rules.describe(query.expected()));
			}
			if(!Rules.sameIdentity(query.expected(), found.get())){
				return Optional.of("lookup " + query.label() + ": got " + Rules.describe(found.get())
						+ ", expected " + Rules.describe(query.expected()));
			}
			return Optional.empty();
		}catch(RuntimeException e){
			return Optional.of("lookup " + query.label() + ": threw " + e);
		}
	}

	private Optional<String> check(CorpusRequest request){
		try{
			ResponseContext response = pdp.decide(request.request());
			if(pipCorpus){
				return PipCorpus.check(corpus, request, response).map(message -> "request " + request.id() + ": " + message);
			}
			Collection<Result> results = response.getResults();
			if(results.size() != 1){
				return Optional.of("request " + request.id() + ": expected one result, got " + results.size());
			}
			return request.mismatch(results.iterator().next()).map(message -> "request " + request.id() + ": " + message);
		}catch(RuntimeException e){
			return Optional.of("request " + request.id() + ": expected " + request.expected() + ", threw " + e);
		}
	}

	private static PolicyRepository openRepository(PolicyStore store, Optional<String> argument, PolicyCorpus corpus){
		String id = "jmh-" + safe(store.name()) + "-" + safe(corpus.name()) + "-" + PROCESS + "-" + REPOSITORIES.incrementAndGet();
		try{
			return store.open(id, argument, FUNCTIONS, ALGORITHMS);
		}catch(RuntimeException e){
			throw e;
		}catch(Exception e){
			throw new IllegalStateException("Store \"" + store.name() + "\" cannot open repository \"" + id + "\"", e);
		}
	}

	/** Keeps an id usable as a file or namespace name: a {@code dir:} corpus name holds a path. */
	private static String safe(String name){
		return name.replaceAll("[^A-Za-z0-9._-]", "_");
	}

	private static String header(PolicyStore store, PolicyCorpus corpus){
		return "Store \"" + store.name() + "\" with corpus \"" + corpus.name() + "\"";
	}

	private static void closeQuietly(PolicyDecisionPoint pdp, PolicyStore store, PolicyRepository repository){
		if(pdp != null){
			try{
				pdp.close();
			}catch(IOException | RuntimeException e){
				LOG.warn("Cannot close a PDP after a failed fixture", e);
			}
		}
		try{
			store.close(repository);
		}catch(Exception e){
			LOG.warn("Cannot close a store repository after a failed fixture", e);
		}
	}
}
