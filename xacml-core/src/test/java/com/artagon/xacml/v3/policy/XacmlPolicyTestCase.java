package com.artagon.xacml.v3.policy;

import static org.easymock.EasyMock.createStrictMock;

import org.junit.Before;

public class XacmlPolicyTestCase
{	
	protected EvaluationContext context;
	protected MockAttributeResolver attributeService;
	protected MockPolicyResolver policyResolver;
	protected Policy currentPolicy;
	
	@Before
	public void init_XACMLTestCase() throws Exception{
		this.attributeService = new MockAttributeResolver();
		this.policyResolver = new MockPolicyResolver();
		this.currentPolicy = createStrictMock(Policy.class);
		EvaluationContextFactory f = new DefaultEvaluationContextFactory(attributeService, policyResolver);
		this.context = f.createContext(currentPolicy);
	}
}