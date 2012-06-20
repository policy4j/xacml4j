package com.artagon.xacml.v30.pdp;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v30.Version;

public class PolicyIDReferenceTest
{
	private EvaluationContext context;
	private Policy refPolicy;
	private IMocksControl c;

	@Before
	public void init(){
		this.c = createStrictControl();
		this.refPolicy = c.createMock(Policy.class);
		this.context = c.createMock(EvaluationContext.class);
	}

	@Test
	public void testEvaluateFailedToResolveReference() throws XacmlException
	{
		PolicyIDReference ref = new PolicyIDReference("testId", new VersionMatch("1.+"));

		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.getCurrentPolicy()).andReturn(null);
		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.resolve(ref)).andThrow(new PolicyResolutionException(context, "Failed to resolve"));
		c.replay();

		EvaluationContext policyRefContext = ref.createContext(context);
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
		assertEquals(Decision.INDETERMINATE, ref.evaluateIfMatch(policyRefContext));
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
		PolicyIDReference ref = new PolicyIDReference("testId", new VersionMatch("1.+"));

		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.getCurrentPolicy()).andReturn(null);
		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.resolve(ref)).andReturn(null);
		c.replay();
		EvaluationContext policyRefContext = ref.createContext(context);
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
		assertEquals(Decision.INDETERMINATE, ref.evaluateIfMatch(policyRefContext));
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
		PolicyIDReference ref = new PolicyIDReference("testId", new VersionMatch("1.+"));

		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.getCurrentPolicy()).andReturn(null);
		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.resolve(ref)).andThrow(new PolicyResolutionException(context, "Failed to resolve"));
		c.replay();
		EvaluationContext policyRefContext = ref.createContext(context);
		assertSame(ref, policyRefContext.getCurrentPolicyIDReference());
		c.verify();

		c.reset();
		expect(context.getCurrentPolicy()).andReturn(null);
		c.replay();
		assertEquals(Decision.INDETERMINATE, ref.evaluateIfMatch(policyRefContext));
		c.verify();
	}

	@Test
	public void testEvaluateIfApplicableFailedToResolveReferenceReturnsNull() throws XacmlException
	{
		PolicyIDReference ref = new PolicyIDReference("testId", new VersionMatch("1.+"));

		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.getCurrentPolicy()).andReturn(null);
		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.resolve(ref)).andReturn(null);

		c.replay();
		EvaluationContext policyRefContext = ref.createContext(context);
		assertSame(ref, policyRefContext.getCurrentPolicyIDReference());
		c.verify();

		c.reset();
		expect(context.getCurrentPolicy()).andReturn(null);
		c.replay();
		assertEquals(Decision.INDETERMINATE, ref.evaluateIfMatch(policyRefContext));
		c.verify();

	}

	@Test
	public void testIsApplicableFailedToResolveReference() throws XacmlException
	{
		PolicyIDReference ref = new PolicyIDReference("testId", new VersionMatch("1.+"));

		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.getCurrentPolicy()).andReturn(null);
		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.resolve(ref)).andThrow(new PolicyResolutionException(context, "Failed to resolve"));

		c.replay();
		EvaluationContext policyRefContext = ref.createContext(context);
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
		PolicyIDReference ref = new PolicyIDReference("testId", new VersionMatch("1.+"));
		ref.evaluate(context);
	}

	@Test
	public void testCreatePolicyIDReferenceEvaluatonConntext()
				throws EvaluationException
	{
		PolicyIDReference ref = new PolicyIDReference("testId", new VersionMatch("1.+"));

		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.getCurrentPolicy()).andReturn(null);
		expect(context.getCurrentPolicyIDReference()).andReturn(null);

		expect(context.resolve(ref)).andReturn(refPolicy);

		Capture<EvaluationContext> refContext = new Capture<EvaluationContext>();
		EvaluationContext policyContext = c.createMock(EvaluationContext.class);
		expect(refPolicy.createContext(capture(refContext))).andReturn(policyContext);

		c.replay();

		ref.createContext(context);

		c.verify();


	}

	@Test
	public void testEvaluateIfApplicablePolicyCanBeResolved() throws XacmlException
	{
		PolicyIDReference ref = new PolicyIDReference("testId", new VersionMatch("1.+"));
		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.getCurrentPolicy()).andReturn(null);
		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.resolve(ref)).andReturn(refPolicy);
		Capture<EvaluationContext> refContext = new Capture<EvaluationContext>();
		expect(refPolicy.createContext(capture(refContext))).andAnswer(new IAnswer<EvaluationContext>() {
			public EvaluationContext answer() throws Throwable {
				EvaluationContext ctx = (EvaluationContext)EasyMock.getCurrentArguments()[0];
				return refPolicy.new PolicyDelegatingEvaluationContext(ctx);
	        }
		});
		expectPolicyMatch(refPolicy, "testId", "1.0");
		expect(refPolicy.evaluateIfMatch(isA(EvaluationContext.class))).andReturn(Decision.PERMIT);
		c.replay();
		EvaluationContext ctx = ref.createContext(context);
		assertEquals(Decision.PERMIT, ref.evaluateIfMatch(ctx));
		c.verify();
		assertSame(ref, ctx.getCurrentPolicyIDReference());
		assertSame(refPolicy, ctx.getCurrentPolicy());

	}

	@Test
	public void testIsApplicablePolicyCanBeResolved() throws XacmlException
	{
		PolicyIDReference ref = new PolicyIDReference("testId", new VersionMatch("1.+"));
		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.getCurrentPolicy()).andReturn(null);
		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.resolve(ref)).andReturn(refPolicy);
		Capture<EvaluationContext> refContext = new Capture<EvaluationContext>();
		expect(refPolicy.createContext(capture(refContext))).andAnswer(new IAnswer<EvaluationContext>() {
			public EvaluationContext answer() throws Throwable {
				EvaluationContext ctx = (EvaluationContext)EasyMock.getCurrentArguments()[0];
				return refPolicy.new PolicyDelegatingEvaluationContext(ctx);
	        }
		});
		expectPolicyMatch(refPolicy, "testId", "1.0");
		expect(refPolicy.isMatch(isA(EvaluationContext.class))).andReturn(MatchResult.MATCH);
		c.replay();
		EvaluationContext ctx = ref.createContext(context);
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
	public void testReferersTo() throws Exception
	{
		PolicyIDReference ref = new PolicyIDReference("testId", new VersionMatch("1.+"));
		expect(refPolicy.getId()).andReturn("testId");
		expect(refPolicy.getVersion()).andReturn(Version.parse("1.0.1"));
		c.replay();
		assertTrue(ref.isReferenceTo(refPolicy));
		c.verify();
	}
}
