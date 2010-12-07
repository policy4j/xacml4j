package com.artagon.xacml.v3.policy;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.XacmlException;
import com.artagon.xacml.v3.XacmlSyntaxException;
import com.artagon.xacml.v3.policy.MatchResult;
import com.artagon.xacml.v3.policy.Policy;
import com.artagon.xacml.v3.policy.PolicyIDReference;
import com.artagon.xacml.v3.policy.PolicyResolutionException;

public class PolicyIDReferenceTest
{
	private EvaluationContext context;
	private Policy refPolicy;
	
	@Before
	public void init(){
		this.refPolicy = createStrictMock(Policy.class);
		this.context = createStrictMock(EvaluationContext.class);
	}
	
	@Test
	public void testEvaluateFailedToResolveReference() throws XacmlException
	{
		PolicyIDReference ref = new PolicyIDReference("testId", new VersionMatch("1.+"));
		
		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.getCurrentPolicy()).andReturn(null);
		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.resolve(ref)).andThrow(new PolicyResolutionException(context, "Failed to resolve"));
		replay(context);
		EvaluationContext policyRefContext = ref.createContext(context);
		assertSame(ref, policyRefContext.getCurrentPolicyIDReference());
		verify(context);
		
		reset(context);
		expect(context.getCurrentPolicy()).andReturn(null);
		replay(context);
		assertEquals(Decision.INDETERMINATE, ref.evaluate(policyRefContext));
		verify(context);
		
		reset(context);
		expect(context.getCurrentPolicy()).andReturn(null);
		replay(context);
		assertEquals(Decision.INDETERMINATE, ref.evaluateIfApplicable(policyRefContext));
		verify(context);
		
		reset(context);
		expect(context.getCurrentPolicy()).andReturn(null);
		replay(context);
		assertEquals(MatchResult.INDETERMINATE, ref.isApplicable(policyRefContext));
		verify(context);
	}
	
	
	@Test
	public void testEvaluateFailedToResolveReferenceAndReturnNull() throws XacmlException
	{
		PolicyIDReference ref = new PolicyIDReference("testId", new VersionMatch("1.+"));
		
		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.getCurrentPolicy()).andReturn(null);
		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.resolve(ref)).andReturn(null);
		replay(context);
		EvaluationContext policyRefContext = ref.createContext(context);
		assertSame(ref, policyRefContext.getCurrentPolicyIDReference());
		verify(context);
		
		reset(context);
		expect(context.getCurrentPolicy()).andReturn(null);
		replay(context);
		assertEquals(Decision.INDETERMINATE, ref.evaluate(policyRefContext));
		verify(context);
		
		reset(context);
		expect(context.getCurrentPolicy()).andReturn(null);
		replay(context);
		assertEquals(Decision.INDETERMINATE, ref.evaluateIfApplicable(policyRefContext));
		verify(context);
		
		reset(context);
		expect(context.getCurrentPolicy()).andReturn(null);
		replay(context);
		assertEquals(MatchResult.INDETERMINATE, ref.isApplicable(policyRefContext));
		verify(context);
	}
	
	@Test
	public void testEvaluateIfApplicableFailedToResolveReferenceViaException() throws XacmlException
	{
		PolicyIDReference ref = new PolicyIDReference("testId", new VersionMatch("1.+"));
		
		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.getCurrentPolicy()).andReturn(null);
		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.resolve(ref)).andThrow(new PolicyResolutionException(context, "Failed to resolve"));
		replay(context);
		EvaluationContext policyRefContext = ref.createContext(context);
		assertSame(ref, policyRefContext.getCurrentPolicyIDReference());
		verify(context);
		
		reset(context);
		expect(context.getCurrentPolicy()).andReturn(null);
		replay(context);
		assertEquals(Decision.INDETERMINATE, ref.evaluateIfApplicable(policyRefContext));
		verify(context);
	}
	
	@Test
	public void testEvaluateIfApplicableFailedToResolveReferenceReturnsNull() throws XacmlException
	{
		PolicyIDReference ref = new PolicyIDReference("testId", new VersionMatch("1.+"));
		
		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.getCurrentPolicy()).andReturn(null);
		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.resolve(ref)).andReturn(null);
		replay(context);
		EvaluationContext policyRefContext = ref.createContext(context);
		assertSame(ref, policyRefContext.getCurrentPolicyIDReference());
		verify(context);
		
		reset(context);
		expect(context.getCurrentPolicy()).andReturn(null);
		replay(context);
		assertEquals(Decision.INDETERMINATE, ref.evaluateIfApplicable(policyRefContext));
		verify(context);
		
	}
	
	@Test
	public void testIsApplicableFailedToResolveReference() throws XacmlException
	{
		PolicyIDReference ref = new PolicyIDReference("testId", new VersionMatch("1.+"));
		
		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.getCurrentPolicy()).andReturn(null);
		expect(context.getCurrentPolicyIDReference()).andReturn(null);
		expect(context.resolve(ref)).andThrow(new PolicyResolutionException(context, "Failed to resolve"));
		replay(context);
		EvaluationContext policyRefContext = ref.createContext(context);
		assertSame(ref, policyRefContext.getCurrentPolicyIDReference());
		verify(context);
				
		reset(context);
		expect(context.getCurrentPolicy()).andReturn(null);
		replay(context);
		assertEquals(MatchResult.INDETERMINATE, ref.isApplicable(policyRefContext));
		verify(context);
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
		expect(refPolicy.evaluate(isA(EvaluationContext.class))).andReturn(Decision.PERMIT);
		replay(context, refPolicy);
		EvaluationContext ctx = ref.createContext(context);
		assertEquals(Decision.PERMIT, ref.evaluate(ctx));
		assertSame(ref, ctx.getCurrentPolicyIDReference());
		assertSame(refPolicy, ctx.getCurrentPolicy());
		verify(context, refPolicy);
	}
	
	@Test
	public void testEvaluateIfApplicablePolicyIDReference() throws XacmlException
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
		expect(refPolicy.evaluateIfApplicable(isA(EvaluationContext.class))).andReturn(Decision.PERMIT);
		replay(context, refPolicy);
		EvaluationContext ctx = ref.createContext(context);
		assertEquals(Decision.PERMIT, ref.evaluateIfApplicable(ctx));
		assertSame(ref, ctx.getCurrentPolicyIDReference());
		assertSame(refPolicy, ctx.getCurrentPolicy());
		verify(context, refPolicy);
	}
	
	@Test
	public void testIsApplicableIDReference() throws XacmlException
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
		expect(refPolicy.isApplicable(isA(EvaluationContext.class))).andReturn(MatchResult.MATCH);
		replay(context, refPolicy);
		EvaluationContext ctx = ref.createContext(context);
		assertEquals(MatchResult.MATCH, ref.isApplicable(ctx));
		assertSame(ref, ctx.getCurrentPolicyIDReference());
		assertSame(refPolicy, ctx.getCurrentPolicy());
		verify(context, refPolicy);
	}

	private void expectPolicyMatch(Policy p, String id, String v) throws XacmlSyntaxException
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
		replay(refPolicy);
		assertTrue(ref.isReferenceTo(refPolicy));
		verify(refPolicy);
	}
}
