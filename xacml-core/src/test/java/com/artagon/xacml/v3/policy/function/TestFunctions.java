package com.artagon.xacml.v3.policy.function;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Ignore;

import com.artagon.xacml.v3.policy.AttributeValue;
import com.artagon.xacml.v3.policy.BagOfAttributeValues;
import com.artagon.xacml.v3.policy.EvaluationContext;
import com.artagon.xacml.v3.policy.EvaluationException;
import com.artagon.xacml.v3.policy.Expression;
import com.artagon.xacml.v3.policy.FunctionSpec;
import com.artagon.xacml.v3.policy.function.annotations.XacmlFunc;
import com.artagon.xacml.v3.policy.function.annotations.XacmlFuncReturnType;
import com.artagon.xacml.v3.policy.function.annotations.XacmlParam;
import com.artagon.xacml.v3.policy.function.annotations.XacmlParamEvaluationContext;
import com.artagon.xacml.v3.policy.function.annotations.XacmlParamFuncReference;
import com.artagon.xacml.v3.policy.function.annotations.XacmlParamVarArg;
import com.artagon.xacml.v3.policy.type.DataTypes;
import com.artagon.xacml.v3.policy.type.IntegerType;
import com.artagon.xacml.v3.policy.type.BooleanType.BooleanValue;

@Ignore
public class TestFunctions 
{
	@XacmlFunc(id="test1")
	@XacmlFuncReturnType(type=DataTypes.BOOLEAN)
	public static BooleanValue test1(
			@XacmlParam(type=DataTypes.INTEGER)IntegerType.IntegerValue a, 
			@XacmlParam(type=DataTypes.INTEGER)IntegerType.IntegerValue b)
	{
		return DataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="test2")
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerType.IntegerValue test2(
			@XacmlParam(type=DataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerType.IntegerValue> bag)
	{
		return DataTypes.INTEGER.create(bag.size());
	}
	
	@XacmlFunc(id="test3", evaluateArguments=false)
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerType.IntegerValue and(
			@XacmlParamEvaluationContext EvaluationContext context,
			@XacmlParamVarArg(type=DataTypes.INTEGER, min=1)IntegerType.IntegerValue ...values) 
		throws EvaluationException
	{
		Long v = 0L;
		for(Expression e : values){
			v += ((IntegerType.IntegerValue)e.evaluate(context)).getValue(); 
			
		}
		return DataTypes.INTEGER.create(v);
	}
	
	@XacmlFunc(id="test4", evaluateArguments=false)
	@XacmlFuncReturnType(type=DataTypes.INTEGER)
	public static IntegerType.IntegerValue and(
			@XacmlParamEvaluationContext EvaluationContext context,
			@XacmlParamVarArg(type=DataTypes.INTEGER, min=1)Expression ...values) 
		throws EvaluationException
	{
		Long v = 0L;
		for(Expression e : values){
			v += ((IntegerType.IntegerValue)e.evaluate(context)).getValue(); 
			
		}
		return DataTypes.INTEGER.create(v);
	}
	
	@XacmlFunc(id="test5")
	@XacmlFuncReturnType(type=DataTypes.INTEGER, isBag=true)
	public static BagOfAttributeValues<IntegerType.IntegerValue> test4(
			@XacmlParamEvaluationContext EvaluationContext context,
			@XacmlParamFuncReference FunctionSpec function,
			@XacmlParam(type=DataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerType.IntegerValue> bag) 
		throws EvaluationException
	{
		Collection<AttributeValue> attributes = new LinkedList<AttributeValue>();
		for(AttributeValue attr : bag.values()){
			AttributeValue v = function.invoke(context, attr);
			attributes.add(v); 
			
		}
		return DataTypes.INTEGER.bag(attributes);
	}
	
	@XacmlFunc(id="test5VarArg")
	@XacmlFuncReturnType(type=DataTypes.INTEGER, isBag=true)
	public static BooleanValue test5(
			@XacmlParam(type=DataTypes.INTEGER)IntegerType.IntegerValue value,
			@XacmlParamVarArg(type=DataTypes.BOOLEAN, min=0)BooleanValue ...values) 
		throws EvaluationException
	{	
		return DataTypes.BOOLEAN.create(false);
	}
	
	@XacmlFunc(id="test6VarArg")
	@XacmlFuncReturnType(type=DataTypes.INTEGER, isBag=true)
	public static BooleanValue test6(
			@XacmlParam(type=DataTypes.INTEGER)IntegerType.IntegerValue a,
			@XacmlParam(type=DataTypes.INTEGER)IntegerType.IntegerValue b,
			@XacmlParamVarArg(type=DataTypes.BOOLEAN, min=0)BooleanValue ...values) 
		throws EvaluationException
	{	
		return DataTypes.BOOLEAN.create(false);
	}
}
