package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.policy.AttributeResolutionService;
import com.artagon.xacml.v3.policy.BaseEvaluationContext;


public class MockPolicyEvaluationContext extends BaseEvaluationContext
{
	public MockPolicyEvaluationContext(
			AttributeResolutionService attributeService) {
		super(attributeService);
	}	
}
