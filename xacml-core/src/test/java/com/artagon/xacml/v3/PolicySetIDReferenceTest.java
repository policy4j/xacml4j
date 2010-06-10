package com.artagon.xacml.v3;

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

import com.artagon.xacml.v3.policy.spi.XPathProvider;

public class PolicySetIDReferenceTest
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
	public void testNoReferencedPolicyFound() throws XacmlException
	{
		PolicySetIDReference ref = new PolicySetIDReference("testId", new VersionMatch("1.+"));
		expect(policyResolver.resolve(context, ref)).andThrow(new PolicyResolutionException(context, "Failed to resolve"));
		replay(policyResolver);
		EvaluationContext policyRefContext = ref.createContext(context);
		verify(policyResolver);
		assertSame(ref, policyRefContext.getCurrentPolicySetIDReference());
		reset(policyResolver);

		expect(policySet.getId()).andReturn("otherTestId");
		expect(policySet.getVersion()).andReturn(Version.parse("1.0"));
		replay(policySet);
		assertEquals(Decision.INDETERMINATE, ref.evaluate(policyRefContext));
		verify(policySet);

		reset(policySet);
		expect(policySet.getId()).andReturn("otherTestId");
		expect(policySet.getVersion()).andReturn(Version.parse("1.0"));
		replay(policySet);
		assertEquals(Decision.INDETERMINATE, ref.evaluateIfApplicable(policyRefContext));
		verify(policySet);

		reset(policySet);
		expect(policySet.getId()).andReturn("otherTestId");
		expect(policySet.getVersion()).andReturn(Version.parse("1.0"));
		replay(policySet);
		assertEquals(MatchResult.INDETERMINATE, ref.isApplicable(policyRefContext));
		verify(policySet);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testPolicyIDResolutionViaWrongEvaluationContext() throws EvaluationException
	{
		PolicyIDReference ref = new PolicyIDReference("testId", new VersionMatch("1.+"));
		ref.evaluate(context);
	}
	
	@Test
	public void testEvaluatePolicyIDReference() throws XacmlException
	{
		PolicySetIDReference ref = new PolicySetIDReference("testId", new VersionMatch("1.+"));
		expect(policyResolver.resolve(context, ref)).andReturn(refPolicySet);
		Capture<EvaluationContext> refContext = new Capture<EvaluationContext>();
		expect(refPolicySet.createContext(capture(refContext))).andAnswer(new IAnswer<EvaluationContext>() {
			public EvaluationContext answer() throws Throwable {
				EvaluationContext ctx = (EvaluationContext)EasyMock.getCurrentArguments()[0];
				return refPolicySet.new PolicySetDelegatingEvaluationContext(ctx);
	        }
		});
		
		expect(refPolicySet.getId()).andReturn("testId");
		expect(refPolicySet.getVersion()).andReturn(Version.parse("1.0"));
		expect(refPolicySet.evaluate(isA(EvaluationContext.class))).andReturn(Decision.PERMIT);

		replay(policyResolver, refPolicySet);
		EvaluationContext ctx = ref.createContext(context);
		assertEquals(Decision.PERMIT, ref.evaluate(ctx));
		assertSame(ref, ctx.getCurrentPolicySetIDReference());
		assertSame(refPolicySet, ctx.getCurrentPolicySet());
		verify(policyResolver, refPolicySet);
	}
	
	@Test
	public void testEvaluateIfApplicablePolicyIDReference() throws XacmlException
	{
		PolicySetIDReference ref = new PolicySetIDReference("testId", new VersionMatch("1.+"));
		expect(policyResolver.resolve(context, ref)).andReturn(refPolicySet);
		Capture<EvaluationContext> refContext = new Capture<EvaluationContext>();
		expect(refPolicySet.createContext(capture(refContext))).andAnswer(new IAnswer<EvaluationContext>() {
			public EvaluationContext answer() throws Throwable {
				EvaluationContext ctx = (EvaluationContext)EasyMock.getCurrentArguments()[0];
				return refPolicySet.new PolicySetDelegatingEvaluationContext(ctx);
	        }
		});
		
		expect(refPolicySet.getId()).andReturn("testId");
		expect(refPolicySet.getVersion()).andReturn(Version.parse("1.0"));
		expect(refPolicySet.evaluateIfApplicable(isA(EvaluationContext.class))).andReturn(Decision.PERMIT);

		replay(policyResolver, refPolicySet);
		EvaluationContext ctx = ref.createContext(context);
		assertEquals(Decision.PERMIT, ref.evaluateIfApplicable(ctx));
		assertSame(ref, ctx.getCurrentPolicySetIDReference());
		assertSame(refPolicySet, ctx.getCurrentPolicySet());
		verify(policyResolver, refPolicySet);
	}
	
	@Test
	public void testIsApplicablePolicyIDReference() throws XacmlException
	{
		PolicySetIDReference ref = new PolicySetIDReference("testId", new VersionMatch("1.+"));
		expect(policyResolver.resolve(context, ref)).andReturn(refPolicySet);
		Capture<EvaluationContext> refContext = new Capture<EvaluationContext>();
		expect(refPolicySet.createContext(capture(refContext))).andAnswer(new IAnswer<EvaluationContext>() {
			public EvaluationContext answer() throws Throwable {
				EvaluationContext ctx = (EvaluationContext)EasyMock.getCurrentArguments()[0];
				return refPolicySet.new PolicySetDelegatingEvaluationContext(ctx);
	        }
		});
		
		expect(refPolicySet.getId()).andReturn("testId");
		expect(refPolicySet.getVersion()).andReturn(Version.parse("1.0"));
		expect(refPolicySet.isApplicable(isA(EvaluationContext.class))).andReturn(MatchResult.MATCH);

		replay(policyResolver, refPolicySet);
		EvaluationContext ctx = ref.createContext(context);
		assertEquals(MatchResult.MATCH, ref.isApplicable(ctx));
		assertSame(ref, ctx.getCurrentPolicySetIDReference());
		assertSame(refPolicySet, ctx.getCurrentPolicySet());
		verify(policyResolver, refPolicySet);
	}
}