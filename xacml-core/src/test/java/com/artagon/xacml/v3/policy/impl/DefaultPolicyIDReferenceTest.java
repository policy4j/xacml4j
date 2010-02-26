package com.artagon.xacml.v3.policy.impl;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.Decision;
import com.artagon.xacml.v3.policy.AttributeResolver;
import com.artagon.xacml.v3.policy.DecisionCombiningAlgorithm;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationContextFactory;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.MatchResult;
import com.artagon.xacml.v3.policy.Policy;
import com.artagon.xacml.v3.policy.PolicyIDReference;
import com.artagon.xacml.v3.policy.PolicyResolutionException;
import com.artagon.xacml.v3.policy.PolicyResolver;
import com.artagon.xacml.v3.policy.PolicySet;
import com.artagon.xacml.v3.policy.Rule;
import com.artagon.xacml.v3.policy.Version;
import com.artagon.xacml.v3.policy.VersionMatch;

public class DefaultPolicyIDReferenceTest
{
	private EvaluationContext context;
	private PolicySet policySet;
	private Policy policy;
	private EvaluationContextFactory contextFactory;
	private PolicyResolver policyResolver;
	
	@Before
	public void init(){
		this.policyResolver = createStrictMock(PolicyResolver.class);
		this.policySet = createStrictMock(PolicySet.class);
		this.policy = createStrictMock(Policy.class);
		this.contextFactory = new DefaultEvaluationContextFactory(
				createStrictMock(AttributeResolver.class), 
				policyResolver);
		this.context = contextFactory.createContext(policySet);
	}
	
	@Test
	public void testNoReferencedPolicyFound() throws EvaluationException
	{
		PolicyIDReference ref = new DefaultPolicyIDReference("testId", new VersionMatch("1.+"));
		expect(policyResolver.resolve(context, ref)).andThrow(new PolicyResolutionException("Failed to resolve"));
		replay(policyResolver);
		EvaluationContext policyRefContext = ref.createContext(context);
		assertNull(policyRefContext.getCurrentPolicy());
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
	
	@SuppressWarnings("unchecked")
	@Test
	public void testEvaluatePolicyIDReference() throws PolicyResolutionException
	{
		PolicyIDReference ref = new DefaultPolicyIDReference("testId", new VersionMatch("1.+"));
		DecisionCombiningAlgorithm<Rule> combine = createStrictMock(DecisionCombiningAlgorithm.class);
		Rule rule = createStrictMock(Rule.class);
		Policy policy = new DefaultPolicy("testId", Version.valueOf(1), combine, rule);
		expect(policyResolver.resolve(context, ref)).andReturn(policy);
		replay(policyResolver, rule, combine);
		EvaluationContext ctx = ref.createContext(context);
		assertEquals(Decision.PERMIT, ref.evaluate(ctx));
		verify(policyResolver, rule, combine);
	}
}
