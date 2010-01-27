package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.policy.function.FunctionInvocationCallback;

public class MockFunctionImplementation <T extends Value> implements FunctionInvocationCallback
{
	private T expectedResult;
	private boolean failWithIndeterminate = false;
	
	public MockFunctionImplementation(T expectedResult){
		this.expectedResult = expectedResult;
	}
	
	public void setFailWithIndeterminate(boolean fail){
		this.failWithIndeterminate = fail;
	}

	@Override
	public T invoke(FunctionSpec spec, EvaluationContext context, Expression ...args) 
		throws EvaluationException 
	{
		if(failWithIndeterminate){
			throw new EvaluationException("Failed to invoke mock function");
		}
		return expectedResult;
	}
	
}
