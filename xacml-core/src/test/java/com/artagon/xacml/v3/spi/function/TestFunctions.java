package com.artagon.xacml.v3.spi.function;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Ignore;

import com.artagon.xacml.v3.AttributeValue;
import com.artagon.xacml.v3.BagOfAttributeValues;
import com.artagon.xacml.v3.EvaluationContext;
import com.artagon.xacml.v3.EvaluationException;
import com.artagon.xacml.v3.Expression;
import com.artagon.xacml.v3.FunctionSpec;
import com.artagon.xacml.v3.types.IntegerType;
import com.artagon.xacml.v3.types.XacmlDataTypes;
import com.artagon.xacml.v3.types.BooleanType.BooleanValue;

@Ignore
public class TestFunctions 
{
	@XacmlFunc(id="test1")
	@XacmlFuncReturnType(type=XacmlDataTypes.BOOLEAN)
	public static BooleanValue test1(
			@XacmlParam(type=XacmlDataTypes.INTEGER)IntegerType.IntegerValue a, 
			@XacmlParam(type=XacmlDataTypes.INTEGER)IntegerType.IntegerValue b) 
	{
		return XacmlDataTypes.BOOLEAN.create(a.equals(b));
	}
	
	@XacmlFunc(id="test2")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerType.IntegerValue test2(
			@XacmlParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerType.IntegerValue> bag)
	{
		return XacmlDataTypes.INTEGER.create(bag.size());
	}
	
	@XacmlFunc(id="test3", evaluateArguments=false)
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerType.IntegerValue and(
			@XacmlParamEvaluationContext EvaluationContext context,
			@XacmlParamVarArg(type=XacmlDataTypes.INTEGER, min=2)IntegerType.IntegerValue ...values) 
		throws EvaluationException
	{
		Long v = 0L;
		for(Expression e : values){
			v += ((IntegerType.IntegerValue)e.evaluate(context)).getValue(); 
			
		}
		return XacmlDataTypes.INTEGER.create(v);
	}
	
	@XacmlFunc(id="test4", evaluateArguments=false)
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static IntegerType.IntegerValue and(
			@XacmlParamEvaluationContext EvaluationContext context,
			@XacmlParamVarArg(type=XacmlDataTypes.INTEGER, min=2)Expression ...values) 
		throws EvaluationException
	{
		Long v = 0L;
		for(Expression e : values){
			v += ((IntegerType.IntegerValue)e.evaluate(context)).getValue(); 
			
		}
		return XacmlDataTypes.INTEGER.create(v);
	}
	
	@XacmlFunc(id="test5")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER, isBag=true)
	public static BagOfAttributeValues<IntegerType.IntegerValue> test5(
			@XacmlParamEvaluationContext EvaluationContext context,
			@XacmlParamFuncReference FunctionSpec function,
			@XacmlParam(type=XacmlDataTypes.INTEGER, isBag=true)BagOfAttributeValues<IntegerType.IntegerValue> bag) 
		throws EvaluationException
	{
		Collection<AttributeValue> attributes = new LinkedList<AttributeValue>();
		for(AttributeValue attr : bag.values()){
			AttributeValue v = function.invoke(context, attr);
			attributes.add(v); 
			
		}
		return XacmlDataTypes.INTEGER.bag(attributes);
	}
	
	@XacmlFunc(id="test5VarArg")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static BooleanValue test5VarArg(
			@XacmlParam(type=XacmlDataTypes.INTEGER)IntegerType.IntegerValue value,
			@XacmlParamVarArg(type=XacmlDataTypes.BOOLEAN, min=0)BooleanValue ...values) 
		throws EvaluationException
	{	
		return XacmlDataTypes.BOOLEAN.create(false);
	}
	
	@XacmlFunc(id="test6VarArg")
	@XacmlFuncReturnType(type=XacmlDataTypes.INTEGER)
	public static BooleanValue test6(
			@XacmlParam(type=XacmlDataTypes.INTEGER)IntegerType.IntegerValue a,
			@XacmlParam(type=XacmlDataTypes.INTEGER)IntegerType.IntegerValue b,
			@XacmlParamVarArg(type=XacmlDataTypes.BOOLEAN, min=0)BooleanValue ...values) 
		throws EvaluationException
	{	
		return XacmlDataTypes.BOOLEAN.create(false);
	}
}
