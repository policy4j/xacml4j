package com.artagon.xacml.v30.policy;

import com.artagon.xacml.v30.policy.AttributeResolutionService;
import com.artagon.xacml.v30.policy.BaseEvaluationContext;


public class MockPolicyEvaluationContext extends BaseEvaluationContext
{
	public MockPolicyEvaluationContext(
			AttributeResolutionService attributeService) {
		super(attributeService);
	}	
}
