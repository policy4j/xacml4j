package com.artagon.xacml.policy;

import com.artagon.xacml.policy.function.StaticallyTypedFunction;


public class MockFunctionImplementation extends StaticallyTypedFunction
{
	private Value expectedResult;
	private boolean failWithIndeterminate = false;
	
	public MockFunctionImplementation(Value expectedResult){
		super(expectedResult.getEvaluatesTo());
		this.expectedResult = expectedResult;
	}
	
	public void setFailWithIndeterminate(boolean fail){
		this.failWithIndeterminate = fail;
	}

	@Override
	public Value invoke(EvaluationContext context, Expression ...args) 
		throws EvaluationException 
	{
		if(failWithIndeterminate){
			throw new EvaluationIndeterminateException("Failed to invoke mock function");
		}
		return expectedResult;
	}
	
}
