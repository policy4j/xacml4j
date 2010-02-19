package com.artagon.xacml.v3.policy;

import static org.easymock.EasyMock.createStrictMock;

import org.junit.Before;

public class XacmlPolicyTestCase
{	
	protected EvaluationContext context;
	protected MockAttributeResolutionService attributeService;
	protected Policy currentPolicy;
	
	@Before
	public void init_XACMLTestCase() throws Exception{
		this.attributeService = new MockAttributeResolutionService();
		this.currentPolicy = createStrictMock(Policy.class);
		EvaluationContextFactory f = new DefaultEvaluationContextFactory(attributeService);
		this.context = f.createContext(currentPolicy);
	}
}