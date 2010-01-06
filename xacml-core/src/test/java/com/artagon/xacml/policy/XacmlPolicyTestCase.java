package com.artagon.xacml.policy;

import org.junit.Before;

import com.artagon.xacml.policy.type.DefaultDataTypeFactory;

public class XacmlPolicyTestCase
{	
	protected EvaluationContext context;
	protected MockAttributeResolutionService attributeService;
	protected DataTypeFactory dataTypes;
	
	@Before
	public void init_XACMLTestCase() throws Exception{
		this.attributeService = new MockAttributeResolutionService();
		this.context = new MockPolicyEvaluationContext(attributeService);
		this.dataTypes = new DefaultDataTypeFactory();
	}
}