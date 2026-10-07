package org.xacml4j.v30.spi.repository;

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


import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.xacml4j.v30.CompositeDecisionRule;
import org.xacml4j.v30.Version;
import org.xacml4j.v30.VersionMatch;
import org.xacml4j.v30.marshal.PolicyUnmarshaller;
import org.xacml4j.v30.policy.DecisionCombiningAlgorithm;
import org.xacml4j.v30.policy.Policy;
import org.xacml4j.v30.policy.PolicySet;
import org.xacml4j.v30.policy.Rule;
import org.xacml4j.v30.policy.combine.DecisionCombiningAlgorithmProvider;
import org.xacml4j.v30.policy.function.FunctionProvider;


public class InMemoryPolicyRepositoryTest
{
	private Policy p1v1;
	private Policy p1v2;
	private Policy p1v2DiffInstance;
	private Policy p1v3;
	private Policy p1v4;

	private PolicyRepository r;
	private DecisionCombiningAlgorithm<Rule> algorithm;

	private PolicyRepositoryListener l;

	private FunctionProvider functions;

	private PolicyUnmarshaller unmarshaller;

	private DecisionCombiningAlgorithmProvider decisionAlgorithms;

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void init() throws Exception
	{
		this.algorithm = Mockito.mock(DecisionCombiningAlgorithm.class);
		this.functions = Mockito.mock(FunctionProvider.class);
		this.unmarshaller = Mockito.mock(PolicyUnmarshaller.class);
		this.decisionAlgorithms = Mockito.mock(DecisionCombiningAlgorithmProvider.class);

		this.p1v1 = Policy.builder("id1").version("1").combiningAlgorithm(algorithm).build();
		this.p1v2 = Policy.builder("id1").version("1.1").combiningAlgorithm(algorithm).build();
		this.p1v2DiffInstance =  Policy.builder("id1").version("1.1").combiningAlgorithm(algorithm).build();
		this.p1v3 =  Policy.builder("id1").version("1.2.1").combiningAlgorithm(algorithm).build();
		this.p1v4 =  Policy.builder("id1").version("2.0.1").combiningAlgorithm(algorithm).build();
		this.r = new InMemoryPolicyRepository("testId", functions, decisionAlgorithms);
		this.l = Mockito.mock(PolicyRepositoryListener.class);
	}

	private CountDownLatch listenerCallbacks(int expected)
	{
		final CountDownLatch callbacks = new CountDownLatch(expected);
		r.addPolicyRepositoryListener(new PolicyRepositoryListener(){
			@Override
			public void policyAdded(Policy p){
				l.policyAdded(p);
				callbacks.countDown();
			}
			@Override
			public void policyRemoved(Policy p){
				l.policyRemoved(p);
				callbacks.countDown();
			}
			@Override
			public void policySetAdded(PolicySet p){
				l.policySetAdded(p);
				callbacks.countDown();
			}
			@Override
			public void policySetRemoved(PolicySet p){
				l.policySetRemoved(p);
				callbacks.countDown();
			}
		});
		return callbacks;
	}

	@Test
	public void testOnePolicyDifferentVersion() throws Exception
	{

		when(algorithm.getId()).thenReturn("testId");
		when(decisionAlgorithms.isRuleAlgorithmProvided("testId")).thenReturn(true);
		when(algorithm.getId()).thenReturn("testId");
		when(decisionAlgorithms.isRuleAlgorithmProvided("testId")).thenReturn(true);
		when(algorithm.getId()).thenReturn("testId");
		when(decisionAlgorithms.isRuleAlgorithmProvided("testId")).thenReturn(true);
		when(algorithm.getId()).thenReturn("testId");
		when(decisionAlgorithms.isRuleAlgorithmProvided("testId")).thenReturn(true);

		CountDownLatch callbacks = listenerCallbacks(4);

		r.add(p1v1);
		r.add(p1v2);
		r.add(p1v3);
		r.add(p1v4);

		CompositeDecisionRule p = r.get("id1", Version.parse("1.0"));
		assertEquals(p1v1, p);
		p = r.get("id1", Version.parse("1.1"));
		assertEquals(p1v2, p);
		p = r.get("id1", Version.parse("1.2.1"));
		assertEquals(p1v3, p);
		p = r.get("id1", Version.parse("2.0.1"));
		assertEquals(p1v4, p);

		Collection<Policy> found = r.getPolicies("id1", VersionMatch.parse("1.+"));
		assertEquals(3, found.size());
		Iterator<Policy> it = found.iterator();

		// sorted by version
		assertEquals(Version.parse("1"), it.next().getVersion());
		assertEquals(Version.parse("1.1"), it.next().getVersion());
		assertEquals(Version.parse("1.2.1"), it.next().getVersion());

		found = r.getPolicies("id1", VersionMatch.parse("1.2.+"));
		assertEquals(1, found.size());

		found = r.getPolicies("id1", VersionMatch.parse("1.+"), VersionMatch.parse("1.2.*"));
		assertEquals(1, found.size());

		Policy policy = r.getPolicy("id1", null, null, null);
		assertEquals(Version.parse("2.0.1"), policy.getVersion());

		assertTrue(callbacks.await(5, TimeUnit.SECONDS), "Timed out waiting for four policy-added callbacks");
		Mockito.verify(algorithm, times(4)).getId();
		Mockito.verify(decisionAlgorithms, times(4)).isRuleAlgorithmProvided("testId");
		verify(l).policyAdded(p1v1);
		verify(l).policyAdded(p1v2);
		verify(l).policyAdded(p1v3);
		verify(l).policyAdded(p1v4);
		Mockito.verifyNoMoreInteractions(l);
	}

	@Test
	public void testAddRemove() throws Exception
	{
		when(algorithm.getId()).thenReturn("testId");
		when(decisionAlgorithms.isRuleAlgorithmProvided("testId")).thenReturn(true);
		CountDownLatch callbacks = listenerCallbacks(2);

		r.add(p1v2);

		CompositeDecisionRule p = r.get("id1", Version.parse("1.0"));
		assertNull(p);
		p = r.get("id1", Version.parse("1.1"));
		assertEquals(p1v2, p);
		r.remove(p1v2);

		Collection<Policy> found = r.getPolicies("id1", VersionMatch.parse("1.+"));
		assertEquals(0, found.size());

		assertTrue(callbacks.await(5, TimeUnit.SECONDS), "Timed out waiting for policy-added and policy-removed callbacks");
		Mockito.verify(algorithm).getId();
		Mockito.verify(decisionAlgorithms).isRuleAlgorithmProvided("testId");
		verify(l).policyAdded(p1v2);
		verify(l).policyRemoved(p1v2);
		Mockito.verifyNoMoreInteractions(l);
	}

	@Test
	public void testAddRemovePolicySetNotifiesMatchingCallbacks() throws Exception
	{
		@SuppressWarnings("unchecked")
		DecisionCombiningAlgorithm<CompositeDecisionRule> policySetAlgorithm = Mockito.mock(DecisionCombiningAlgorithm.class);
		PolicySet policySet = PolicySet.builder("set1")
				.version("1.0")
				.withCombiningAlgorithm(policySetAlgorithm)
				.build();
		when(policySetAlgorithm.getId()).thenReturn("testId");
		when(decisionAlgorithms.isPolicyAlgorithmProvided("testId")).thenReturn(true);
		CountDownLatch callbacks = listenerCallbacks(2);

		r.add(policySet);
		r.remove(policySet);

		assertTrue(callbacks.await(5, TimeUnit.SECONDS), "Timed out waiting for policy-set-added and policy-set-removed callbacks");
		verify(l, times(1)).policySetAdded(policySet);
		verify(l, times(1)).policySetRemoved(policySet);
		Mockito.verifyNoMoreInteractions(l);
	}

	@Test
	public void testAddPolicyWithTheSameIdAndSameVersion() throws Exception
	{
		when(algorithm.getId()).thenReturn("testId");
		when(decisionAlgorithms.isRuleAlgorithmProvided("testId")).thenReturn(true);
		when(algorithm.getId()).thenReturn("testId");
		when(decisionAlgorithms.isRuleAlgorithmProvided("testId")).thenReturn(true);
		CountDownLatch callbacks = listenerCallbacks(1);
		assertTrue(r.add(p1v2));
		assertFalse(r.add(p1v2DiffInstance));
		Policy p = (Policy)r.get("id1", Version.parse("1.1"));
		assertSame(p1v2, p);
		assertNotSame(p1v2DiffInstance, p);
		assertTrue(callbacks.await(5, TimeUnit.SECONDS), "Timed out waiting for one policy-added callback");
		Mockito.verify(algorithm, times(2)).getId();
		Mockito.verify(decisionAlgorithms, times(2)).isRuleAlgorithmProvided("testId");
		verify(l).policyAdded(p1v2);
		Mockito.verifyNoMoreInteractions(l);
	}


	@Test
	public void testFindAllPoliciesWithTheSameId() throws Exception
	{
		when(algorithm.getId()).thenReturn("testId");
		when(decisionAlgorithms.isRuleAlgorithmProvided("testId")).thenReturn(true);
		when(algorithm.getId()).thenReturn("testId");
		when(decisionAlgorithms.isRuleAlgorithmProvided("testId")).thenReturn(true);
		when(algorithm.getId()).thenReturn("testId");
		when(decisionAlgorithms.isRuleAlgorithmProvided("testId")).thenReturn(true);
		when(algorithm.getId()).thenReturn("testId");
		when(decisionAlgorithms.isRuleAlgorithmProvided("testId")).thenReturn(true);

		CountDownLatch callbacks = listenerCallbacks(4);

		r.add(p1v2);
		r.add(p1v1);
		r.add(p1v3);
		r.add(p1v4);

		Collection<Policy> found = r.getPolicies("id1", null, null, null);
		assertEquals(4, found.size());
		Iterator<Policy> it = found.iterator();

		assertEquals(Version.parse("1"), it.next().getVersion());
		assertEquals(Version.parse("1.1"), it.next().getVersion());
		assertEquals(Version.parse("1.2.1"), it.next().getVersion());
		assertEquals(Version.parse("2.0.1"), it.next().getVersion());

		assertTrue(callbacks.await(5, TimeUnit.SECONDS), "Timed out waiting for four policy-added callbacks");
		Mockito.verify(algorithm, times(4)).getId();
		Mockito.verify(decisionAlgorithms, times(4)).isRuleAlgorithmProvided("testId");
		verify(l).policyAdded(p1v2);
		verify(l).policyAdded(p1v1);
		verify(l).policyAdded(p1v3);
		verify(l).policyAdded(p1v4);
		Mockito.verifyNoMoreInteractions(l);
	}
}
