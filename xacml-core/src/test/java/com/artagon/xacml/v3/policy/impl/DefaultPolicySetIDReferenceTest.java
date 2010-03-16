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
import com.artagon.xacml.v3.policy.AttributeResolver;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationContextFactory;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.MatchResult;
import com.artagon.xacml.v3.policy.PolicyIDReference;
import com.artagon.xacml.v3.policy.PolicyResolutionException;
import com.artagon.xacml.v3.policy.DecisionRuleReferenceResolver;
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
	private DecisionRuleReferenceResolver policyResolver;
	
	@Before
	public void init(){
		this.policyResolver = createStrictMock(DecisionRuleReferenceResolver.class);
		this.policySet = createStrictMock(PolicySet.class);
		this.refPolicySet = createStrictMock(PolicySet.class);
		this.contextFactory = new DefaultEvaluationContextFactory(
				createStrictMock(AttributeResolver.class), 
				policyResolver,
				createStrictMock(XPathProvider.class));
		this.context = contextFactory.createContext(policySet);
	}
	
	@Test
	public void testNoReferencedPolicyFound() throws EvaluationException
	{
		PolicySetIDReference ref = new DefaultPolicySetIDReference("testId", new VersionMatch("1.+"));
		expect(policyResolver.resolve(context, ref)).andThrow(new PolicyResolutionException(context, "Failed to resolve"));
		replay(policyResolver);
		EvaluationContext policyRefContext = ref.createContext(context);
		assertSame(ref, policyRefContext.getCurrentPolicyIDReference());
		verify(policyResolver);
		reset(policyResolver);
		assertEquals(Decision.INDETERMINATE, ref.evaluate(policyRefContext));
		assertEquals(Decision.INDETERMINATE, ref.evaluateIfApplicable(policyRefContext));
		assertEquals(MatchResult.INDETERMINATE, ref.isApplicable(policyRefContext));
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
		//TODO: currently we do not match actual argument
		expect(refPolicySet.evaluate(isA(EvaluationContext.class))).andReturn(Decision.PERMIT);
		replay(policyResolver, refPolicySet);
		EvaluationContext ctx = ref.createContext(context);
		assertEquals(Decision.PERMIT, ref.evaluate(ctx));
		verify(policyResolver, refPolicySet);
	}
}