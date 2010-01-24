package com.artagon.xacml.policy;

import com.artagon.xacml.policy.function.RegularFunction;


public class MockFunctionImplementation implements RegularFunction
{
	private Value expectedResult;
	private boolean failWithIndeterminate = false;
	
	public MockFunctionImplementation(Value expectedResult){
		this.expectedResult = expectedResult;
	}
	
	public void setFailWithIndeterminate(boolean fail){
		this.failWithIndeterminate = fail;
	}

	@Override
	public ValueType getReturnType() {
		return expectedResult.getEvaluatesTo();
	}

	@Override
	public Value invoke(EvaluationContext context, Expression ...args) 
		throws PolicyEvaluationException 
	{
		if(failWithIndeterminate){
			throw new PolicyEvaluationIndeterminateException("Failed to invoke mock function");
		}
		return expectedResult;
	}
	
}
