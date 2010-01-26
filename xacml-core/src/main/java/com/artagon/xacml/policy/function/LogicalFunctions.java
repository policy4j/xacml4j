package com.artagon.xacml.policy.function;

import com.artagon.xacml.policy.EvaluationContext;
import com.artagon.xacml.policy.Expression;
import com.artagon.xacml.policy.PolicyEvaluationException;
import com.artagon.xacml.policy.function.annotations.XacmlFunc;
import com.artagon.xacml.policy.function.annotations.XacmlFuncReturnType;
import com.artagon.xacml.policy.function.annotations.XacmlFuncVarArgParam;
import com.artagon.xacml.policy.type.XacmlDataType;
import com.artagon.xacml.policy.type.BooleanType.BooleanValue;

public class LogicalFunctions 
{
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:and", evaluateArguments=false)
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static BooleanValue and(
			EvaluationContext context,
			@XacmlFuncVarArgParam(type=XacmlDataType.BOOLEAN, min=0)Expression ...values) 
		throws PolicyEvaluationException
	{
		// lazy evaluate
		Boolean r = Boolean.TRUE;
		for(Expression e : values){
			Boolean v = ((BooleanValue)e.evaluate(context)).getValue();
			r = r & v; 
			if(!r){
				break;
			}
		}
		return XacmlDataType.BOOLEAN.create(r);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:or", evaluateArguments=false)
	@XacmlFuncReturnType(type=XacmlDataType.BOOLEAN)
	public static BooleanValue or(
			EvaluationContext context,
			@XacmlFuncVarArgParam(type=XacmlDataType.BOOLEAN, min=0)Expression ...values) 
		throws PolicyEvaluationException
	{
		// lazy evaluate
		Boolean r = Boolean.TRUE;
		for(Expression e : values){
			Boolean v = ((BooleanValue)e.evaluate(context)).getValue();
			r = r & v; 
			if(r){
				break;
			}
		}
		return XacmlDataType.BOOLEAN.create(r);
	}
}
