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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;
import org.xacml4j.v30.AttributeDesignatorKey;
import org.xacml4j.v30.AttributeReferenceEvaluationException;
import org.xacml4j.v30.BagOfValues;
import org.xacml4j.v30.CategoryId;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.StatusCodeId;
import org.xacml4j.v30.types.XacmlTypes;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Uninterruptibles;

public class ParallelAttributeResolverTest
{
	private static final Duration TIMEOUT = Duration.ofMillis(500);
	private static final Duration HANG = Duration.ofSeconds(10);
	private static final AttributeDesignatorKey ROLE = key("role");
	private static final AttributeDesignatorKey DEPARTMENT = key("department");
	private static final AttributeDesignatorKey USERNAME = key("username");
	private static final BagOfValues ADMIN = XacmlTypes.STRING.bagOf(XacmlTypes.STRING.ofAny("admin"));
	private static final BagOfValues GUEST = XacmlTypes.STRING.bagOf(XacmlTypes.STRING.ofAny("guest"));
	private static final BagOfValues SALES = XacmlTypes.STRING.bagOf(XacmlTypes.STRING.ofAny("sales"));
	private static final BagOfValues ALICE = XacmlTypes.STRING.bagOf(XacmlTypes.STRING.ofAny("alice"));
	private static final Function<ResolverContext, BagOfValues> FAILING = c->{
		throw new IllegalStateException("back end down");
	};

	private EvaluationContext context;

	@BeforeEach
	public void init(){
		this.context = Mockito.mock(EvaluationContext.class);
		when(context.getClock()).thenReturn(Clock.systemUTC());
	}

	@Test
	public void fallbackAndEmptyBagAreAsForSynchronousResolvers(){
		for(boolean parallel : new boolean[]{false, true}){
			assertEquals(Optional.of(ADMIN),
			             pip(role("r1", parallel, c->null), role("r2", parallel, c->ADMIN)).resolve(context, ROLE));
			assertEquals(Optional.of(ADMIN),
			             pip(role("r1", parallel, FAILING), role("r2", parallel, c->ADMIN)).resolve(context, ROLE));
			assertEquals(Optional.of(XacmlTypes.STRING.emptyBag()),
			             pip(role("r1", parallel, c->XacmlTypes.STRING.emptyBag()), role("r2", parallel, c->ADMIN))
					             .resolve(context, ROLE));
			assertResolutionFailure(
					()->pip(role("r1", parallel, FAILING), role("r2", parallel, c->null)).resolve(context, ROLE));
		}
	}

	@Test
	public void aHungResolverDoesNotHoldUpTheNextOne(){
		CountDownLatch release = new CountDownLatch(1);
		PolicyInformationPoint pip = pip(
				role("hung", true, c->{
					// A back end that ignores its interruption
					Uninterruptibles.awaitUninterruptibly(release, HANG);
					return SALES;
				}),
				role("r2", true, c->ADMIN));
		try{
			long start = System.nanoTime();
			assertEquals(Optional.of(ADMIN), pip.resolve(context, ROLE));
			assertTrue(System.nanoTime() - start < TIMEOUT.plusSeconds(2).toNanos());
		}finally{
			release.countDown();
		}
	}

	@Test
	public void twoAttributesOfOneParallelResolverGetTheirBags(){
		AttributeResolverDescriptor people = AttributeResolverDescriptor
				.builder("people", "People", CategoryId.SUBJECT_ACCESS)
				.noCache()
				.attribute("role", XacmlTypes.STRING, c->ADMIN)
				.attribute("department", XacmlTypes.STRING, c->SALES)
				.buildParallel();
		PolicyInformationPoint pip = pip(people);
		assertEquals(Optional.of(ADMIN), pip.resolve(context, ROLE));
		assertEquals(Optional.of(SALES), pip.resolve(context, DEPARTMENT));
		AttributeSet set = people.getResolverFunction()
		                         .apply(ResolverContext.createContext(context, people))
		                         .orElseThrow();
		assertEquals(Optional.of(ADMIN), set.get("role"));
		assertEquals(Optional.of(SALES), set.get("department"));
	}

	@Test
	public void aFailedAttributeFunctionFailsOnlyItsAttribute(){
		PolicyInformationPoint pip = pip(AttributeResolverDescriptor
				.builder("people", "People", CategoryId.SUBJECT_ACCESS)
				.noCache()
				.attribute("role", XacmlTypes.STRING, c->ADMIN)
				.attribute("department", XacmlTypes.STRING, FAILING)
				.buildParallel());
		assertEquals(Optional.of(ADMIN), pip.resolve(context, ROLE));
		assertResolutionFailure(()->pip.resolve(context, DEPARTMENT));
	}

	@Test
	public void contextKeysAreResolvedOnTheDecidingThread(){
		Thread deciding = Thread.currentThread();
		when(context.resolve(USERNAME)).thenAnswer(i->Thread.currentThread() == deciding? Optional.of(ALICE): Optional.empty());
		PolicyInformationPoint pip = pip(AttributeResolverDescriptor
				.builder("people", "People", CategoryId.SUBJECT_ACCESS)
				.noCache()
				.contextRef(USERNAME)
				.attribute("role", XacmlTypes.STRING, c->c.resolveContextRef(USERNAME)
				                                          .filter(ALICE::equals)
				                                          .map(user->ADMIN)
				                                          .orElseThrow())
				.buildParallel());
		assertEquals(Optional.of(ADMIN), pip.resolve(context, ROLE));
	}

	@Test
	public void aFunctionThatReadsTheEvaluationContextIsAProcessingError(){
		PolicyInformationPoint pip = pip(AttributeResolverDescriptor
				.builder("people", "People", CategoryId.SUBJECT_ACCESS)
				.noCache()
				.attribute("role", XacmlTypes.STRING, c->{
					c.getEvaluationContext();
					return ADMIN;
				})
				.buildParallel());
		assertResolutionFailure(()->pip.resolve(context, ROLE));
	}

	@Test
	public void onlyACompleteSetIsServedFromTheCache(){
		AtomicReference<BagOfValues> backend = new AtomicReference<>(ADMIN);
		PolicyInformationPoint complete = cachingPip(people(c->backend.get(), c->SALES));
		PolicyInformationPoint partial = cachingPip(people(c->backend.get(), FAILING));
		assertEquals(Optional.of(ADMIN), complete.resolve(decision(), ROLE));
		assertEquals(Optional.of(ADMIN), partial.resolve(decision(), ROLE));
		backend.set(GUEST);
		assertEquals(Optional.of(ADMIN), complete.resolve(decision(), ROLE));
		assertEquals(Optional.of(GUEST), partial.resolve(decision(), ROLE));
	}

	@Test
	public void maxConcurrentResolutionsBoundsTheFunctionsInFlight(){
		AtomicInteger inFlight = new AtomicInteger();
		AtomicInteger most = new AtomicInteger();
		Function<ResolverContext, BagOfValues> busy = c->{
			most.accumulateAndGet(inFlight.incrementAndGet(), Math::max);
			Uninterruptibles.sleepUninterruptibly(Duration.ofMillis(20));
			inFlight.decrementAndGet();
			return ADMIN;
		};
		AttributeResolverDescriptor.Builder b = AttributeResolverDescriptor.builder("busy", "Busy", CategoryId.SUBJECT_ACCESS)
		                                                                   .noCache();
		for(int i = 0; i < 4; i++){
			b.attribute("a" + i, XacmlTypes.STRING, busy);
		}
		PolicyInformationPoint pip = PolicyInformationPoint.builder("pip")
		                                                   .registry(ResolverRegistry.builder().withAttributeResolver(b.buildParallel()).build())
		                                                   .maxConcurrentResolutions(1)
		                                                   .build();
		assertEquals(Optional.of(ADMIN), pip.resolve(context, key("a0")));
		assertEquals(1, most.get());
	}

	private static void assertResolutionFailure(Executable resolution){
		AttributeReferenceEvaluationException e = assertThrows(AttributeReferenceEvaluationException.class, resolution);
		assertEquals(StatusCodeId.STATUS_PROCESSING_ERROR, e.getStatus().getStatusCode().getValue());
		assertEquals(Optional.of(AttributeReferenceEvaluationException.RESOLUTION_FAILED_MESSAGE),
		             e.getStatus().getMessage());
	}

	private static PolicyInformationPoint pip(AttributeResolverDescriptor ... resolvers){
		ResolverRegistry.Builder registry = ResolverRegistry.builder();
		for(AttributeResolverDescriptor d : resolvers){
			registry.withAttributeResolver(d);
		}
		return PolicyInformationPoint.builder("pip")
		                             .registry(registry.build())
		                             .resolverTimeout(TIMEOUT)
		                             .build();
	}

	/** A resolver of the attribute "role", built as a parallel or as a synchronous resolver */
	private static AttributeResolverDescriptor role(String id, boolean parallel,
	                                                Function<ResolverContext, BagOfValues> role){
		AttributeResolverDescriptor.Builder b = AttributeResolverDescriptor.builder(id, id, CategoryId.SUBJECT_ACCESS)
		                                                                   .noCache();
		if(parallel){
			return b.attribute("role", XacmlTypes.STRING, role).buildParallel();
		}
		return b.attribute("role", XacmlTypes.STRING)
		        .build(c->{
			        BagOfValues v = role.apply(c);
			        return v == null? ImmutableMap.of(): ImmutableMap.of("role", v);
		        });
	}

	/** A cached parallel resolver of "role" and "department" */
	private static AttributeResolverDescriptor people(Function<ResolverContext, BagOfValues> role,
	                                                  Function<ResolverContext, BagOfValues> department){
		return AttributeResolverDescriptor.builder("people", "People", CategoryId.SUBJECT_ACCESS)
		                                  .cache(60)
		                                  .attribute("role", XacmlTypes.STRING, role)
		                                  .attribute("department", XacmlTypes.STRING, department)
		                                  .buildParallel();
	}

	private static PolicyInformationPoint cachingPip(AttributeResolverDescriptor resolver){
		return PolicyInformationPoint.builder("pip")
		                             .registry(ResolverRegistry.builder().withAttributeResolver(resolver).build())
		                             .cacheProvider(new MapCache())
		                             .build();
	}

	/** The evaluation context of a new decision */
	private static EvaluationContext decision(){
		EvaluationContext context = Mockito.mock(EvaluationContext.class);
		when(context.getClock()).thenReturn(Clock.systemUTC());
		return context;
	}

	private static AttributeDesignatorKey key(String attributeId){
		return AttributeDesignatorKey.builder()
		                             .category(CategoryId.SUBJECT_ACCESS)
		                             .attributeId(attributeId)
		                             .dataType(XacmlTypes.STRING)
		                             .build();
	}

	/** A cache of attribute sets in a map, shared by the decisions of one information point */
	private static final class MapCache extends BasePolicyInformationPointCacheProvider
	{
		private final Map<ResolverCacheKey, AttributeSet> attributes = new ConcurrentHashMap<>();

		@Override
		protected ContentRef doGetContent(ResolverContext context) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected AttributeSet doGetAttributes(ResolverContext context) {
			return attributes.get(context.getCacheKey());
		}

		@Override
		protected void doPutContent(ResolverContext context, ContentRef content) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected void doPutAttributes(ResolverContext context, AttributeSet v) {
			attributes.put(context.getCacheKey(), v);
		}
	}
}
