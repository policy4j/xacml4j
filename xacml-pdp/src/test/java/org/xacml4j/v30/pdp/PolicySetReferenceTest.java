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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.*;


public class PolicySetReferenceTest
{
	private DecisionRuleEvaluationContext context;
	private PolicySet policySet;
	private PolicySet refPolicySet;
	private IMocksControl c;

	@Before
	public void init(){
		this.c = createStrictControl();
		this.policySet = c.createMock(PolicySet.class);
		this.refPolicySet = c.createMock(PolicySet.class);
		this.context = c.createMock(DecisionRuleEvaluationContext.class);

	}

	@Test
	public void testEvaluateFailToResolveReferenceViaException() throws XacmlException
	{
		PolicySetReference ref = PolicySetReference.builder("testId").version("1.+").build();
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.resolve(ref)).andThrow(new PolicyResolutionException(context, "Failed to resolve"));

		c.replay();
		DecisionRuleEvaluationContext policyRefContext = ref.createContext(context);
		assertSame(ref, policyRefContext.getCurrentPolicySetIDReference());

		c.verify();
		c.reset();

		expect(context.getCurrentPolicySet()).andReturn(policySet);
		expect(policySet.getId()).andReturn("otherTestId");
		expect(policySet.getVersion()).andReturn(Version.parse("1.0"));
		c.replay();
		assertEquals(Decision.INDETERMINATE, ref.evaluate(policyRefContext));
		c.verify();
	}

	@Test
	public void testEvaluateFailToResolveReferenceReturnsNull() throws XacmlException
	{
		PolicySetReference ref = PolicySetReference.builder("testId").version("1.+").build();
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.resolve(ref)).andReturn(null);

		c.replay();

		DecisionRuleEvaluationContext policyRefContext = ref.createContext(context);
		assertSame(ref, policyRefContext.getCurrentPolicySetIDReference());

		c.verify();

		c.reset();

		expect(context.getCurrentPolicySet()).andReturn(policySet);
		expect(policySet.getId()).andReturn("otherTestId");
		expect(policySet.getVersion()).andReturn(Version.parse("1.0"));

		c.replay();

		assertEquals(Decision.INDETERMINATE, ref.evaluate(policyRefContext));

		c.verify();
	}

	@Test
	public void testIsApplicableFailToResolveReference() throws XacmlException
	{
		PolicySetReference ref = PolicySetReference.builder("testId").version("1.+").build();
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.resolve(ref)).andThrow(new PolicyResolutionException(context, "Failed to resolve"));

		c.replay();

		DecisionRuleEvaluationContext policyRefContext = ref.createContext(context);
		assertSame(ref, policyRefContext.getCurrentPolicySetIDReference());

		c.verify();
		c.reset();

		expect(context.getCurrentPolicySet()).andReturn(policySet);
		expect(policySet.getId()).andReturn("otherTestId");
		expect(policySet.getVersion()).andReturn(Version.parse("1.0"));

		c.replay();
		assertEquals(MatchResult.INDETERMINATE, ref.isMatch(policyRefContext));
		c.verify();
	}

	@Test(expected=IllegalArgumentException.class)
	public void testPolicyIDResolutionViaWrongEvaluationContext() throws EvaluationException
	{
		PolicySetReference ref = PolicySetReference.builder("testId").version("1.+").build();
		ref.evaluate(context);
	}

	@Test
	public void testIsApplicablePolicyIDReference() throws XacmlException
	{
		PolicySetReference ref = PolicySetReference.builder("testId").version("1.+").build();

		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.resolve(ref)).andReturn(refPolicySet);
		Capture<DecisionRuleEvaluationContext> refContext = new Capture<DecisionRuleEvaluationContext>();
		expect(refPolicySet.createContext(capture(refContext))).andAnswer(new IAnswer<DecisionRuleEvaluationContext>() {
			@Override
			public DecisionRuleEvaluationContext answer() throws Throwable {
				DecisionRuleEvaluationContext ctx = (DecisionRuleEvaluationContext)EasyMock.getCurrentArguments()[0];
				return refPolicySet.new PolicySetDelegatingEvaluationContext(ctx);
	        }
		});

		expect(context.getCurrentPolicySet()).andReturn(policySet).anyTimes();
		expect(context.getCurrentPolicy()).andReturn(null).anyTimes();

		expect(refPolicySet.getId()).andReturn("testId");
		expect(refPolicySet.getVersion()).andReturn(Version.parse("1.0"));
		expect(refPolicySet.isMatch(isA(DecisionRuleEvaluationContext.class))).andReturn(MatchResult.MATCH);

		c.replay();

		DecisionRuleEvaluationContext policyRefContext = ref.createContext(context);
		assertEquals(MatchResult.MATCH, ref.isMatch(policyRefContext));
		c.verify();


		assertSame(ref, policyRefContext.getCurrentPolicySetIDReference());
		assertSame(refPolicySet, policyRefContext.getCurrentPolicySet());

	}

	@Test
	public void testEvaluateIfApplicable() throws XacmlException
	{
		PolicySetReference ref = PolicySetReference.builder("testId").version("1.+").build();

		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.resolve(ref)).andReturn(refPolicySet);
		Capture<DecisionRuleEvaluationContext> refContext = new Capture<DecisionRuleEvaluationContext>();
		expect(refPolicySet.createContext(capture(refContext))).andAnswer(new IAnswer<DecisionRuleEvaluationContext>() {
			@Override
			public DecisionRuleEvaluationContext answer() throws Throwable {
				DecisionRuleEvaluationContext ctx = (DecisionRuleEvaluationContext)EasyMock.getCurrentArguments()[0];
				return refPolicySet.new PolicySetDelegatingEvaluationContext(ctx);
	        }
		});

		expect(context.getCurrentPolicySet()).andReturn(policySet).anyTimes();
		expect(context.getCurrentPolicy()).andReturn(null).anyTimes();

		expect(refPolicySet.getId()).andReturn("testId");
		expect(refPolicySet.getVersion()).andReturn(Version.parse("1.0"));
		expect(refPolicySet.evaluate(isA(DecisionRuleEvaluationContext.class))).andReturn(Decision.DENY);

		c.replay();
		DecisionRuleEvaluationContext policyRefContext = ref.createContext(context);
		assertEquals(Decision.DENY, ref.evaluate(policyRefContext));
		c.verify();

		assertSame(ref, policyRefContext.getCurrentPolicySetIDReference());
		assertSame(refPolicySet, policyRefContext.getCurrentPolicySet());
	}

	@Test
	public void testObjectMethods() {
		PolicySetReference ref1 = PolicySetReference.builder("testId1").version("1.+").build();
		PolicySetReference ref2 = PolicySetReference.builder("testId1").version("1.+").build();
		PolicySetReference ref3 = PolicySetReference.builder("testId2").version("1.+").build();


		assertTrue(ref1.equals(ref1));
		assertTrue(ref1.equals(ref2));
		assertFalse(ref1.equals(null));
		assertFalse(ref1.equals(ref3));
		assertFalse(ref1.equals("some string"));
		assertFalse(ref1.equals(PolicyReference.builder("testId1").version("1.+").build()));

		assertEquals(ref1.hashCode(), ref2.hashCode());
		assertEquals(ref1.toString(), ref2.toString());
	}
}
