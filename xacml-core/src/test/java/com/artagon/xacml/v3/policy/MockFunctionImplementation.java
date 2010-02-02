package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.policy.function.FunctionInvocation;

public class MockFunctionImplementation <T extends Value> implements FunctionInvocation
{
	private T expectedResult;
	private boolean failWithIndeterminate = false;
	
	public MockFunctionImplementation(T expectedResult){
		this.expectedResult = expectedResult;
	}
	
	public void setFailWithIndeterminate(boolean fail){
		this.failWithIndeterminate = fail;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T invoke(FunctionSpec spec, EvaluationContext context, Expression ...args) 
		throws FunctionInvocationException 
	{
		if(failWithIndeterminate){
			throw new FunctionInvocationException("Failed to invoke mock function");
		}
		return expectedResult;
	}
	
}
