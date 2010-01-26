package com.artagon.xacml.v3.policy.function.impl;

import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.Expression;
import com.artagon.xacml.v3.policy.function.annotations.XacmlParamEvaluationContext;
import com.artagon.xacml.v3.policy.function.annotations.XacmlFunc;
import com.artagon.xacml.v3.policy.function.annotations.XacmlParam;
import com.artagon.xacml.v3.policy.function.annotations.XacmlFuncReturnType;
import com.artagon.xacml.v3.policy.function.annotations.XacmlParamVarArg;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.BooleanType.BooleanValue;
import com.artagon.xacml.v3.policy.type.IntegerType.IntegerValue;

public class LogicalFunctions 
{
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:and", evaluateArguments=false)
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue and(
			@XacmlParamEvaluationContext EvaluationContext context,
			@XacmlParamVarArg(type=DataTypes.BOOLEAN, min=0)Expression ...values) 
		throws EvaluationException
	{
		Boolean r = Boolean.TRUE;
		for(Expression e : values){
			r = r & ((BooleanValue)e.evaluate(context)).getValue(); 
			if(!r){
				break;
			}
		}
		return DataTypes.BOOLEAN.create(r);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:or", evaluateArguments=false)
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue or(
			@XacmlParamEvaluationContext EvaluationContext context,
			@XacmlParamVarArg(type=DataTypes.BOOLEAN, min=0)Expression...values) 
		throws EvaluationException
	{
		Boolean r = Boolean.TRUE;
		for(Expression e : values){
			Boolean v = ((BooleanValue)e.evaluate(context)).getValue();
			r = r & v; 
			if(r){
				break;
			}
		}
		return DataTypes.BOOLEAN.create(r);
	}
	
	@XacmlFunc(id="urn:oasis:names:tc:xacml:1.0:function:or")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue nof(
			@XacmlParamEvaluationContext EvaluationContext context,
			@XacmlParam(type=DataTypes.INTEGER)IntegerValue n,
			@XacmlParamVarArg(type=DataTypes.BOOLEAN, min=0)Expression...values) 
		throws EvaluationException
	{
		Boolean r = Boolean.TRUE;
		if(values.length < n.getValue()){
			throw new EvaluationException("Number of arguments=\"%s\" is " +
					"less than minimum required number=\"%s\"", values.length, n.getValue());
		}
		for(int i = 0; i < n.getValue(); i++ ){
			r &= ((BooleanValue)values[i].evaluate(context)).getValue();
		}
		return DataTypes.BOOLEAN.create(r);
	}
}
