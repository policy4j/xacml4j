package com.artagon.xacml.v3.policy;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;


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
	
	@Test(expected=PolicyResolutionException.class)
	public void testPolicyIDResolutionFailsWhenCreatingReferenceContext() throws EvaluationException
	{
		PolicyIDReference ref = new DefaultPolicyIDReference("testId", new VersionMatch("1.+"));
		expect(policyResolver.resolve(ref)).andThrow(new PolicyResolutionException("Failed to resolve"));
		replay(policyResolver);
		ref.createContext(context);
		verify(policyResolver);
	}
	
	@Test
	public void testCreatePolicyIDRefEvaluationContext() throws EvaluationException
	{
		PolicyIDReference ref = new DefaultPolicyIDReference("testId", new VersionMatch("1.+"));
		expect(policyResolver.resolve(ref)).andReturn(policy);
		replay(policyResolver, policy);
		EvaluationContext ctx = ref.createContext(context);
		assertEquals(policy, ctx.getCurrentPolicy());
		assertEquals(ref, ctx.getCurrentPolicyIDReference());
		assertEquals(policySet, ctx.getCurrentPolicySet());
		verify(policyResolver, policy);
	}
}
