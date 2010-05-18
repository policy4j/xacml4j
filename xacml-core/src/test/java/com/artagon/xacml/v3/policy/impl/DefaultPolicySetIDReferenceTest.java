package com.artagon.xacml.v3.policy.impl;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.Request;
import com.artagon.xacml.v3.Version;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationContextFactory;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.MatchResult;
import com.artagon.xacml.v3.policy.PolicyIDReference;
import com.artagon.xacml.v3.policy.PolicyReferenceResolver;
import com.artagon.xacml.v3.policy.PolicyResolutionException;
import com.artagon.xacml.v3.policy.PolicySet;
import com.artagon.xacml.v3.policy.PolicySetIDReference;
import com.artagon.xacml.v3.policy.VersionMatch;
import com.artagon.xacml.v3.policy.spi.XPathProvider;

public class DefaultPolicySetIDReferenceTest
{
	private EvaluationContext context;
	private PolicySet policySet;
	private PolicySet refPolicySet;
	private EvaluationContextFactory contextFactory;
	private PolicyReferenceResolver policyResolver;
	private Request request;
	
	@Before
	public void init(){
		this.policyResolver = createStrictMock(PolicyReferenceResolver.class);
		this.request = createStrictMock(Request.class);
		this.policySet = createStrictMock(PolicySet.class);
		this.refPolicySet = createStrictMock(PolicySet.class);
		this.contextFactory = new DefaultEvaluationContextFactory(
				policyResolver,
				createStrictMock(XPathProvider.class));
		this.context = contextFactory.createContext(policySet, request);
	}
	
	@Test
	public void testNoReferencedPolicyFound() throws EvaluationException
	{
		PolicySetIDReference ref = new DefaultPolicySetIDReference("testId", new VersionMatch("1.+"));
		expect(policyResolver.resolve(context, ref)).andThrow(new PolicyResolutionException(context, "Failed to resolve"));
		replay(policyResolver);
		EvaluationContext policyRefContext = ref.createContext(context);
		verify(policyResolver);
		assertSame(ref, policyRefContext.getCurrentPolicySetIDReference());
		reset(policyResolver);

		expect(policySet.getId()).andReturn("otherTestId");
		expect(policySet.getVersion()).andReturn(Version.valueOf("1.0"));
		replay(policySet);
		assertEquals(Decision.INDETERMINATE, ref.evaluate(policyRefContext));
		verify(policySet);

		reset(policySet);
		expect(policySet.getId()).andReturn("otherTestId");
		expect(policySet.getVersion()).andReturn(Version.valueOf("1.0"));
		replay(policySet);
		assertEquals(Decision.INDETERMINATE, ref.evaluateIfApplicable(policyRefContext));
		verify(policySet);

		reset(policySet);
		expect(policySet.getId()).andReturn("otherTestId");
		expect(policySet.getVersion()).andReturn(Version.valueOf("1.0"));
		replay(policySet);
		assertEquals(MatchResult.INDETERMINATE, ref.isApplicable(policyRefContext));
		verify(policySet);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testPolicyIDResolutionViaWrongEvaluationContext() throws EvaluationException
	{
		PolicyIDReference ref = new DefaultPolicyIDReference("testId", new VersionMatch("1.+"));
		ref.evaluate(context);
	}
	
	@Test
	public void testEvaluatePolicyIDReference() throws PolicyResolutionException
	{
		PolicySetIDReference ref = new DefaultPolicySetIDReference("testId", new VersionMatch("1.+"));
		expect(policyResolver.resolve(context, ref)).andReturn(refPolicySet);
		Capture<EvaluationContext> refContext = new Capture<EvaluationContext>();
		expect(refPolicySet.createContext(capture(refContext))).andAnswer(new IAnswer<EvaluationContext>() {
			public EvaluationContext answer() throws Throwable {
				EvaluationContext ctx = (EvaluationContext)EasyMock.getCurrentArguments()[0];
				return new PolicySetDelegatingEvaluationContext(ctx, refPolicySet);
	        }
		});
		
		expect(refPolicySet.getId()).andReturn("testId");
		expect(refPolicySet.getVersion()).andReturn(Version.valueOf("1.0"));
		expect(refPolicySet.evaluate(isA(EvaluationContext.class))).andReturn(Decision.PERMIT);

		replay(policyResolver, refPolicySet);
		EvaluationContext ctx = ref.createContext(context);
		assertEquals(Decision.PERMIT, ref.evaluate(ctx));
		assertSame(ref, ctx.getCurrentPolicySetIDReference());
		assertSame(refPolicySet, ctx.getCurrentPolicySet());
		verify(policyResolver, refPolicySet);
	}
	
	@Test
	public void testEvaluateIfApplicablePolicyIDReference() throws PolicyResolutionException
	{
		PolicySetIDReference ref = new DefaultPolicySetIDReference("testId", new VersionMatch("1.+"));
		expect(policyResolver.resolve(context, ref)).andReturn(refPolicySet);
		Capture<EvaluationContext> refContext = new Capture<EvaluationContext>();
		expect(refPolicySet.createContext(capture(refContext))).andAnswer(new IAnswer<EvaluationContext>() {
			public EvaluationContext answer() throws Throwable {
				EvaluationContext ctx = (EvaluationContext)EasyMock.getCurrentArguments()[0];
				return new PolicySetDelegatingEvaluationContext(ctx, refPolicySet);
	        }
		});
		
		expect(refPolicySet.getId()).andReturn("testId");
		expect(refPolicySet.getVersion()).andReturn(Version.valueOf("1.0"));
		expect(refPolicySet.evaluateIfApplicable(isA(EvaluationContext.class))).andReturn(Decision.PERMIT);

		replay(policyResolver, refPolicySet);
		EvaluationContext ctx = ref.createContext(context);
		assertEquals(Decision.PERMIT, ref.evaluateIfApplicable(ctx));
		assertSame(ref, ctx.getCurrentPolicySetIDReference());
		assertSame(refPolicySet, ctx.getCurrentPolicySet());
		verify(policyResolver, refPolicySet);
	}
	
	@Test
	public void testIsApplicablePolicyIDReference() throws PolicyResolutionException
	{
		PolicySetIDReference ref = new DefaultPolicySetIDReference("testId", new VersionMatch("1.+"));
		expect(policyResolver.resolve(context, ref)).andReturn(refPolicySet);
		Capture<EvaluationContext> refContext = new Capture<EvaluationContext>();
		expect(refPolicySet.createContext(capture(refContext))).andAnswer(new IAnswer<EvaluationContext>() {
			public EvaluationContext answer() throws Throwable {
				EvaluationContext ctx = (EvaluationContext)EasyMock.getCurrentArguments()[0];
				return new PolicySetDelegatingEvaluationContext(ctx, refPolicySet);
	        }
		});
		
		expect(refPolicySet.getId()).andReturn("testId");
		expect(refPolicySet.getVersion()).andReturn(Version.valueOf("1.0"));
		expect(refPolicySet.isApplicable(isA(EvaluationContext.class))).andReturn(MatchResult.MATCH);

		replay(policyResolver, refPolicySet);
		EvaluationContext ctx = ref.createContext(context);
		assertEquals(MatchResult.MATCH, ref.isApplicable(ctx));
		assertSame(ref, ctx.getCurrentPolicySetIDReference());
		assertSame(refPolicySet, ctx.getCurrentPolicySet());
		verify(policyResolver, refPolicySet);
	}
}