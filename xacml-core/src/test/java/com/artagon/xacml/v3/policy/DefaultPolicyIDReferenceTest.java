package com.artagon.xacml.v3.policy;

import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.artagon.xacml.v3.Decision;


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
	
	@Test(expected=IllegalArgumentException.class)
	public void testPolicyIDResolutionViaWrongEvaluationContext() throws EvaluationException
	{
		PolicyIDReference ref = new DefaultPolicyIDReference("testId", new VersionMatch("1.+"));
		ref.evaluate(context);
	}
	
	@Test
	public void testEvaluatePolicyIDReference() throws EvaluationException
	{
		PolicyIDReference ref = new DefaultPolicyIDReference("testId", new VersionMatch("1.+"));
		expect(policyResolver.resolve(ref)).andReturn(policy);
		replay(policyResolver);
		EvaluationContext ctx = ref.createContext(context);
		reset(policyResolver);
		expect(policy.getId()).andReturn("testId");
		expect(policy.evaluate(ctx)).andReturn(Decision.PERMIT);
		replay(policyResolver, policy);
		assertEquals(Decision.PERMIT, ref.evaluate(ctx));
		verify(policyResolver, policy);
	}
}
