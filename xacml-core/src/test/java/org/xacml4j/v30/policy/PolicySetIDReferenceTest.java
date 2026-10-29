package org.xacml4j.v30.policy;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xacml4j.v30.CoreException;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.PolicyResolutionException;
import org.xacml4j.v30.Version;
import org.xacml4j.v30.VersionMatch;

@ExtendWith(MockitoExtension.class)
public class PolicySetIDReferenceTest
{
	private EvaluationContext context;
	private PolicySet policySet;
	private PolicySet refPolicySet;
	private DecisionCombiningAlgorithm<org.xacml4j.v30.CompositeDecisionRule> algorithm;

	@BeforeEach
	public void init()
	{
		context = mock(EvaluationContext.class);
		algorithm = mock(DecisionCombiningAlgorithm.class);
		policySet = PolicySet.builder("otherTestId").version("1.0").withCombiningAlgorithm(algorithm).build();
		refPolicySet = PolicySet.builder("testId").version("1.0").withCombiningAlgorithm(algorithm).build();
	}

	@Test
	public void testEvaluateFailToResolveReferenceViaException() throws CoreException
	{
		PolicySetIDReference ref = PolicySetIDReference.builder("testId").versionAsString("1.+").build();
		when(context.getCurrentPolicySetIDReference()).thenReturn(null);
		when(context.resolve(ref)).thenThrow(new PolicyResolutionException(null));
		InOrder order = inOrder(context, algorithm);
		EvaluationContext policyRefContext = ref.createContext(context);
		assertSame(ref, policyRefContext.getCurrentPolicySetIDReference());
		order.verify(context, times(2)).getCurrentPolicySetIDReference();
		order.verify(context).resolve(ref);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(context, times(2)).getCurrentPolicySetIDReference();
		verify(context, times(1)).resolve(ref);
		verifyNoMoreInteractions(algorithm);
		when(context.getCurrentPolicySet()).thenReturn(policySet);
		assertEquals("otherTestId", policySet.getId());
		assertEquals(Version.parse("1.0"), policySet.getVersion());
		assertEquals(Decision.INDETERMINATE, ref.evaluate(policyRefContext));
		order.verify(context).getCurrentPolicySet();
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(context, times(2)).getCurrentPolicySetIDReference();
		verify(context, times(1)).resolve(ref);
		verify(context, times(1)).getCurrentPolicySet();
		verifyNoMoreInteractions(algorithm);
	}

	@Test
	public void testEvaluateFailToResolveReferenceReturnsNull() throws CoreException
	{
		PolicySetIDReference ref = PolicySetIDReference.builder("testId").versionAsString("1.+").build();
		when(context.getCurrentPolicySetIDReference()).thenReturn(null);
		when(context.resolve(ref)).thenReturn(null);
		InOrder order = inOrder(context, algorithm);
		EvaluationContext policyRefContext = ref.createContext(context);
		assertSame(ref, policyRefContext.getCurrentPolicySetIDReference());
		order.verify(context, times(2)).getCurrentPolicySetIDReference();
		order.verify(context).resolve(ref);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(context, times(2)).getCurrentPolicySetIDReference();
		verify(context, times(1)).resolve(ref);
		verifyNoMoreInteractions(algorithm);
		when(context.getCurrentPolicySet()).thenReturn(policySet);
		assertEquals("otherTestId", policySet.getId());
		assertEquals(Version.parse("1.0"), policySet.getVersion());
		assertEquals(Decision.INDETERMINATE, ref.evaluate(policyRefContext));
		order.verify(context).getCurrentPolicySet();
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(context, times(2)).getCurrentPolicySetIDReference();
		verify(context, times(1)).resolve(ref);
		verify(context, times(1)).getCurrentPolicySet();
		verifyNoMoreInteractions(algorithm);
	}

	@Test
	public void testIsApplicableFailToResolveReference() throws CoreException
	{
		PolicySetIDReference ref = PolicySetIDReference.builder("testId").versionAsString("1.+").build();
		when(context.getCurrentPolicySetIDReference()).thenReturn(null);
		when(context.resolve(ref)).thenThrow(new PolicyResolutionException(null));
		InOrder order = inOrder(context, algorithm);
		EvaluationContext policyRefContext = ref.createContext(context);
		assertSame(ref, policyRefContext.getCurrentPolicySetIDReference());
		order.verify(context, times(2)).getCurrentPolicySetIDReference();
		order.verify(context).resolve(ref);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(context, times(2)).getCurrentPolicySetIDReference();
		verify(context, times(1)).resolve(ref);
		verifyNoMoreInteractions(algorithm);
		when(context.getCurrentPolicySet()).thenReturn(policySet);
		assertEquals("otherTestId", policySet.getId());
		assertEquals(Version.parse("1.0"), policySet.getVersion());
		assertEquals(MatchResult.INDETERMINATE, ref.isMatch(policyRefContext));
		order.verify(context).getCurrentPolicySet();
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(context, times(2)).getCurrentPolicySetIDReference();
		verify(context, times(1)).resolve(ref);
		verify(context, times(1)).getCurrentPolicySet();
		verifyNoMoreInteractions(algorithm);
	}

	@Test
	public void testPolicyIDResolutionViaWrongEvaluationContext() throws EvaluationException
	{
		PolicySetIDReference ref = PolicySetIDReference.builder("testId").versionAsString("1.+").build();
		assertThrows(IllegalArgumentException.class, () -> ref.evaluate(context));
		verify(context).getCurrentPolicySetIDReference();
		verifyNoMoreInteractions(algorithm);
	}

	@Test
	public void testIsApplicablePolicyIDReference() throws CoreException
	{
		PolicySetIDReference ref = PolicySetIDReference.builder("testId").versionAsString("1.+").build();
		when(context.getCurrentPolicySetIDReference()).thenReturn(null);
		when(context.getCurrentPolicySet()).thenReturn(policySet);
		when(context.getCurrentPolicy()).thenReturn(null);
		when(context.resolve(ref)).thenReturn(refPolicySet);
		EvaluationContext policyRefContext = ref.createContext(context);
		assertEquals("testId", refPolicySet.getId());
		assertEquals(Version.parse("1.0"), refPolicySet.getVersion());
		assertEquals(MatchResult.MATCH, ref.isMatch(policyRefContext));
		InOrder order = inOrder(context, algorithm);
		order.verify(context, times(2)).getCurrentPolicySetIDReference();
		order.verify(context).resolve(ref);
		order.verify(context, times(2)).getCurrentPolicySet();
		order.verify(context).getCurrentPolicy();
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(context, times(2)).getCurrentPolicySetIDReference();
		verify(context, times(1)).resolve(ref);
		verify(context, times(2)).getCurrentPolicySet();
		verify(context, times(1)).getCurrentPolicy();
		verifyNoMoreInteractions(algorithm);
		assertSame(ref, policyRefContext.getCurrentPolicySetIDReference());
		assertSame(refPolicySet, policyRefContext.getCurrentPolicySet());
	}

	@Test
	public void testEvaluateIfApplicable() throws CoreException
	{
		PolicySetIDReference ref = PolicySetIDReference.builder("testId").versionAsString("1.+").build();
		when(context.getCurrentPolicySetIDReference()).thenReturn(null);
		when(context.getCurrentPolicySet()).thenReturn(policySet);
		when(context.getCurrentPolicy()).thenReturn(null);
		when(context.resolve(ref)).thenReturn(refPolicySet);
		when(algorithm.combine(any(EvaluationContext.class), eq(Collections.emptyList()))).thenReturn(Decision.DENY);
		EvaluationContext policyRefContext = ref.createContext(context);
		assertEquals("testId", refPolicySet.getId());
		assertEquals(Version.parse("1.0"), refPolicySet.getVersion());
		assertEquals(Decision.DENY, ref.evaluate(policyRefContext));
		InOrder order = inOrder(context, algorithm);
		order.verify(context, times(2)).getCurrentPolicySetIDReference();
		order.verify(context).resolve(ref);
		order.verify(context, times(2)).getCurrentPolicySet();
		order.verify(context).getCurrentPolicy();
		ArgumentCaptor<EvaluationContext> refContext = ArgumentCaptor.forClass(EvaluationContext.class);
		order.verify(algorithm).combine(refContext.capture(), eq(Collections.emptyList()));
		assertSame(policyRefContext, refContext.getValue());
		// A policy set without applicable advice or obligations adds none (design D19)
		verify(context, never()).addAdvices(any(), any());
		verify(context, never()).addObligations(any(), any());
		order.verify(context).addEvaluationResult(refPolicySet, Decision.DENY);
		// Count all calls; strict stubs can otherwise mark extra calls as verified.
		verify(context, times(2)).getCurrentPolicySetIDReference();
		verify(context, times(1)).resolve(ref);
		verify(context, times(2)).getCurrentPolicySet();
		verify(context, times(1)).getCurrentPolicy();
		verify(algorithm, times(1)).combine(refContext.capture(), eq(Collections.emptyList()));
		verify(context, times(1)).addEvaluationResult(refPolicySet, Decision.DENY);
		assertSame(ref, policyRefContext.getCurrentPolicySetIDReference());
		assertSame(refPolicySet, policyRefContext.getCurrentPolicySet());
	}

	@Test
	public void testObjectMethods() {
		PolicySetIDReference ref1 = PolicySetIDReference.builder("testId1").versionAsString("1.+").build();
		PolicySetIDReference ref2 = PolicySetIDReference.builder("testId1").versionAsString("1.+").build();
		PolicySetIDReference ref3 = PolicySetIDReference.builder("testId2").versionAsString("1.+").build();


		assertTrue(ref1.equals(ref1));
		assertTrue(ref1.equals(ref2));
		assertFalse(ref1.equals(null));
		assertFalse(ref1.equals(ref3));
		assertFalse(ref1.equals("some string"));
		assertFalse(ref1.equals(PolicyIDReference.builder("testId1").versionAsString("1.+").build()));

		assertEquals(ref1.hashCode(), ref2.hashCode());
		assertEquals(ref1.toString(), ref2.toString());
	}
}
