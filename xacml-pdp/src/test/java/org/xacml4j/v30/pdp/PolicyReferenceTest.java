package org.xacml4j.v30.pdp;

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

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.*;


public class PolicyReferenceTest
{
	private DecisionRuleEvaluationContext context;
	private Policy refPolicy;
	private IMocksControl c;

	@Before
	public void init(){
		this.c = createStrictControl();
		this.refPolicy = c.createMock(Policy.class);
		this.context = c.createMock(DecisionRuleEvaluationContext.class);
	}

	@Test
	public void testEvaluateFailedToResolveReference() throws XacmlException
	{
		PolicyReference ref = PolicyReference.builder("testId").version("1.+").build();

		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.getCurrentPolicy()).andReturn(null);
		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.resolve(ref)).andThrow(new PolicyResolutionException(context, "Failed to resolve"));
		c.replay();

		DecisionRuleEvaluationContext policyRefContext = ref.createContext(context);
		assertSame(ref, policyRefContext.getCurrentPolicyIDReference());
		c.verify();

		c.reset();
		expect(context.getCurrentPolicy()).andReturn(null);
		c.replay();

		assertEquals(Decision.INDETERMINATE, ref.evaluate(policyRefContext));
		c.verify();

		c.reset();
		expect(context.getCurrentPolicy()).andReturn(null);
		c.replay();
		assertEquals(Decision.INDETERMINATE, ref.evaluate(policyRefContext));
		c.verify();

		c.reset();
		expect(context.getCurrentPolicy()).andReturn(null);
		c.replay();
		assertEquals(MatchResult.INDETERMINATE, ref.isMatch(policyRefContext));
		c.verify();
	}


	@Test
	public void testEvaluateFailedToResolveReferenceAndReturnNull() throws XacmlException
	{
		PolicyReference ref = PolicyReference.builder("testId").version("1.+").build();

		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.getCurrentPolicy()).andReturn(null);
		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.resolve(ref)).andReturn(null);
		c.replay();
		DecisionRuleEvaluationContext policyRefContext = ref.createContext(context);
		assertSame(ref, policyRefContext.getCurrentPolicyIDReference());
		c.verify();

		c.reset();
		expect(context.getCurrentPolicy()).andReturn(null);
		c.replay();
		assertEquals(Decision.INDETERMINATE, ref.evaluate(policyRefContext));
		c.verify();

		c.reset();
		expect(context.getCurrentPolicy()).andReturn(null);
		c.replay();
		assertEquals(Decision.INDETERMINATE, ref.evaluate(policyRefContext));
		c.verify();

		c.reset();
		expect(context.getCurrentPolicy()).andReturn(null);
		c.replay();
		assertEquals(MatchResult.INDETERMINATE, ref.isMatch(policyRefContext));
		c.verify();
	}

	@Test
	public void testEvaluateIfApplicableFailedToResolveReferenceViaException() throws XacmlException
	{
		PolicyReference ref = PolicyReference.builder("testId").version("1.+").build();

		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.getCurrentPolicy()).andReturn(null);
		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.resolve(ref)).andThrow(new PolicyResolutionException(context, "Failed to resolve"));
		c.replay();
		DecisionRuleEvaluationContext policyRefContext = ref.createContext(context);
		assertSame(ref, policyRefContext.getCurrentPolicyIDReference());
		c.verify();

		c.reset();
		expect(context.getCurrentPolicy()).andReturn(null);
		c.replay();
		assertEquals(Decision.INDETERMINATE, ref.evaluate(policyRefContext));
		c.verify();
	}

	@Test
	public void testEvaluateIfApplicableFailedToResolveReferenceReturnsNull() throws XacmlException
	{
		PolicyReference ref = PolicyReference.builder("testId").version("1.+").build();

		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.getCurrentPolicy()).andReturn(null);
		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.resolve(ref)).andReturn(null);

		c.replay();
		DecisionRuleEvaluationContext policyRefContext = ref.createContext(context);
		assertSame(ref, policyRefContext.getCurrentPolicyIDReference());
		c.verify();

		c.reset();
		expect(context.getCurrentPolicy()).andReturn(null);
		c.replay();
		assertEquals(Decision.INDETERMINATE, ref.evaluate(policyRefContext));
		c.verify();

	}

	@Test
	public void testIsApplicableFailedToResolveReference() throws XacmlException
	{
		PolicyReference ref = PolicyReference.builder("testId").version("1.+").build();

		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.getCurrentPolicy()).andReturn(null);
		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.resolve(ref)).andThrow(new PolicyResolutionException(context, "Failed to resolve"));

		c.replay();
		DecisionRuleEvaluationContext policyRefContext = ref.createContext(context);
		assertSame(ref, policyRefContext.getCurrentPolicyIDReference());
		c.verify();

		c.reset();
		expect(context.getCurrentPolicy()).andReturn(null);
		c.replay();
		assertEquals(MatchResult.INDETERMINATE, ref.isMatch(policyRefContext));
		c.verify();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testPolicyIDResolutionViaWrongEvaluationContext() throws EvaluationException
	{
		PolicyReference ref = PolicyReference.builder("testId").version("1.+").build();
		ref.evaluate(context);
	}

	@Test
	public void testCreatePolicyIDReferenceEvaluatonConntext()
				throws EvaluationException
	{
		PolicyReference ref = PolicyReference.builder("testId").version("1.+").build();

		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.getCurrentPolicy()).andReturn(null);
		expect(context.getCurrentPolicyIDReference()).andReturn(null);

		expect(context.resolve(ref)).andReturn(refPolicy);

		Capture<DecisionRuleEvaluationContext> refContext = new Capture<DecisionRuleEvaluationContext>();
		DecisionRuleEvaluationContext policyContext = c.createMock(DecisionRuleEvaluationContext.class);
		expect(refPolicy.createContext(capture(refContext))).andReturn(policyContext);

		c.replay();

		ref.createContext(context);

		c.verify();


	}

	@Test
	public void testEvaluateIfApplicablePolicyCanBeResolved() throws XacmlException
	{
		PolicyReference ref = PolicyReference.builder("testId").version("1.+").build();
		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.getCurrentPolicy()).andReturn(null);
		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.resolve(ref)).andReturn(refPolicy);
		Capture<DecisionRuleEvaluationContext> refContext = new Capture<DecisionRuleEvaluationContext>();
		expect(refPolicy.createContext(capture(refContext))).andAnswer(new IAnswer<DecisionRuleEvaluationContext>() {
			@Override
			public DecisionRuleEvaluationContext answer() throws Throwable {
				DecisionRuleEvaluationContext ctx = (DecisionRuleEvaluationContext)EasyMock.getCurrentArguments()[0];
				return refPolicy.new PolicyDelegatingEvaluationContext(ctx);
	        }
		});
		expectPolicyMatch(refPolicy, "testId", "1.0");
		expect(refPolicy.evaluate(isA(DecisionRuleEvaluationContext.class))).andReturn(Decision.PERMIT);
		c.replay();
		DecisionRuleEvaluationContext ctx = ref.createContext(context);
		assertEquals(Decision.PERMIT, ref.evaluate(ctx));
		c.verify();
		assertSame(ref, ctx.getCurrentPolicyIDReference());
		assertSame(refPolicy, ctx.getCurrentPolicy());

	}

	@Test
	public void testIsApplicablePolicyCanBeResolved() throws XacmlException
	{
		PolicyReference ref = PolicyReference.builder("testId").version("1.+").build();
		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.getCurrentPolicy()).andReturn(null);
		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.resolve(ref)).andReturn(refPolicy);
		Capture<DecisionRuleEvaluationContext> refContext = new Capture<DecisionRuleEvaluationContext>();
		expect(refPolicy.createContext(capture(refContext))).andAnswer(new IAnswer<DecisionRuleEvaluationContext>() {
			@Override
			public DecisionRuleEvaluationContext answer() throws Throwable {
				DecisionRuleEvaluationContext ctx = (DecisionRuleEvaluationContext)EasyMock.getCurrentArguments()[0];
				return refPolicy.new PolicyDelegatingEvaluationContext(ctx);
	        }
		});
		expectPolicyMatch(refPolicy, "testId", "1.0");
		expect(refPolicy.isMatch(isA(DecisionRuleEvaluationContext.class))).andReturn(MatchResult.MATCH);
		c.replay();
		DecisionRuleEvaluationContext ctx = ref.createContext(context);
		assertEquals(MatchResult.MATCH, ref.isMatch(ctx));
		c.verify();
		assertSame(ref, ctx.getCurrentPolicyIDReference());
		assertSame(refPolicy, ctx.getCurrentPolicy());

	}

	private void expectPolicyMatch(Policy p, String id, String v)
				throws EvaluationException
	{
		expect(p.getId()).andReturn(id);
		expect(p.getVersion()).andReturn(Version.parse(v));
	}

	@Test
	public void testRefersTo() throws Exception
	{
		PolicyReference ref = PolicyReference.builder("testId").version("1.+").build();
		expect(refPolicy.getId()).andReturn("testId");
		expect(refPolicy.getVersion()).andReturn(Version.parse("1.0.1"));
		c.replay();
		assertTrue(ref.isReferenceTo(refPolicy));
		c.verify();
	}

	@Test
	public void testReferenceBuilder()
	{
		PolicyReference.Builder b = PolicyReference.builder("testId");
		assertEquals("testId", b.build().getId());
		b.version("1.0");
		assertEquals("testId", b.build().getId());
		assertEquals(VersionMatch.parse("1.0"), b.build().getVersion());
		b = PolicyReference.builder("testId");
		b.version((Version)null);
		assertEquals("testId", b.build().getId());
		assertNull(b.build().getVersion());
		b = PolicyReference.builder("testId");
		b.version("");
		assertEquals("testId", b.build().getId());
		assertNull(b.build().getVersion());
		b = PolicyReference.builder("testId");
		b.latest(null);
		assertEquals("testId", b.build().getId());
		assertNull(b.build().getVersion());
		assertNull(b.build().getLatestVersion());
		b = PolicyReference.builder("testId");
		b.latest("");
		assertEquals("testId", b.build().getId());
		assertNull(b.build().getVersion());
		assertNull(b.build().getLatestVersion());
	}

	@Test
	public void testObjectMethods() {
		PolicyReference ref1 = PolicyReference.builder("testId1").version("1.+").build();
		PolicyReference ref2 = PolicyReference.builder("testId1").version("1.+").build();
		PolicyReference ref3 = PolicyReference.builder("testId2").version("1.+").build();


		assertTrue(ref1.equals(ref1));
		assertTrue(ref1.equals(ref2));
		assertFalse(ref1.equals(null));
		assertFalse(ref1.equals(ref3));
		assertFalse(ref1.equals("some string"));
		assertFalse(ref1.equals(PolicySetReference.builder("testId1").version("1.+").build()));

		assertEquals(ref1.hashCode(), ref2.hashCode());
		assertEquals(ref1.toString(), ref2.toString());
	}
}
