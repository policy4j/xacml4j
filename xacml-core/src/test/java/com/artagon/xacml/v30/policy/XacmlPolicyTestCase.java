package com.artagon.xacml.v30.policy;

import org.junit.Before;

import com.artagon.xacml.v30.policy.EvaluationContext;

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