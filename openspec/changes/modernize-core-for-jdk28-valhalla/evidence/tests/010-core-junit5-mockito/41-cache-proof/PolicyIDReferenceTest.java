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
public class PolicyIDReferenceTest
{
	private EvaluationContext context;
	private Policy refPolicy;
	private DecisionCombiningAlgorithm<Rule> algorithm;

	@BeforeEach
	public void init()
	{
		context = mock(EvaluationContext.class);
		algorithm = mock(DecisionCombiningAlgorithm.class);
		refPolicy = Policy.builder("testId").version("1.0").combiningAlgorithm(algorithm).build();
	}

	@Test
	public void testEvaluateFailedToResolveReference() throws CoreException
	{
		PolicyIDReference ref = PolicyIDReference.builder("testId").versionAsString("1.+").build();
		when(context.getCurrentPolicyIDReference()).thenReturn(null);
		when(context.getCurrentPolicy()).thenReturn(null);
		when(context.resolve(ref)).thenThrow(new PolicyResolutionException(null));
		InOrder order = inOrder(context, algorithm);
		EvaluationContext policyRefContext = ref.createContext(context);
		assertSame(ref, policyRefContext.getCurrentPolicyIDReference());
		order.verify(context).getCurrentPolicyIDReference();
		order.verify(context).getCurrentPolicy();
		order.verify(context).getCurrentPolicyIDReference();
		order.verify(context).resolve(ref);
		verifyNoMoreInteractions(context, algorithm);
		assertEquals(Decision.INDETERMINATE, ref.evaluate(policyRefContext));
		order.verify(context).getCurrentPolicy();
		verifyNoMoreInteractions(context, algorithm);
		assertEquals(Decision.INDETERMINATE, ref.evaluate(policyRefContext));
		order.verify(context).getCurrentPolicy();
		verifyNoMoreInteractions(context, algorithm);
		assertEquals(MatchResult.INDETERMINATE, ref.isMatch(policyRefContext));
		order.verify(context).getCurrentPolicy();
		verifyNoMoreInteractions(context, algorithm);
	}

	@Test
	public void testEvaluateFailedToResolveReferenceAndReturnNull() throws CoreException
	{
		PolicyIDReference ref = PolicyIDReference.builder("testId").versionAsString("1.+").build();
		when(context.getCurrentPolicyIDReference()).thenReturn(null);
		when(context.getCurrentPolicy()).thenReturn(null);
		when(context.resolve(ref)).thenReturn(null);
		InOrder order = inOrder(context, algorithm);
		EvaluationContext policyRefContext = ref.createContext(context);
		assertSame(ref, policyRefContext.getCurrentPolicyIDReference());
		order.verify(context).getCurrentPolicyIDReference();
		order.verify(context).getCurrentPolicy();
		order.verify(context).getCurrentPolicyIDReference();
		order.verify(context).resolve(ref);
		verifyNoMoreInteractions(context, algorithm);
		assertEquals(Decision.INDETERMINATE, ref.evaluate(policyRefContext));
		order.verify(context).getCurrentPolicy();
		verifyNoMoreInteractions(context, algorithm);
		assertEquals(Decision.INDETERMINATE, ref.evaluate(policyRefContext));
		order.verify(context).getCurrentPolicy();
		verifyNoMoreInteractions(context, algorithm);
		assertEquals(MatchResult.INDETERMINATE, ref.isMatch(policyRefContext));
		order.verify(context).getCurrentPolicy();
		verifyNoMoreInteractions(context, algorithm);
	}

	@Test
	public void testEvaluateIfApplicableFailedToResolveReferenceViaException() throws CoreException
	{
		PolicyIDReference ref = PolicyIDReference.builder("testId").versionAsString("1.+").build();
		when(context.getCurrentPolicyIDReference()).thenReturn(null);
		when(context.getCurrentPolicy()).thenReturn(null);
		when(context.resolve(ref)).thenThrow(new PolicyResolutionException(null));
		InOrder order = inOrder(context, algorithm);
		EvaluationContext policyRefContext = ref.createContext(context);
		assertSame(ref, policyRefContext.getCurrentPolicyIDReference());
		order.verify(context).getCurrentPolicyIDReference();
		order.verify(context).getCurrentPolicy();
		order.verify(context).getCurrentPolicyIDReference();
		order.verify(context).resolve(ref);
		verifyNoMoreInteractions(context, algorithm);
		assertEquals(Decision.INDETERMINATE, ref.evaluate(policyRefContext));
		order.verify(context).getCurrentPolicy();
		verifyNoMoreInteractions(context, algorithm);
	}

	@Test
	public void testEvaluateIfApplicableFailedToResolveReferenceReturnsNull() throws CoreException
	{
		PolicyIDReference ref = PolicyIDReference.builder("testId").versionAsString("1.+").build();
		when(context.getCurrentPolicyIDReference()).thenReturn(null);
		when(context.getCurrentPolicy()).thenReturn(null);
		when(context.resolve(ref)).thenReturn(null);
		InOrder order = inOrder(context, algorithm);
		EvaluationContext policyRefContext = ref.createContext(context);
		assertSame(ref, policyRefContext.getCurrentPolicyIDReference());
		order.verify(context).getCurrentPolicyIDReference();
		order.verify(context).getCurrentPolicy();
		order.verify(context).getCurrentPolicyIDReference();
		order.verify(context).resolve(ref);
		verifyNoMoreInteractions(context, algorithm);
		assertEquals(Decision.INDETERMINATE, ref.evaluate(policyRefContext));
		order.verify(context).getCurrentPolicy();
		verifyNoMoreInteractions(context, algorithm);
	}

	@Test
	public void testIsApplicableFailedToResolveReference() throws CoreException
	{
		PolicyIDReference ref = PolicyIDReference.builder("testId").versionAsString("1.+").build();
		when(context.getCurrentPolicyIDReference()).thenReturn(null);
		when(context.getCurrentPolicy()).thenReturn(null);
		when(context.resolve(ref)).thenThrow(new PolicyResolutionException(null));
		InOrder order = inOrder(context, algorithm);
		EvaluationContext policyRefContext = ref.createContext(context);
		assertSame(ref, policyRefContext.getCurrentPolicyIDReference());
		order.verify(context).getCurrentPolicyIDReference();
		order.verify(context).getCurrentPolicy();
		order.verify(context).getCurrentPolicyIDReference();
		order.verify(context).resolve(ref);
		verifyNoMoreInteractions(context, algorithm);
		assertEquals(MatchResult.INDETERMINATE, ref.isMatch(policyRefContext));
		order.verify(context).getCurrentPolicy();
		verifyNoMoreInteractions(context, algorithm);
	}

	@Test
	public void testPolicyIDResolutionViaWrongEvaluationContext() throws EvaluationException
	{
		PolicyIDReference ref = PolicyIDReference.builder("testId").versionAsString("1.+").build();
		assertThrows(IllegalArgumentException.class, () -> ref.evaluate(context));
		verify(context).getCurrentPolicyIDReference();
		verifyNoMoreInteractions(context, algorithm);
	}

	@Test
	public void testCreatePolicyIDReferenceEvaluatonConntext() throws EvaluationException
	{
		PolicyIDReference ref = PolicyIDReference.builder("testId").versionAsString("1.+").build();
		when(context.getCurrentPolicyIDReference()).thenReturn(null);
		when(context.getCurrentPolicy()).thenReturn(null);
		when(context.resolve(ref)).thenReturn(refPolicy);
		EvaluationContext policyContext = ref.createContext(context);
		assertSame(refPolicy, policyContext.getCurrentPolicy());
		assertSame(ref, policyContext.getParentContext().getCurrentPolicyIDReference());
		assertSame(context, policyContext.getParentContext().getParentContext());
		InOrder order = inOrder(context, algorithm);
		order.verify(context).getCurrentPolicyIDReference();
		order.verify(context).getCurrentPolicy();
		order.verify(context).getCurrentPolicyIDReference();
		order.verify(context).resolve(ref);
		order.verify(context).getCurrentPolicy();
		verifyNoMoreInteractions(context, algorithm);
	}

	@Test
	public void testEvaluateIfApplicablePolicyCanBeResolved() throws CoreException
	{
		PolicyIDReference ref = PolicyIDReference.builder("testId").versionAsString("1.+").build();
		when(context.getCurrentPolicyIDReference()).thenReturn(null);
		when(context.getCurrentPolicy()).thenReturn(null);
		when(context.resolve(ref)).thenReturn(refPolicy);
		when(algorithm.combine(any(EvaluationContext.class), eq(Collections.emptyList()))).thenReturn(Decision.PERMIT);
		EvaluationContext ctx = ref.createContext(context);
		assertEquals("testId", refPolicy.getId());
		assertEquals(Version.parse("1.0"), refPolicy.getVersion());
		assertEquals(Decision.PERMIT, ref.evaluate(ctx));
		ArgumentCaptor<EvaluationContext> refContext = ArgumentCaptor.forClass(EvaluationContext.class);
		InOrder order = inOrder(context, algorithm);
		order.verify(context).getCurrentPolicyIDReference();
		order.verify(context).getCurrentPolicy();
		order.verify(context).getCurrentPolicyIDReference();
		order.verify(context).resolve(ref);
		order.verify(context).getCurrentPolicy();
		order.verify(algorithm).combine(refContext.capture(), eq(Collections.emptyList()));
		assertSame(ctx, refContext.getValue());
		verify(context, never()).isExtendedIndeterminateEval();
		order.verify(context).addAdvices(Decision.PERMIT, Collections.emptyList());
		order.verify(context).addObligations(Decision.PERMIT, Collections.emptyList());
		order.verify(context).addEvaluationResult(refPolicy, Decision.PERMIT);
		verifyNoMoreInteractions(context, algorithm);
		assertSame(ref, ctx.getCurrentPolicyIDReference());
		assertSame(refPolicy, ctx.getCurrentPolicy());
	}

	@Test
	public void testIsApplicablePolicyCanBeResolved() throws CoreException
	{
		PolicyIDReference ref = PolicyIDReference.builder("testId").versionAsString("1.+").build();
		when(context.getCurrentPolicyIDReference()).thenReturn(null);
		when(context.getCurrentPolicy()).thenReturn(null);
		when(context.resolve(ref)).thenReturn(refPolicy);
		EvaluationContext ctx = ref.createContext(context);
		assertEquals("testId", refPolicy.getId());
		assertEquals(Version.parse("1.0"), refPolicy.getVersion());
		assertEquals(MatchResult.MATCH, ref.isMatch(ctx));
		InOrder order = inOrder(context, algorithm);
		order.verify(context).getCurrentPolicyIDReference();
		order.verify(context).getCurrentPolicy();
		order.verify(context).getCurrentPolicyIDReference();
		order.verify(context).resolve(ref);
		order.verify(context).getCurrentPolicy();
		verifyNoMoreInteractions(context, algorithm);
		assertSame(ref, ctx.getCurrentPolicyIDReference());
		assertSame(refPolicy, ctx.getCurrentPolicy());
	}

	@Test
	public void testReferersTo() throws Exception
	{
		PolicyIDReference ref = PolicyIDReference.builder("testId").versionAsString("1.+").build();
		refPolicy = Policy.builder("testId").version("1.0.1").combiningAlgorithm(algorithm).build();
		assertEquals("testId", refPolicy.getId());
		assertEquals(Version.parse("1.0.1"), refPolicy.getVersion());
		assertTrue(ref.isReferenceTo(refPolicy));
		verifyNoMoreInteractions(context, algorithm);
	}

	@Test
	public void testReferenceBuilder()
	{
		PolicyIDReference.Builder b = PolicyIDReference.builder("testId");
		assertEquals("testId", b.build().getId());
		b.versionAsString("1.0");
		assertEquals("testId", b.build().getId());
		assertEquals(VersionMatch.parse("1.0"), b.build().getVersion());
		b = PolicyIDReference.builder("testId");
		b.versionAsString(null);
		assertEquals("testId", b.build().getId());
		assertNull(b.build().getVersion());
		b = PolicyIDReference.builder("testId");
		b.versionAsString("");
		assertEquals("testId", b.build().getId());
		assertNull(b.build().getVersion());
		b = PolicyIDReference.builder("testId");
		b.latest(null);
		assertEquals("testId", b.build().getId());
		assertNull(b.build().getVersion());
		assertNull(b.build().getLatestVersion());
		b = PolicyIDReference.builder("testId");
		b.latest("");
		assertEquals("testId", b.build().getId());
		assertNull(b.build().getVersion());
		assertNull(b.build().getLatestVersion());
	}

	@Test
	public void testObjectMethods() {
		PolicyIDReference ref1 = PolicyIDReference.builder("testId1").versionAsString("1.+").build();
		PolicyIDReference ref2 = PolicyIDReference.builder("testId1").versionAsString("1.+").build();
		PolicyIDReference ref3 = PolicyIDReference.builder("testId2").versionAsString("1.+").build();


		assertTrue(ref1.equals(ref1));
		assertTrue(ref1.equals(ref2));
		assertFalse(ref1.equals(null));
		assertFalse(ref1.equals(ref3));
		assertFalse(ref1.equals("some string"));
		assertFalse(ref1.equals(PolicySetIDReference.builder("testId1").versionAsString("1.+").build()));

		assertEquals(ref1.hashCode(), ref2.hashCode());
		assertEquals(ref1.toString(), ref2.toString());
	}
}
