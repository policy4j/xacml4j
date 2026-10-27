package org.xacml4j.v30.spi.repository;

/*
 * #%L
 * Xacml4J Policy Decision Point
 * %%
 * Copyright (C) 2009 - 2026 Xacml4J.org
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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.xacml4j.v30.VersionMatch;
import org.xacml4j.v30.policy.DecisionCombiningAlgorithm;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicyIDReference;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.policy.PolicySetIDReference;

/**
 * Today's reference cache, which cycle F of change optimize-pdp-evaluation must keep (design
 * D10): a hit does not ask the repository, a matching change of the repository empties the
 * entry, a disabled cache asks every time, and concurrent first resolutions all get the policy.
 */
public class DefaultPolicyReferenceResolverCacheTest
{
	private PolicyRepository repository;
	private Policy p1v1;
	private PolicySet ps1v1;

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void init() throws Exception
	{
		repository = Mockito.mock(PolicyRepository.class);
		p1v1 = Policy.builder("id").version("1.0.0")
				.combiningAlgorithm(Mockito.mock(DecisionCombiningAlgorithm.class)).build();
		ps1v1 = PolicySet.builder("id").version("1.0.0")
				.withCombiningAlgorithm(Mockito.mock(DecisionCombiningAlgorithm.class)).build();
		when(repository.getPolicy("id", new VersionMatch("1.0.0"), null, null)).thenReturn(p1v1);
		when(repository.getPolicySet("id", new VersionMatch("1.0.0"), null, null)).thenReturn(ps1v1);
	}

	@Test
	public void hitsDoNotAskTheRepositoryAndAMatchingChangeEmptiesTheEntry() throws Exception
	{
		DefaultPolicyReferenceResolver r = new DefaultPolicyReferenceResolver(repository);
		PolicyIDReference policyRef = PolicyIDReference.builder("id").versionAsString("1.0.0").build();
		PolicySetIDReference setRef = PolicySetIDReference.builder("id").versionAsString("1.0.0").build();

		for(int i = 0; i < 3; i++){
			assertSame(p1v1, r.resolve(policyRef));
			assertSame(ps1v1, r.resolve(setRef));
		}
		verify(repository, times(1)).getPolicy("id", new VersionMatch("1.0.0"), null, null);
		verify(repository, times(1)).getPolicySet("id", new VersionMatch("1.0.0"), null, null);

		r.policyAdded(p1v1);
		r.policySetAdded(ps1v1);
		assertSame(p1v1, r.resolve(policyRef));
		assertSame(ps1v1, r.resolve(setRef));
		verify(repository, times(2)).getPolicy("id", new VersionMatch("1.0.0"), null, null);
		verify(repository, times(2)).getPolicySet("id", new VersionMatch("1.0.0"), null, null);
	}

	@Test
	public void disabledCacheAsksTheRepositoryEveryTime() throws Exception
	{
		DefaultPolicyReferenceResolver r = new DefaultPolicyReferenceResolver(repository, false, 16);
		PolicyIDReference policyRef = PolicyIDReference.builder("id").versionAsString("1.0.0").build();

		assertSame(p1v1, r.resolve(policyRef));
		assertSame(p1v1, r.resolve(policyRef));
		verify(repository, times(2)).getPolicy("id", new VersionMatch("1.0.0"), null, null);
	}

	@Test
	public void concurrentFirstResolutionsAllGetThePolicy() throws Exception
	{
		DefaultPolicyReferenceResolver r = new DefaultPolicyReferenceResolver(repository);
		PolicyIDReference policyRef = PolicyIDReference.builder("id").versionAsString("1.0.0").build();
		int threads = 8;
		ExecutorService pool = Executors.newFixedThreadPool(threads);
		try{
			CyclicBarrier start = new CyclicBarrier(threads);
			List<Future<Policy>> found = new ArrayList<>();
			for(int t = 0; t < threads; t++){
				found.add(pool.submit(() -> {
					start.await(10, TimeUnit.SECONDS);
					return r.resolve(policyRef);
				}));
			}
			for(Future<Policy> f : found){
				assertSame(p1v1, f.get(20, TimeUnit.SECONDS));
			}
			assertSame(p1v1, r.resolve(policyRef));
		}finally{
			pool.shutdownNow();
		}
	}

	/**
	 * Review RVC2: a removal that arrives while a lookup of the same reference is running. Today
	 * (before and after cycle F) the lookup's late put caches the removed policy, and the next
	 * resolution returns it without asking the repository. This pins the pre-existing race so the
	 * cycle cannot change it silently; fixing it is a separate bug.
	 */
	@Test
	public void removalDuringALookupLeavesTheLookedUpPolicyCached() throws Exception
	{
		java.util.concurrent.CountDownLatch inLookup = new java.util.concurrent.CountDownLatch(1);
		java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
		// The repository's content: the policy until it is removed
		java.util.concurrent.atomic.AtomicReference<Policy> stored = new java.util.concurrent.atomic.AtomicReference<>(p1v1);
		when(repository.getPolicy("id", new VersionMatch("1.0.0"), null, null)).thenAnswer(invocation -> {
			Policy found = stored.get();
			inLookup.countDown();
			if(!release.await(10, TimeUnit.SECONDS)){
				throw new IllegalStateException("lookup not released");
			}
			return found;
		});
		DefaultPolicyReferenceResolver r = new DefaultPolicyReferenceResolver(repository);
		PolicyIDReference policyRef = PolicyIDReference.builder("id").versionAsString("1.0.0").build();
		ExecutorService pool = Executors.newSingleThreadExecutor();
		try{
			Future<Policy> lookup = pool.submit(() -> r.resolve(policyRef));
			org.junit.jupiter.api.Assertions.assertTrue(inLookup.await(10, TimeUnit.SECONDS), "the lookup started");
			stored.set(null);
			r.policyRemoved(p1v1);
			release.countDown();

			assertSame(p1v1, lookup.get(20, TimeUnit.SECONDS));
			org.junit.jupiter.api.Assertions.assertNull(stored.get(), "the repository no longer has the policy");
			assertSame(p1v1, r.resolve(policyRef));
			verify(repository, times(1)).getPolicy("id", new VersionMatch("1.0.0"), null, null);
		}finally{
			pool.shutdownNow();
		}
	}
}
