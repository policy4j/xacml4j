package org.xacml4j.v30.pdp;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createStrictControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.xacml4j.v30.Decision;
import org.xacml4j.v30.EvaluationContext;
import org.xacml4j.v30.EvaluationException;
import org.xacml4j.v30.MatchResult;
import org.xacml4j.v30.PolicyResolutionException;
import org.xacml4j.v30.Version;


public class PolicySetIDReferenceTest
{
	private EvaluationContext context;
	private PolicySet policySet;
	private PolicySet refPolicySet;
	private IMocksControl c;
	@Before
	public void init(){
		this.c = createStrictControl();
		this.policySet = c.createMock(PolicySet.class);
		this.refPolicySet = c.createMock(PolicySet.class);
		this.context = c.createMock(EvaluationContext.class);
		
	}
	
	@Test
	public void testEvaluateFailToResolveReferenceViaException() throws XacmlException
	{
		PolicySetIDReference ref = PolicySetIDReference.builder("testId").versionAsString("1.+").build();
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.resolve(ref)).andThrow(new PolicyResolutionException(context, "Failed to resolve"));
		
		c.replay();
		EvaluationContext policyRefContext = ref.createContext(context);
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
		PolicySetIDReference ref = PolicySetIDReference.builder("testId").versionAsString("1.+").build();
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.resolve(ref)).andReturn(null);
		
		c.replay();
		
		EvaluationContext policyRefContext = ref.createContext(context);
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
		PolicySetIDReference ref = PolicySetIDReference.builder("testId").versionAsString("1.+").build();
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.resolve(ref)).andThrow(new PolicyResolutionException(context, "Failed to resolve"));
		
		c.replay();
		
		EvaluationContext policyRefContext = ref.createContext(context);
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
		PolicySetIDReference ref = PolicySetIDReference.builder("testId").versionAsString("1.+").build();
		ref.evaluate(context);
	}
	
	@Test
	public void testIsApplicablePolicyIDReference() throws XacmlException
	{
		PolicySetIDReference ref = PolicySetIDReference.builder("testId").versionAsString("1.+").build();
		
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.resolve(ref)).andReturn(refPolicySet);
		Capture<EvaluationContext> refContext = new Capture<EvaluationContext>();
		expect(refPolicySet.createContext(capture(refContext))).andAnswer(new IAnswer<EvaluationContext>() {
			@Override
			public EvaluationContext answer() throws Throwable {
				EvaluationContext ctx = (EvaluationContext)EasyMock.getCurrentArguments()[0];
				return refPolicySet.new PolicySetDelegatingEvaluationContext(ctx);
	        }
		});
		
		expect(context.getCurrentPolicySet()).andReturn(policySet).anyTimes();
		expect(context.getCurrentPolicy()).andReturn(null).anyTimes();
		
		expect(refPolicySet.getId()).andReturn("testId");
		expect(refPolicySet.getVersion()).andReturn(Version.parse("1.0"));
		expect(refPolicySet.isMatch(isA(EvaluationContext.class))).andReturn(MatchResult.MATCH);

		c.replay();
		
		EvaluationContext policyRefContext = ref.createContext(context);
		assertEquals(MatchResult.MATCH, ref.isMatch(policyRefContext));
		c.verify();
		
		
		assertSame(ref, policyRefContext.getCurrentPolicySetIDReference());
		assertSame(refPolicySet, policyRefContext.getCurrentPolicySet());
		
	}
	
	@Test
	public void testEvaluateIfApplicable() throws XacmlException
	{
		PolicySetIDReference ref = PolicySetIDReference.builder("testId").versionAsString("1.+").build();
		
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.getCurrentPolicySetIDReference()).andReturn(null);
		expect(context.resolve(ref)).andReturn(refPolicySet);
		Capture<EvaluationContext> refContext = new Capture<EvaluationContext>();
		expect(refPolicySet.createContext(capture(refContext))).andAnswer(new IAnswer<EvaluationContext>() {
			@Override
			public EvaluationContext answer() throws Throwable {
				EvaluationContext ctx = (EvaluationContext)EasyMock.getCurrentArguments()[0];
				return refPolicySet.new PolicySetDelegatingEvaluationContext(ctx);
	        }
		});
		
		expect(context.getCurrentPolicySet()).andReturn(policySet).anyTimes();
		expect(context.getCurrentPolicy()).andReturn(null).anyTimes();
		
		expect(refPolicySet.getId()).andReturn("testId");
		expect(refPolicySet.getVersion()).andReturn(Version.parse("1.0"));
		expect(refPolicySet.evaluate(isA(EvaluationContext.class))).andReturn(Decision.DENY);

		c.replay();
		EvaluationContext policyRefContext = ref.createContext(context);
		assertEquals(Decision.DENY, ref.evaluate(policyRefContext));
		c.verify();
		
		assertSame(ref, policyRefContext.getCurrentPolicySetIDReference());
		assertSame(refPolicySet, policyRefContext.getCurrentPolicySet());
	}
	
}
