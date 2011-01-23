package com.artagon.xacml.v30;

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

import com.artagon.xacml.v30.Decision;
import com.artagon.xacml.v30.EvaluationContext;
import com.artagon.xacml.v30.EvaluationException;
import com.artagon.xacml.v30.MatchResult;
import com.artagon.xacml.v30.PolicyIDReference;
import com.artagon.xacml.v30.PolicyResolutionException;
import com.artagon.xacml.v30.PolicySet;
import com.artagon.xacml.v30.PolicySetIDReference;
import com.artagon.xacml.v30.Version;
import com.artagon.xacml.v30.VersionMatch;
import com.artagon.xacml.v30.XacmlException;

public class PolicySetIDReferenceTest
{
	private EvaluationContext context;
	private PolicySet policySet;
	private PolicySet refPolicySet;
	
	@Before
	public void init(){
		this.policySet = createStrictMock(PolicySet.class);
		this.refPolicySet = createStrictMock(PolicySet.class);
		this.context = createStrictMock(EvaluationContext.class);
		
	}
	
	@Test
	public void testEvaluateFailToResolveReferenceViaException() throws XacmlException
	{
		PolicySetIDReference ref = new PolicySetIDReference("testId", new VersionMatch("1.+"));
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.resolve(ref)).andThrow(new PolicyResolutionException(context, "Failed to resolve"));
		replay(context, policySet);
		EvaluationContext policyRefContext = ref.createContext(context);
		assertSame(ref, policyRefContext.getCurrentPolicySetIDReference());
		verify(context, policySet);
		reset(context, policySet);

		expect(context.getCurrentPolicySet()).andReturn(policySet);
		expect(policySet.getId()).andReturn("otherTestId");
		expect(policySet.getVersion()).andReturn(Version.parse("1.0"));
		replay(context, policySet);
		assertEquals(Decision.INDETERMINATE, ref.evaluate(policyRefContext));
		verify(context, policySet);
	}
	
	@Test
	public void testEvaluateFailToResolveReferenceReturnsNull() throws XacmlException
	{
		PolicySetIDReference ref = new PolicySetIDReference("testId", new VersionMatch("1.+"));
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.resolve(ref)).andReturn(null);
		replay(context, policySet);
		EvaluationContext policyRefContext = ref.createContext(context);
		assertSame(ref, policyRefContext.getCurrentPolicySetIDReference());
		verify(context, policySet);
		reset(context, policySet);

		expect(context.getCurrentPolicySet()).andReturn(policySet);
		expect(policySet.getId()).andReturn("otherTestId");
		expect(policySet.getVersion()).andReturn(Version.parse("1.0"));
		replay(context, policySet);
		assertEquals(Decision.INDETERMINATE, ref.evaluate(policyRefContext));
		verify(context, policySet);
	}
	
	@Test
	public void testIsApplicableFailToResolveReference() throws XacmlException
	{
		PolicySetIDReference ref = new PolicySetIDReference("testId", new VersionMatch("1.+"));
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.resolve(ref)).andThrow(new PolicyResolutionException(context, "Failed to resolve"));
		replay(context, policySet);
		EvaluationContext policyRefContext = ref.createContext(context);
		assertSame(ref, policyRefContext.getCurrentPolicySetIDReference());
		verify(context, policySet);
		reset(context, policySet);

		expect(context.getCurrentPolicySet()).andReturn(policySet);
		expect(policySet.getId()).andReturn("otherTestId");
		expect(policySet.getVersion()).andReturn(Version.parse("1.0"));
		replay(context, policySet);
		assertEquals(MatchResult.INDETERMINATE, ref.isApplicable(policyRefContext));
		verify(context, policySet);
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
		
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.resolve(ref)).andReturn(refPolicySet);
		Capture<EvaluationContext> refContext = new Capture<EvaluationContext>();
		expect(refPolicySet.createContext(capture(refContext))).andAnswer(new IAnswer<EvaluationContext>() {
			public EvaluationContext answer() throws Throwable {
				EvaluationContext ctx = (EvaluationContext)EasyMock.getCurrentArguments()[0];
				return refPolicySet.new PolicySetDelegatingEvaluationContext(ctx);
	        }
		});
		
		expect(context.getCurrentPolicySet()).andReturn(policySet).anyTimes();
		expect(context.getCurrentPolicy()).andReturn(null).anyTimes();
		
		expect(refPolicySet.getId()).andReturn("testId");
		expect(refPolicySet.getVersion()).andReturn(Version.parse("1.0"));
		expect(refPolicySet.evaluate(isA(EvaluationContext.class))).andReturn(Decision.PERMIT);

		replay(context, refPolicySet);
		EvaluationContext policyRefContext = ref.createContext(context);
		assertEquals(Decision.PERMIT, ref.evaluate(policyRefContext));
		assertSame(ref, policyRefContext.getCurrentPolicySetIDReference());
		assertSame(refPolicySet, policyRefContext.getCurrentPolicySet());
		verify(context, refPolicySet);
	}
	
	@Test
	public void testIsApplicablePolicyIDReference() throws XacmlException
	{
		PolicySetIDReference ref = new PolicySetIDReference("testId", new VersionMatch("1.+"));
		
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.resolve(ref)).andReturn(refPolicySet);
		Capture<EvaluationContext> refContext = new Capture<EvaluationContext>();
		expect(refPolicySet.createContext(capture(refContext))).andAnswer(new IAnswer<EvaluationContext>() {
			public EvaluationContext answer() throws Throwable {
				EvaluationContext ctx = (EvaluationContext)EasyMock.getCurrentArguments()[0];
				return refPolicySet.new PolicySetDelegatingEvaluationContext(ctx);
	        }
		});
		
		expect(context.getCurrentPolicySet()).andReturn(policySet).anyTimes();
		expect(context.getCurrentPolicy()).andReturn(null).anyTimes();
		
		expect(refPolicySet.getId()).andReturn("testId");
		expect(refPolicySet.getVersion()).andReturn(Version.parse("1.0"));
		expect(refPolicySet.isApplicable(isA(EvaluationContext.class))).andReturn(MatchResult.MATCH);

		replay(context, refPolicySet);
		EvaluationContext policyRefContext = ref.createContext(context);
		assertEquals(MatchResult.MATCH, ref.isApplicable(policyRefContext));
		assertSame(ref, policyRefContext.getCurrentPolicySetIDReference());
		assertSame(refPolicySet, policyRefContext.getCurrentPolicySet());
		verify(context, refPolicySet);
	}
	
	@Test
	public void testEvaluateIfApplicable() throws XacmlException
	{
		PolicySetIDReference ref = new PolicySetIDReference("testId", new VersionMatch("1.+"));
		
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.resolve(ref)).andReturn(refPolicySet);
		Capture<EvaluationContext> refContext = new Capture<EvaluationContext>();
		expect(refPolicySet.createContext(capture(refContext))).andAnswer(new IAnswer<EvaluationContext>() {
			public EvaluationContext answer() throws Throwable {
				EvaluationContext ctx = (EvaluationContext)EasyMock.getCurrentArguments()[0];
				return refPolicySet.new PolicySetDelegatingEvaluationContext(ctx);
	        }
		});
		
		expect(context.getCurrentPolicySet()).andReturn(policySet).anyTimes();
		expect(context.getCurrentPolicy()).andReturn(null).anyTimes();
		
		expect(refPolicySet.getId()).andReturn("testId");
		expect(refPolicySet.getVersion()).andReturn(Version.parse("1.0"));
		expect(refPolicySet.evaluateIfApplicable(isA(EvaluationContext.class))).andReturn(Decision.DENY);

		replay(context, refPolicySet);
		EvaluationContext policyRefContext = ref.createContext(context);
		assertEquals(Decision.DENY, ref.evaluateIfApplicable(policyRefContext));
		assertSame(ref, policyRefContext.getCurrentPolicySetIDReference());
		assertSame(refPolicySet, policyRefContext.getCurrentPolicySet());
		verify(context, refPolicySet);
	}
	
}