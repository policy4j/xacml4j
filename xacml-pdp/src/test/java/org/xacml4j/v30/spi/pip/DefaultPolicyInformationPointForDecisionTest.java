package org.xacml4j.v30.spi.pip;

/*
 * #%L
 * Xacml4J PDP
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceEvaluationException;
import org.xacml4j.v30.AttributeReferenceKey;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.PolicyDecisionPoint;
import org.xacml4j.v30.RequestContext;
import org.xacml4j.v30.XPathVersion;
import org.xacml4j.v30.policy.EvaluationContextHandler;
import org.xacml4j.v30.policy.PolicyReferenceResolver;
import org.xacml4j.v30.policy.RootEvaluationContext;
import org.xacml4j.v30.spi.observe.DecisionObservation;
import org.xacml4j.v30.spi.observe.DecisionObserver;
import org.xacml4j.v30.types.XacmlTypes;

/**
 * The information point of one decision (task 30 of change optimize-pdp-evaluation, design D27):
 * the attributes that one resolver gives in one set serve that decision together, once the key of
 * the resolver call is known without resolving a context key before the resolver asks for it
 * (review R-FABLE-sol).
 */
public class DefaultPolicyInformationPointForDecisionTest
{
	private static final AttributeDesignatorKey SUBJECT_ID = key("urn:xacml4j:test:subject-id");
	private static final AttributeDesignatorKey ROLE = key("urn:xacml4j:test:role");
	private static final AttributeDesignatorKey DEPARTMENT = key("urn:xacml4j:test:department");
	private static final AttributeDesignatorKey TITLE = key("urn:xacml4j:test:title");

	private final TestClock clock = new TestClock();
	/** The backend: what each read of the profile of the subject answers, in order */
	private final Deque<Supplier<Map<String, BagOfValues>>> reads = new ArrayDeque<>();
	private EvaluationContext context;

	@BeforeEach
	public void init()
	{
		context = Mockito.mock(EvaluationContext.class);
		when(context.getClock()).thenReturn(clock);
		when(context.resolve(SUBJECT_ID)).thenReturn(Optional.of(bag("alice")));
	}

	@Test
	public void theAttributesOfOneReadServeTheDecisionTogether()
	{
		reads.add(profile("admin", "sales"));
		reads.add(profile("admin", "support"));
		PolicyInformationPoint point = point(profile(3600));
		PolicyInformationPoint decision = point.forDecision();

		assertEquals(Optional.of(bag("admin")), decision.resolve(context, ROLE));
		assertEquals(Optional.of(bag("sales")), decision.resolve(context, DEPARTMENT));
		assertEquals(Optional.of(bag("support")), point.forDecision().resolve(context, DEPARTMENT));
	}

	@Test
	public void anAttributeMissingFromTheReadIsResolvedAgain()
	{
		reads.add(() -> Map.of(ROLE.getAttributeId(), bag("admin")));
		reads.add(profile("admin", "sales"));
		PolicyInformationPoint decision = point(profile(3600)).forDecision();

		assertEquals(Optional.of(bag("admin")), decision.resolve(context, ROLE));
		assertEquals(Optional.of(bag("sales")), decision.resolve(context, DEPARTMENT));
	}

	@Test
	public void aReadOlderThanTheTimeToLiveOfItsResolverIsNotReused()
	{
		reads.add(profile("admin", "sales"));
		reads.add(profile("admin", "support"));
		PolicyInformationPoint decision = point(profile(1)).forDecision();

		assertEquals(Optional.of(bag("admin")), decision.resolve(context, ROLE));
		clock.advanceSeconds(2);
		assertEquals(Optional.of(bag("support")), decision.resolve(context, DEPARTMENT));
	}

	@Test
	public void aFailedReadIsNotKeptForTheDecision()
	{
		reads.add(() -> { throw new IllegalStateException("backend down"); });
		reads.add(profile("admin", "sales"));
		PolicyInformationPoint decision = point(profile(3600)).forDecision();

		assertThrows(AttributeReferenceEvaluationException.class, () -> decision.resolve(context, ROLE));
		assertEquals(Optional.of(bag("sales")), decision.resolve(context, DEPARTMENT));
	}

	/** A resolver with a context key: the key of its call is known once the cache looked it up, and the set this decision saw serves over a newer one in the cache */
	@Test
	public void theSetOfADependentResolverServesTheDecisionOnceTheCacheResolvedItsKey()
	{
		reads.add(profile("admin", "sales"));
		AttributeResolverDescriptor profile = dependentProfile(3600);
		MapCache cache = new MapCache();
		PolicyInformationPoint point = point(profile, cache);
		PolicyInformationPoint decision = point.forDecision();

		assertEquals(Optional.of(bag("admin")), decision.resolve(context, ROLE));
		cache.putAttributes(ResolverContext.createContext(context, profile), AttributeSet.builder(profile).clock(clock)
				.attribute(ROLE.getAttributeId(), bag("admin")).attribute(DEPARTMENT.getAttributeId(), bag("support")).build());

		assertEquals(Optional.of(bag("sales")), decision.resolve(context, DEPARTMENT));
		assertEquals(Optional.of(bag("support")), point.forDecision().resolve(context, DEPARTMENT));
	}

	/** The context key of a resolver is resolved when the resolver asks, as without the memo (design D27): absence stays absence, and the next call asks again */
	@Test
	public void aDependencyAbsentThenFailedKeepsTheFirstLookupAbsenceAndTheLaterRetry()
	{
		when(context.resolve(SUBJECT_ID)).thenReturn(Optional.empty()).thenThrow(new IllegalStateException("subject store down"));
		reads.add(profile("admin", "sales"));
		PolicyInformationPoint decision = point(dependentProfile(3600)).forDecision();

		assertEquals(Optional.empty(), decision.resolve(context, ROLE));
		assertThrows(AttributeReferenceEvaluationException.class, () -> decision.resolve(context, ROLE));
	}

	/** A cache that fails after the decision kept a set does not take the answer the set has (review R-FABLE2-sol) */
	@Test
	public void theSetOfTheDecisionAnswersASiblingWhenTheCacheFails()
	{
		reads.add(profile("admin", "sales"));
		MapCache cache = new MapCache();
		PolicyInformationPoint point = point(profile(3600), cache);
		PolicyInformationPoint decision = point.forDecision();

		assertEquals(Optional.of(bag("admin")), decision.resolve(context, ROLE));
		cache.fail();

		assertEquals(Optional.of(bag("sales")), decision.resolve(context, DEPARTMENT));
		assertThrows(AttributeReferenceEvaluationException.class, () -> point.forDecision().resolve(context, DEPARTMENT));
	}

	/** Two refreshes of an expired partial set in flight at the same time: the attributes come from the refresh that won (review R-FABLE2-sol) */
	@Test
	public void overlappingRefreshesOfAnExpiredSetAnswerFromOneProfile() throws Exception
	{
		// When both refreshes are reading, the clock moves past the time to live of the kept set
		CyclicBarrier bothReading = new CyclicBarrier(2, () -> clock.advanceSeconds(2));
		AtomicInteger arrivals = new AtomicInteger();
		AttributeResolverDescriptor profile = profile(1, resolverContext -> {
			int arrival = arrivals.getAndIncrement();
			if(arrival == 0){
				return Map.of(ROLE.getAttributeId(), bag("admin"));
			}
			await(bothReading);
			return arrival == 1 ? profile("lead", "east", "manager") : profile("guest", "west", "agent");
		});
		PolicyInformationPoint decision = point(profile).forDecision();
		assertEquals(Optional.of(bag("admin")), decision.resolve(context, ROLE));
		ExecutorService pool = Executors.newFixedThreadPool(2);
		try{
			Future<Optional<BagOfValues>> department = pool.submit(() -> decision.resolve(context, DEPARTMENT));
			Future<Optional<BagOfValues>> title = pool.submit(() -> decision.resolve(context, TITLE));
			Optional<BagOfValues> resolvedDepartment = department.get(20, TimeUnit.SECONDS);
			Optional<BagOfValues> resolvedTitle = title.get(20, TimeUnit.SECONDS);

			boolean first = resolvedDepartment.equals(Optional.of(bag("east"))) && resolvedTitle.equals(Optional.of(bag("manager")));
			boolean second = resolvedDepartment.equals(Optional.of(bag("west"))) && resolvedTitle.equals(Optional.of(bag("agent")));
			assertTrue(first || second, "department " + resolvedDepartment + " and title " + resolvedTitle + " are not of one profile");
		}finally{
			pool.shutdownNow();
		}
	}

	/** A cached set that loses to the set a sibling lookup of the decision kept first serves nothing, and is no cache hit (review R-FABLE3-sol) */
	@Test
	public void aCachedSetThatLosesToASiblingLookupIsNoCacheHit() throws Exception
	{
		AttributeResolverDescriptor profile = profile(3600);
		reads.add(profile("guest", "support"));
		HeldCache cache = new HeldCache(AttributeSet.builder(profile).clock(clock).attributes(profile("admin", "sales").get()).build());
		ResolverEvents observer = new ResolverEvents();
		RootEvaluationContext root = new RootEvaluationContext(clock, false, Duration.ZERO, XPathVersion.XPATH1,
				Mockito.mock(PolicyReferenceResolver.class), Mockito.mock(EvaluationContextHandler.class), false,
				DecisionObservation.start(observer, "request", RequestContext.builder().build(), 1L, 1));
		PolicyInformationPoint point = point(profile, cache);
		PolicyInformationPoint decision = point.forDecision();
		ExecutorService pool = Executors.newSingleThreadExecutor();
		try{
			// The sibling lookup reads the fresh profile while the cached candidate is held, and keeps it first
			Future<Optional<BagOfValues>> department = pool.submit(() -> {
				await(cache.candidateTaken);
				try{
					return decision.resolve(root, DEPARTMENT);
				}finally{
					cache.siblingDone.countDown();
				}
			});
			Optional<BagOfValues> role = decision.resolve(root, ROLE);

			assertEquals(Optional.of(bag("guest")), role);
			assertEquals(Optional.of(bag("support")), department.get(20, TimeUnit.SECONDS));
			assertEquals(List.of("resolverStarted profile", "resolverFinished profile value"), observer.events);
		}finally{
			pool.shutdownNow();
		}
		// The set now in the cache serves a new decision, and that is a hit
		observer.events.clear();
		assertEquals(Optional.of(bag("guest")), point.forDecision().resolve(root, ROLE));
		assertEquals(List.of("resolverStarted profile", "resolverCacheHit profile", "resolverFinished profile value"), observer.events);
	}

	/** Two reads of one decision in flight at the same time: the attributes come from one of the profiles, not one from each */
	@Test
	public void overlappingReadsOfOneDecisionAnswerFromOneProfile() throws Exception
	{
		CyclicBarrier bothReading = new CyclicBarrier(2);
		AtomicInteger arrivals = new AtomicInteger();
		AttributeResolverDescriptor profile = profile(3600, resolverContext -> {
			int arrival = arrivals.getAndIncrement();
			await(bothReading);
			return arrival == 0 ? profile("admin", "sales").get() : profile("guest", "support").get();
		});
		PolicyInformationPoint decision = point(profile).forDecision();
		ExecutorService pool = Executors.newFixedThreadPool(2);
		try{
			Future<Optional<BagOfValues>> role = pool.submit(() -> decision.resolve(context, ROLE));
			Future<Optional<BagOfValues>> department = pool.submit(() -> decision.resolve(context, DEPARTMENT));
			Optional<BagOfValues> resolvedRole = role.get(20, TimeUnit.SECONDS);
			Optional<BagOfValues> resolvedDepartment = department.get(20, TimeUnit.SECONDS);

			boolean first = resolvedRole.equals(Optional.of(bag("admin"))) && resolvedDepartment.equals(Optional.of(bag("sales")));
			boolean second = resolvedRole.equals(Optional.of(bag("guest"))) && resolvedDepartment.equals(Optional.of(bag("support")));
			assertTrue(first || second, "role " + resolvedRole + " and department " + resolvedDepartment + " are not of one profile");
		}finally{
			pool.shutdownNow();
		}
	}

	private static void await(CyclicBarrier barrier)
	{
		try{
			barrier.await(10, TimeUnit.SECONDS);
		}catch(Exception e){
			throw new IllegalStateException("the other read did not arrive", e);
		}
	}

	private static void await(CountDownLatch latch)
	{
		try{
			if(!latch.await(10, TimeUnit.SECONDS)){
				throw new IllegalStateException("the other lookup did not arrive");
			}
		}catch(InterruptedException e){
			Thread.currentThread().interrupt();
			throw new IllegalStateException("interrupted while waiting for the other lookup", e);
		}
	}

	/** A resolver without context keys that answers the next read of the backend for the role and the department of a subject */
	private AttributeResolverDescriptor profile(int timeToLiveSeconds)
	{
		return profile(timeToLiveSeconds, resolverContext -> nextRead());
	}

	private AttributeResolverDescriptor profile(int timeToLiveSeconds, Function<ResolverContext, Map<String, BagOfValues>> read)
	{
		return AttributeResolverDescriptor.builder("profile", "profile", CategoryId.SUBJECT_ACCESS)
				.cache(timeToLiveSeconds)
				.attribute(ROLE.getAttributeId(), XacmlTypes.STRING)
				.attribute(DEPARTMENT.getAttributeId(), XacmlTypes.STRING)
				.attribute(TITLE.getAttributeId(), XacmlTypes.STRING)
				.build(read);
	}

	private static Map<String, BagOfValues> profile(String role, String department, String title)
	{
		return Map.of(ROLE.getAttributeId(), bag(role), DEPARTMENT.getAttributeId(), bag(department), TITLE.getAttributeId(), bag(title));
	}

	/** A resolver that reads the profile of the subject it finds in the context, and nothing when there is no subject */
	private AttributeResolverDescriptor dependentProfile(int timeToLiveSeconds)
	{
		return AttributeResolverDescriptor.builder("profile", "profile", CategoryId.SUBJECT_ACCESS)
				.cache(timeToLiveSeconds).contextRef(SUBJECT_ID)
				.attribute(ROLE.getAttributeId(), XacmlTypes.STRING)
				.attribute(DEPARTMENT.getAttributeId(), XacmlTypes.STRING)
				.build(resolverContext -> resolverContext.resolveContextRef(SUBJECT_ID).isPresent() ? nextRead() : Map.of());
	}

	private Map<String, BagOfValues> nextRead()
	{
		Supplier<Map<String, BagOfValues>> read = reads.poll();
		if(read == null){
			throw new IllegalStateException("no read left");
		}
		return read.get();
	}

	private static PolicyInformationPoint point(AttributeResolverDescriptor resolver)
	{
		return PolicyInformationPoint.builder("profiles")
				.registry(ResolverRegistry.builder().withAttributeResolver(resolver).build())
				.build();
	}

	private static PolicyInformationPoint point(AttributeResolverDescriptor resolver, PolicyInformationPointCacheProvider cache)
	{
		return PolicyInformationPoint.builder("profiles")
				.registry(ResolverRegistry.builder().withAttributeResolver(resolver).build())
				.cacheProvider(cache)
				.build();
	}

	private static Supplier<Map<String, BagOfValues>> profile(String role, String department)
	{
		return () -> Map.of(ROLE.getAttributeId(), bag(role), DEPARTMENT.getAttributeId(), bag(department));
	}

	private static AttributeDesignatorKey key(String attributeId)
	{
		return AttributeDesignatorKey.builder().category(CategoryId.SUBJECT_ACCESS)
				.attributeId(attributeId).dataType(XacmlTypes.STRING).build();
	}

	private static BagOfValues bag(String value)
	{
		return XacmlTypes.STRING.ofAny(value).toBag();
	}

	/** A cache provider over a map, keyed by the resolver call; it can be made to fail */
	private static final class MapCache extends BasePolicyInformationPointCacheProvider
	{
		private final Map<ResolverCacheKey, AttributeSet> attributes = new HashMap<>();
		private volatile boolean failing;

		void fail()
		{
			failing = true;
		}

		@Override
		protected ContentRef doGetContent(ResolverContext context)
		{
			return null;
		}

		@Override
		protected AttributeSet doGetAttributes(ResolverContext context)
		{
			if(failing){
				throw new IllegalStateException("cache unavailable");
			}
			return attributes.get(context.getCacheKey());
		}

		@Override
		protected void doPutContent(ResolverContext context, ContentRef content)
		{
		}

		@Override
		protected void doPutAttributes(ResolverContext context, AttributeSet v)
		{
			attributes.put(context.getCacheKey(), v);
		}
	}

	/**
	 * A cache provider that holds its first lookup, with the candidate ready, until the sibling
	 * lookup is done; every later lookup reads the map the puts fill
	 */
	private static final class HeldCache extends BasePolicyInformationPointCacheProvider
	{
		final CountDownLatch candidateTaken = new CountDownLatch(1);
		final CountDownLatch siblingDone = new CountDownLatch(1);
		private final AttributeSet candidate;
		private final Map<ResolverCacheKey, AttributeSet> attributes = new ConcurrentHashMap<>();
		private final AtomicBoolean first = new AtomicBoolean(true);

		HeldCache(AttributeSet candidate)
		{
			this.candidate = candidate;
		}

		@Override
		protected ContentRef doGetContent(ResolverContext context)
		{
			return null;
		}

		@Override
		protected AttributeSet doGetAttributes(ResolverContext context)
		{
			if(first.compareAndSet(true, false)){
				candidateTaken.countDown();
				await(siblingDone);
				return candidate;
			}
			return attributes.get(context.getCacheKey());
		}

		@Override
		protected void doPutContent(ResolverContext context, ContentRef content)
		{
		}

		@Override
		protected void doPutAttributes(ResolverContext context, AttributeSet v)
		{
			attributes.put(context.getCacheKey(), v);
		}
	}

	/** Records the resolver callbacks of the deciding thread */
	private static final class ResolverEvents implements DecisionObserver<String>
	{
		final List<String> events = new ArrayList<>();

		@Override
		public String requestStarted(PolicyDecisionPoint pdp, RequestContext request)
		{
			return "request";
		}

		@Override
		public String resolverStarted(String parent, EvaluationContext context, String resolverId, AttributeReferenceKey key)
		{
			events.add("resolverStarted " + resolverId);
			return "resolver";
		}

		@Override
		public void resolverCacheHit(String resolver, EvaluationContext context, String resolverId, AttributeReferenceKey key)
		{
			events.add("resolverCacheHit " + resolverId);
		}

		@Override
		public void resolverFinished(String resolver, EvaluationContext context, String resolverId, AttributeReferenceKey key,
				Optional<BagOfValues> value)
		{
			events.add("resolverFinished " + resolverId + (value.isPresent() ? " value" : " absent"));
		}

		@Override
		public void resolverFailed(String resolver, EvaluationContext context, String resolverId, AttributeReferenceKey key,
				Throwable failure)
		{
			events.add("resolverFailed " + resolverId);
		}
	}

	/** A clock that the test moves forward */
	private static final class TestClock extends Clock
	{
		private Instant now = Instant.parse("2026-10-27T10:00:00Z");

		void advanceSeconds(long seconds)
		{
			now = now.plusSeconds(seconds);
		}

		@Override
		public ZoneId getZone()
		{
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zone)
		{
			return this;
		}

		@Override
		public Instant instant()
		{
			return now;
		}
	}
}
