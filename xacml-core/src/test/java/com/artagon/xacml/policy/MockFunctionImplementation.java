package com.artagon.xacml.policy;

import java.util.List;

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
	public Value invoke(EvaluationContext context, List<Expression> parameters) 
		throws PolicyEvaluationException 
	{
		if(failWithIndeterminate){
			throw new PolicyEvaluationIndeterminateException("Failed to invoke mock function");
		}
		return expectedResult;
	}
	
}
