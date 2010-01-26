package com.artagon.xacml.v3.policy;

import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.Expression;
import com.artagon.xacml.v3.policy.Value;
import com.artagon.xacml.v3.policy.function.StaticallyTypedFunction;


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
			throw new EvaluationException("Failed to invoke mock function");
		}
		return expectedResult;
	}
	
}
