package com.artagon.xacml.v3.policy;



public class MockPolicyEvaluationContext extends BaseEvaluationContext
{
	public MockPolicyEvaluationContext(
			PolicyInformationPoint attributeService) {
		super(true, attributeService);
	}	
}
