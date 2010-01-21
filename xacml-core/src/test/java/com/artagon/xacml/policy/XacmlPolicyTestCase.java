package com.artagon.xacml.policy;

import org.junit.Before;

public class XacmlPolicyTestCase
{	
	protected EvaluationContext context;
	protected MockAttributeResolutionService attributeService;
	
	@Before
	public void init_XACMLTestCase() throws Exception{
		this.attributeService = new MockAttributeResolutionService();
		this.context = new MockPolicyEvaluationContext(attributeService);
	}
}