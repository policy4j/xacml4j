package com.artagon.xacml.policy;

import java.util.List;

public class MockFunctionImplementation extends BaseFunctionInvocation
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
	protected Value doInvoke(EvaluationContext context, List<Expression> parameters) 
		throws PolicyEvaluationException 
	{
		if(failWithIndeterminate){
			throw new PolicyEvaluationIndeterminateException("Failed to invoke mock function");
		}
		return expectedResult;
	}
	
}
