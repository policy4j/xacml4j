package com.artagon.xacml.v3.policy;



public class MockPolicyEvaluationContext extends BaseEvaluationContext
{
	public MockPolicyEvaluationContext(
			AttributeResolver attributeService) {
		super(true, attributeService);
	}	
}
