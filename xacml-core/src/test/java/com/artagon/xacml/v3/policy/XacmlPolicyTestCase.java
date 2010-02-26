package com.artagon.xacml.v3.policy;

import static org.easymock.EasyMock.createStrictMock;

import org.junit.Before;

import com.artagon.xacml.v3.policy.impl.DefaultEvaluationContextFactory;

public class XacmlPolicyTestCase
{	
	protected EvaluationContext context;
	protected EvaluationContextFactory contextFactory;
	protected MockAttributeResolver attributeService;
	protected MockPolicyResolver policyResolver;
	protected Policy currentPolicy;
	
	@Before
	public void init_XACMLTestCase() throws Exception{
		this.attributeService = new MockAttributeResolver();
		this.policyResolver = new MockPolicyResolver();
		this.currentPolicy = createStrictMock(Policy.class);
		this.contextFactory = new DefaultEvaluationContextFactory(attributeService, policyResolver);
		this.context = contextFactory.createContext(currentPolicy);
	}
}